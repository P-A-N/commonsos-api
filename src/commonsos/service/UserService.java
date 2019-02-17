package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.web3j.crypto.Credentials;

import commonsos.JobService;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.PasswordResetRequest;
import commonsos.repository.entity.TemporaryEmailAddress;
import commonsos.repository.entity.TemporaryUser;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.DelegateWalletTask;
import commonsos.service.command.CreateAccountTemporaryCommand;
import commonsos.service.command.MobileDeviceUpdateCommand;
import commonsos.service.command.PasswordResetRequestCommand;
import commonsos.service.command.UpdateEmailTemporaryCommand;
import commonsos.service.command.UploadPhotoCommand;
import commonsos.service.command.UserNameUpdateCommand;
import commonsos.service.command.UserPasswordResetRequestCommand;
import commonsos.service.command.UserStatusUpdateCommand;
import commonsos.service.command.UserUpdateCommand;
import commonsos.service.command.UserUpdateCommunitiesCommand;
import commonsos.service.crypto.AccessIdService;
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.session.UserSession;
import commonsos.util.CommunityUtil;
import commonsos.util.UserUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.BalanceView;
import commonsos.view.CommunityView;
import commonsos.view.UserPrivateView;
import commonsos.view.UserView;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserService {
  public static final String WALLET_PASSWORD = "test";

  @Inject private UserRepository userRepository;
  @Inject private CommunityRepository communityRepository;
  @Inject private BlockchainService blockchainService;
  @Inject private CryptoService cryptoService;
  @Inject private AccessIdService accessIdService;
  @Inject private EmailService emailService;
  @Inject private TransactionService transactionService;
  @Inject private ImageUploadService imageService;
  @Inject private JobService jobService;

  public User checkPassword(String username, String password) {
    User user = userRepository.findByUsername(username).orElseThrow(AuthenticationException::new);
    if (!cryptoService.checkPassword(password, user.getPasswordHash())) throw new AuthenticationException();
    return user;
  }

  public UserPrivateView privateView(User user) {
    List<BalanceView> balanceList = new ArrayList<>();
    List<CommunityView> communityList = new ArrayList<>();
    if (user.getCommunityList() != null) {
      user.getCommunityList().forEach(c -> {
        balanceList.add(transactionService.balance(user, c.getId()));
        communityList.add(CommunityUtil.view(c, blockchainService.tokenSymbol(c.getId())));
      });
    }
    
    return UserUtil.privateView(user, balanceList, communityList);
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    User user = userRepository.findStrictById(userId);
    boolean isAdmin = user.getCommunityList().stream().anyMatch(c -> {
      return c.getAdminUser() != null && c.getAdminUser().getId().equals(currentUser.getId());
    });
    if (!currentUser.getId().equals(user.getId()) && !isAdmin) throw new ForbiddenException();

    return privateView(user);
  }

  public void createAccountTemporary(CreateAccountTemporaryCommand command) {
    validate(command);
    if (userRepository.isUsernameTaken(command.getUsername())) throw new DisplayableException("error.usernameTaken");
    if (userRepository.isEmailAddressTaken(command.getEmailAddress())) throw new DisplayableException("error.emailAddressTaken");
    List<Community> communityList = new ArrayList<>();
    if (command.getCommunityList() != null && !command.getCommunityList().isEmpty()) {
      communityList = communityList(command.getCommunityList());
    }

    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !userRepository.findTemporaryUser(accessIdHash).isPresent();
    });

    TemporaryUser tmpUser = new TemporaryUser()
      .setAccessIdHash(cryptoService.hash(accessId))
      .setExpirationTime(Instant.now().plus(1, ChronoUnit.DAYS))
      .setInvalid(false)
      .setUsername(command.getUsername())
      .setPasswordHash(cryptoService.encryptoPassword(command.getPassword()))
      .setFirstName(command.getFirstName())
      .setLastName(command.getLastName())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setCommunityList(communityList)
      .setEmailAddress(command.getEmailAddress())
      .setWaitUntilCompleted(command.isWaitUntilCompleted());

    userRepository.createTemporary(tmpUser);

    emailService.sendCreateAccountTemporary(tmpUser.getEmailAddress(), tmpUser.getUsername(), accessId);
  }

  public User createAccountComplete(String accessId) {
    // validate
    if (!blockchainService.isConnected()) throw new RuntimeException("Cannot create user, technical error with blockchain");
    TemporaryUser tempUser = userRepository.findStrictTemporaryUser(cryptoService.hash(accessId));
    
    // prepare
    User user = new User()
        .setCommunityList(new ArrayList<>(tempUser.getCommunityList()))
        .setUsername(tempUser.getUsername())
        .setPasswordHash(tempUser.getPasswordHash())
        .setFirstName(tempUser.getFirstName())
        .setLastName(tempUser.getLastName())
        .setDescription(tempUser.getDescription())
        .setLocation(tempUser.getLocation())
        .setEmailAddress(tempUser.getEmailAddress())
        .setStatus("");

    String wallet = blockchainService.createWallet(WALLET_PASSWORD);
    Credentials credentials = blockchainService.credentials(wallet, WALLET_PASSWORD);

    user.setWallet(wallet);
    user.setWalletAddress(credentials.getAddress());
    
    user.getCommunityList().forEach(c -> {
      DelegateWalletTask task = new DelegateWalletTask(user, c);
      if (tempUser.isWaitUntilCompleted())
        jobService.execute(task);
      else
        jobService.submit(user, task);
    });

    // create
    userRepository.updateTemporary(tempUser.setInvalid(true));
    return userRepository.create(user);
  }
  
  public void updateEmailTemporary(UpdateEmailTemporaryCommand command) {
    ValidateUtil.validateEmailAddress(command.getNewEmailAddress());
    User user = userRepository.findStrictById(command.getUserId());
    if (user.getEmailAddress() != null && user.getEmailAddress().equals(command.getNewEmailAddress())) throw new BadRequestException("new address is same as now");
    if (userRepository.isEmailAddressTaken(command.getNewEmailAddress())) throw new DisplayableException("error.emailAddressTaken");

    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !userRepository.findTemporaryEmailAddress(accessIdHash).isPresent();
    });
    
    TemporaryEmailAddress tmpEmailAddr = new TemporaryEmailAddress()
      .setAccessIdHash(cryptoService.hash(accessId))
      .setExpirationTime(Instant.now().plus(1, ChronoUnit.DAYS))
      .setInvalid(false)
      .setUserId(command.getUserId())
      .setEmailAddress(command.getNewEmailAddress());
    
    userRepository.createTemporaryEmailAddress(tmpEmailAddr);
    
    emailService.sendUpdateEmailTemporary(command.getNewEmailAddress(), user.getUsername(), user.getId(), accessId);
  }

  public void updateEmailComplete(Long userId, String accessId) {
    // validate
    User user = userRepository.findStrictById(userId);
    TemporaryEmailAddress tmpEmailAddr = userRepository.findStrictTemporaryEmailAddress(cryptoService.hash(accessId));
    if (!user.getId().equals(tmpEmailAddr.getUserId())) throw new BadRequestException("invalid user id");
    
    // update
    user.setEmailAddress(tmpEmailAddr.getEmailAddress());
    userRepository.updateTemporaryEmailAddress(tmpEmailAddr.setInvalid(true));
    userRepository.update(user);
  }

  public void passwordResetRequest(PasswordResetRequestCommand command) {
    Optional<User> user = userRepository.findByEmailAddress(command.getEmailAddress());
    if (!user.isPresent()) {
      log.info("user not found by given email address.");
      return;
    }

    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !userRepository.findPasswordResetRequest(accessIdHash).isPresent();
    });

    PasswordResetRequest passReset = new PasswordResetRequest()
      .setAccessIdHash(cryptoService.hash(accessId))
      .setExpirationTime(Instant.now().plus(10, ChronoUnit.MINUTES))
      .setInvalid(false)
      .setUserId(user.get().getId());
    
    userRepository.createPasswordResetRequest(passReset);
    
    emailService.sendPasswordReset(user.get().getEmailAddress(), user.get().getUsername(), accessId);
  }

  public void passwordResetRequestCheck(String accessId) {
    userRepository.findStrictPasswordResetRequest(cryptoService.hash(accessId));
  }

  public void passwordReset(String accessId, String newPassword) {
    // validate
    ValidateUtil.validatePassword(newPassword);
    PasswordResetRequest passReset = userRepository.findStrictPasswordResetRequest(cryptoService.hash(accessId));
    User user = userRepository.findStrictById(passReset.getUserId());
    
    // update
    user.setPasswordHash(cryptoService.encryptoPassword(newPassword));
    userRepository.updatePasswordResetRequest(passReset.setInvalid(true));
    userRepository.update(user);
  }

  private List<Community> communityList(List<Long> communityList) {
    List<Community> result = new ArrayList<>();
    communityList.forEach(id -> {
      result.add(communityRepository.findStrictById(id));
    });
    return result;
  }

  void validate(CreateAccountTemporaryCommand command) {
    ValidateUtil.validateUsername(command.getUsername());
    ValidateUtil.validatePassword(command.getPassword());
//    if (command.getFirstName() == null || command.getFirstName().length() < 1) throw new BadRequestException("invalid first name");
//    if (command.getLastName() == null || command.getLastName().length() < 1) throw new BadRequestException("invalid last name");
    ValidateUtil.validateEmailAddress(command.getEmailAddress());
  }
  
  public UserView view(Long id) {
    return UserUtil.view(userRepository.findStrictById(id));
  }

  public User user(Long id) {
    return userRepository.findStrictById(id);
  }

  public List<UserView> searchUsers(User user, Long communityId, String query) {
    return userRepository.search(communityId, query).stream().filter(u -> !u.getId().equals(user.getId())).map(UserUtil::view).collect(toList());
  }

  public String updateAvatar(User user, UploadPhotoCommand command) {
    String url = imageService.create(command);
    imageService.delete(user.getAvatarUrl());
    user.setAvatarUrl(url);
    userRepository.update(user);
    return url;
  }

  public UserSession session(User user) {
    return new UserSession().setUserId(user.getId()).setUsername(user.getUsername());
  }

  public User updateUser(User user, UserUpdateCommand command) {
    user.setFirstName(command.getFirstName());
    user.setLastName(command.getLastName());
    user.setDescription(command.getDescription());
    user.setLocation(command.getLocation());
    return userRepository.update(user);
  }

  public User updateUserCommunities(User user, UserUpdateCommunitiesCommand command) {
    List<Community> communityList = new ArrayList<>();
    if (command.getCommunityList() != null && !command.getCommunityList().isEmpty()) {
      communityList = communityList(command.getCommunityList());
    }

    user.setCommunityList(communityList);

    user.getCommunityList().forEach(c -> {
      DelegateWalletTask task = new DelegateWalletTask(user, c);
      jobService.submit(user, task);
    });

    return userRepository.update(user);
  }

  public User updateUserName(User user, UserNameUpdateCommand command) {
    ValidateUtil.validateUsername(command.getUsername());
    if (userRepository.isUsernameTaken(command.getUsername())) throw new DisplayableException("error.usernameTaken");
    
    user.setUsername(command.getUsername());
    return userRepository.update(user);
  }

  public User updateStatus(User user, UserStatusUpdateCommand command) {
    ValidateUtil.validateStatus(command.getStatus());
    
    user.setStatus(command.getStatus());
    return userRepository.update(user);
  }

  public void userPasswordResetRequest(User user, UserPasswordResetRequestCommand command) {
    if (!cryptoService.checkPassword(command.getCurrentPassword(), user.getPasswordHash())) throw new AuthenticationException();
    
    String accessId = accessIdService.generateAccessId(id -> {
      String accessIdHash = cryptoService.hash(id);
      return !userRepository.findPasswordResetRequest(accessIdHash).isPresent();
    });

    PasswordResetRequest passReset = new PasswordResetRequest()
      .setAccessIdHash(cryptoService.hash(accessId))
      .setExpirationTime(Instant.now().plus(10, ChronoUnit.MINUTES))
      .setInvalid(false)
      .setUserId(user.getId());
    
    userRepository.createPasswordResetRequest(passReset);
    
    emailService.sendPasswordReset(user.getEmailAddress(), user.getUsername(), accessId);
  }

  public void updateMobileDevice(User user, MobileDeviceUpdateCommand command) {
    user.setPushNotificationToken(command.getPushNotificationToken());
    userRepository.update(user);
  }
}

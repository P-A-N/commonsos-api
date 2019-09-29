package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;

import commonsos.Configuration;
import commonsos.JobService;
import commonsos.command.PaginationCommand;
import commonsos.command.admin.SearchUserForAdminCommand;
import commonsos.command.app.CreateUserTemporaryCommand;
import commonsos.command.app.LastViewTimeUpdateCommand;
import commonsos.command.app.MobileDeviceUpdateCommand;
import commonsos.command.app.PasswordResetRequestCommand;
import commonsos.command.app.UpdateEmailTemporaryCommand;
import commonsos.command.app.UploadPhotoCommand;
import commonsos.command.app.UserNameUpdateCommand;
import commonsos.command.app.UserPasswordResetRequestCommand;
import commonsos.command.app.UserStatusUpdateCommand;
import commonsos.command.app.UserUpdateCommand;
import commonsos.command.app.UserUpdateCommunitiesCommand;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.PasswordResetRequest;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.TemporaryEmailAddress;
import commonsos.repository.entity.TemporaryUser;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.DelegateWalletTask;
import commonsos.service.blockchain.TokenBalance;
import commonsos.service.crypto.AccessIdService;
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.service.image.QrCodeService;
import commonsos.session.UserSession;
import commonsos.util.CommunityUtil;
import commonsos.util.PaginationUtil;
import commonsos.util.TransactionUtil;
import commonsos.util.UserUtil;
import commonsos.util.ValidateUtil;
import commonsos.view.CommunityUserListView;
import commonsos.view.CommunityUserView;
import commonsos.view.UserListView;
import commonsos.view.UserTokenBalanceView;
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
  @Inject private ImageUploadService imageUploadService;
  @Inject private QrCodeService qrCodeService;
  @Inject private JobService jobService;
  @Inject private Configuration config;

  public User checkPassword(String username, String password) {
    User user = userRepository.findByUsername(username).orElseThrow(AuthenticationException::new);
    if (!cryptoService.checkPassword(password, user.getPasswordHash())) throw new AuthenticationException();
    return user;
  }

  public UserView privateView(User user) {
    List<UserTokenBalanceView> balanceList = new ArrayList<>();
    List<CommunityUserView> communityUserList = new ArrayList<>();
    user.getCommunityUserList().forEach(cu -> {
      TokenBalance tokenBalance = blockchainService.getTokenBalance(user, cu.getCommunity().getId());
      balanceList.add(TransactionUtil.userTokenBalanceView(tokenBalance));
      communityUserList.add(UserUtil.communityUserViewForApp(cu, tokenBalance));
    });
    
    return UserUtil.privateViewForApp(user, balanceList, communityUserList);
  }

  public UserView privateView(User currentUser, Long userId) {
    User user = userRepository.findStrictById(userId);
    boolean isAdmin = user.getCommunityUserList().stream().map(CommunityUser::getCommunity).anyMatch(c -> {
      return c.getAdminUser() != null && c.getAdminUser().getId().equals(currentUser.getId());
    });
    if (!currentUser.getId().equals(user.getId()) && !isAdmin) throw new ForbiddenException();

    return privateView(user);
  }
  
  public UserView publicUserAndCommunityView(Long id) {
    User user = userRepository.findStrictById(id);
    return publicUserAndCommunityView(user);
  }
  
  public UserView publicUserAndCommunityView(User user) {
    List<CommunityUserView> communityList = new ArrayList<>();
    user.getCommunityUserList().stream().map(CommunityUser::getCommunity).forEach(c -> {
      communityList.add(CommunityUtil.viewForApp(c, blockchainService.tokenSymbol(c.getTokenContractAddress())));
    });
    
    return UserUtil.publicViewForApp(user, communityList);
  }

  public CommunityUserListView searchUsersCommunity(User user, String filter, PaginationCommand pagination) {
    ResultList<CommunityUser> result = StringUtils.isEmpty(filter) ?
        communityRepository.listPublic(user.getCommunityUserList(), pagination) :
        communityRepository.listPublic(filter, user.getCommunityUserList(), pagination);
    
    CommunityUserListView listView = new CommunityUserListView();
    listView.setCommunityList(result.getList().stream().map(cu -> {
      TokenBalance tokenBalance = blockchainService.getTokenBalance(user, cu.getCommunity().getId());
      return UserUtil.communityUserViewForApp(cu, tokenBalance);
    }).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
  
  public void createAccountTemporary(CreateUserTemporaryCommand command) {
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
      .setTelNo(command.getTelNo())
      .setWaitUntilCompleted(command.isWaitUntilCompleted());

    userRepository.createTemporary(tmpUser);

    emailService.sendCreateAccountTemporary(tmpUser.getEmailAddress(), tmpUser.getUsername(), accessId);
  }

  public User createAccountComplete(String accessId) {
    // validate
    if (!blockchainService.isConnected()) throw new RuntimeException("Cannot create user, technical error with blockchain");
    TemporaryUser tempUser = userRepository.findStrictTemporaryUser(cryptoService.hash(accessId));
    
    // prepare
    List<CommunityUser> communityUserList = new ArrayList<>();
    tempUser.getCommunityList().forEach(c -> communityUserList.add(new CommunityUser().setCommunity(c)));
    User user = new User()
        .setCommunityUserList(communityUserList)
        .setUsername(tempUser.getUsername())
        .setPasswordHash(tempUser.getPasswordHash())
        .setFirstName(tempUser.getFirstName())
        .setLastName(tempUser.getLastName())
        .setDescription(tempUser.getDescription())
        .setLocation(tempUser.getLocation())
        .setEmailAddress(tempUser.getEmailAddress())
        .setTelNo(tempUser.getTelNo())
        .setStatus("")
        .setLoggedinAt(Instant.now());

    String wallet = blockchainService.createWallet(WALLET_PASSWORD);
    Credentials credentials = blockchainService.credentials(wallet, WALLET_PASSWORD);

    user.setWallet(wallet);
    user.setWalletAddress(credentials.getAddress());
    
    user.getCommunityUserList().forEach(cu -> {
      DelegateWalletTask task = new DelegateWalletTask(user, cu.getCommunity());
      if (tempUser.isWaitUntilCompleted())
        jobService.execute(task);
      else
        jobService.submit(user, task);
    });

    // create
    userRepository.lockForUpdate(tempUser);
    userRepository.updateTemporary(tempUser.setInvalid(true));
    userRepository.create(user);

    return user;
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
    userRepository.lockForUpdate(user);
    userRepository.lockForUpdate(tmpEmailAddr);
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
    userRepository.lockForUpdate(user);
    userRepository.lockForUpdate(passReset);
    user.setPasswordHash(cryptoService.encryptoPassword(newPassword));
    userRepository.updatePasswordResetRequest(passReset.setInvalid(true));
    userRepository.update(user);
  }

  private List<Community> communityList(List<Long> communityList) {
    List<Community> result = new ArrayList<>();
    communityList.forEach(id -> {
      result.add(communityRepository.findPublicStrictById(id));
    });
    return result;
  }

  void validate(CreateUserTemporaryCommand command) {
    ValidateUtil.validateUsername(command.getUsername());
    ValidateUtil.validatePassword(command.getPassword());
//    if (command.getFirstName() == null || command.getFirstName().length() < 1) throw new BadRequestException("invalid first name");
//    if (command.getLastName() == null || command.getLastName().length() < 1) throw new BadRequestException("invalid last name");
    ValidateUtil.validateEmailAddress(command.getEmailAddress());
    ValidateUtil.validateTelNo(command.getTelNo());
  }

  public User user(Long id) {
    return userRepository.findStrictById(id);
  }

  public UserListView searchUsers(User user, Long communityId, String query, PaginationCommand pagination) {
    ResultList<User> result = userRepository.search(communityId, query, pagination);
    
    UserListView listView = new UserListView();
    listView.setUserList(result.getList().stream().filter(u -> !u.getId().equals(user.getId())).map(this::publicUserAndCommunityView).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public UserListView searchUsersForAdmin(Admin admin, SearchUserForAdminCommand command, PaginationCommand pagination) {
    ResultList<User> result = userRepository.search(command.getUsername(), command.getEmailAddress(), command.getCommunityId(), pagination);
    
    UserListView listView = new UserListView();
    listView.setUserList(result.getList().stream().map(u -> UserUtil.wideViewForAdmin(u, balanceViewList(u))).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }

  public String updateAvatar(User user, UploadPhotoCommand command) {
    String url = imageUploadService.create(command, "");
    imageUploadService.delete(user.getAvatarUrl());
    
    userRepository.lockForUpdate(user);
    user.setAvatarUrl(url);
    userRepository.update(user);
    return url;
  }

  public UserSession session(User user) {
    return new UserSession().setUserId(user.getId()).setUsername(user.getUsername());
  }

  public User updateUser(User user, UserUpdateCommand command) {
    userRepository.lockForUpdate(user);
    user.setFirstName(command.getFirstName());
    user.setLastName(command.getLastName());
    user.setDescription(command.getDescription());
    user.setLocation(command.getLocation());
    user.setTelNo(command.getTelNo());
    return userRepository.update(user);
  }

  public User updateUserCommunities(User user, UserUpdateCommunitiesCommand command) {
    // create new communityUserList
    List<CommunityUser> oldCommunityUserList = user.getCommunityUserList();
    List<Long> oldCommunityIdList = oldCommunityUserList.stream().map(CommunityUser::getCommunity).map(Community::getId).collect(Collectors.toList());
    List<CommunityUser> newCommunityUserList = new ArrayList<>();
    List<Long> newCommunityIdList = command.getCommunityList();
    oldCommunityUserList.forEach(cu -> {
      if (newCommunityIdList.contains(cu.getCommunity().getId())) {
        newCommunityUserList.add(cu);
      }
    });
    newCommunityIdList.forEach(id -> {
      if (!oldCommunityIdList.contains(id)) {
        newCommunityUserList.add(new CommunityUser().setCommunity(communityRepository.findPublicStrictById(id)));
      }
    });
    newCommunityUserList.sort((c1, c2) -> c1.getCommunity().getId().compareTo(c2.getCommunity().getId()));

    // set new communityUserList
    userRepository.lockForUpdate(user);
    user.setCommunityUserList(newCommunityUserList);

    // update
    user.getCommunityUserList().forEach(cu -> {
      DelegateWalletTask task = new DelegateWalletTask(user, cu.getCommunity());
      jobService.submit(user, task);
    });

    return userRepository.update(user);
  }

  public User updateUserName(User user, UserNameUpdateCommand command) {
    ValidateUtil.validateUsername(command.getUsername());
    if (userRepository.isUsernameTaken(command.getUsername())) throw new DisplayableException("error.usernameTaken");
    
    userRepository.lockForUpdate(user);
    user.setUsername(command.getUsername());
    return userRepository.update(user);
  }

  public User updateStatus(User user, UserStatusUpdateCommand command) {
    ValidateUtil.validateStatus(command.getStatus());
    
    userRepository.lockForUpdate(user);
    user.setStatus(command.getStatus());
    return userRepository.update(user);
  }

  public User updateLoggedinAt(User user) {
    userRepository.lockForUpdate(user);
    user.setLoggedinAt(Instant.now());
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
    userRepository.lockForUpdate(user);
    user.setPushNotificationToken(command.getPushNotificationToken());
    userRepository.update(user);
  }

  public void updateWalletLastViewTime(User user, LastViewTimeUpdateCommand command) {
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, community)) throw new BadRequestException(String.format("User is not a member of community. communityId=%d", community.getId()));
    
    userRepository.lockForUpdate(user);
    user.getCommunityUserList().forEach(cu -> {
      if (cu.getCommunity().getId().equals(community.getId())) {
        cu.setWalletLastViewTime(Instant.now());
      }
    });
    
    userRepository.update(user);
  }

  public void updateAdLastViewTime(User user, LastViewTimeUpdateCommand command) {
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, community)) throw new BadRequestException(String.format("User is not a member of community. communityId=%d", community.getId()));
    
    userRepository.lockForUpdate(user);
    user.getCommunityUserList().forEach(cu -> {
      if (cu.getCommunity().getId().equals(community.getId())) {
        cu.setAdLastViewTime(Instant.now());
      }
    });
    
    userRepository.update(user);
  }

  public void updateNotificationLastViewTime(User user, LastViewTimeUpdateCommand command) {
    Community community = communityRepository.findPublicStrictById(command.getCommunityId());
    if (!UserUtil.isMember(user, community)) throw new BadRequestException(String.format("User is not a member of community. communityId=%d", community.getId()));

    userRepository.lockForUpdate(user);
    user.getCommunityUserList().forEach(cu -> {
      if (cu.getCommunity().getId().equals(community.getId())) {
        cu.setNotificationLastViewTime(Instant.now());
      }
    });
    
    userRepository.update(user);
  }
  
  public String getQrCodeUrl(User user, Long communityId, BigDecimal amount) {
    communityRepository.findPublicStrictById(communityId);
    String cryptoUserId = cryptoService.encryptoWithAES(String.valueOf(user.getId()));
    
    File imageFile = null;
    try {
      if (amount == null) {
        imageFile = qrCodeService.getTransactionQrCode(cryptoUserId, communityId);
      } else {
        imageFile = qrCodeService.getTransactionQrCode(cryptoUserId, communityId, amount);
      }

      return imageUploadService.create(imageFile, config.s3QrPrefix());
    } finally {
      if (imageFile != null) imageFile.delete();
    }
  }

  public List<UserTokenBalanceView> balanceViewList(User user) {
    List<UserTokenBalanceView> list = new ArrayList<>();
    user.getCommunityUserList().stream().map(CommunityUser::getCommunity).forEach(c -> {
      TokenBalance tokenBalance = blockchainService.getTokenBalance(user, c.getId());
      list.add(TransactionUtil.userTokenBalanceView(tokenBalance));
    });
    return list;
  }
}

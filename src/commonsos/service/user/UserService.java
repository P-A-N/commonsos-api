package commonsos.service.user;

import static java.util.stream.Collectors.toList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.validator.routines.EmailValidator;
import org.web3j.crypto.Credentials;

import commonsos.AuthenticationException;
import commonsos.DisplayableException;
import commonsos.ForbiddenException;
import commonsos.JobService;
import commonsos.UserSession;
import commonsos.controller.auth.DelegateWalletTask;
import commonsos.exception.BadRequestException;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.message.MessageThreadRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.ImageService;
import commonsos.service.auth.ProvisionalAccountCreateCommand;
import commonsos.service.auth.PasswordService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.community.CommunityView;
import commonsos.service.transaction.BalanceView;
import commonsos.service.transaction.TransactionService;
import commonsos.util.CommunityUtil;
import commonsos.util.UserUtil;

@Singleton
public class UserService {
  public static final String WALLET_PASSWORD = "test";

  @Inject UserRepository userRepository;
  @Inject CommunityRepository communityRepository;
  @Inject MessageThreadRepository messageThreadRepository;
  @Inject AdRepository adRepository;
  @Inject UserUtil userUtil;
  @Inject CommunityUtil communityUtil;
  @Inject BlockchainService blockchainService;
  @Inject PasswordService passwordService;
  @Inject TransactionService transactionService;
  @Inject ImageService imageService;
  @Inject JobService jobService;


  public User checkPassword(String username, String password) {
    User user = userRepository.findByUsername(username).orElseThrow(AuthenticationException::new);
    if (!passwordService.passwordMatchesHash(password, user.getPasswordHash())) throw new AuthenticationException();
    return user;
  }

  public UserPrivateView privateView(User user) {
    List<BalanceView> balanceList = new ArrayList<>();
    List<CommunityView> communityList = new ArrayList<>();
    if (user.getJoinedCommunities() != null) {
      user.getJoinedCommunities().forEach(c -> {
        balanceList.add(transactionService.balance(user, c.getId()));
        communityList.add(communityUtil.view(c));
      });
    }
    
    return userUtil.privateView(user, balanceList, communityList);
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("user not found"));
    boolean isAdmin = user.getJoinedCommunities().stream().anyMatch(c -> {
      return c.getAdminUser() != null && c.getAdminUser().getId().equals(currentUser.getId());
    });
    if (!currentUser.getId().equals(user.getId()) && !isAdmin) throw new ForbiddenException();
    
    // TODO filter balance
    return privateView(user);
  }

  public User create(ProvisionalAccountCreateCommand command) {
    validate(command);
    if (!blockchainService.isConnected()) throw new RuntimeException("Cannot create user, technical error with blockchain");
    if (userRepository.findByUsername(command.getUsername()).isPresent()) throw new DisplayableException("error.usernameTaken");
    List<Community> communityList = new ArrayList<>();
    if (command.getCommunityList() != null && !command.getCommunityList().isEmpty()) {
      communityList = communityList(command.getCommunityList());
    }
    User user = new User()
      .setJoinedCommunities(communityList)
      .setUsername(command.getUsername())
      .setPasswordHash(passwordService.hash(command.getPassword()))
      .setFirstName(command.getFirstName())
      .setLastName(command.getLastName())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setEmailAddress(command.getEmailAddress());

    String wallet = blockchainService.createWallet(WALLET_PASSWORD);
    Credentials credentials = blockchainService.credentials(wallet, WALLET_PASSWORD);

    user.setWallet(wallet);
    user.setWalletAddress(credentials.getAddress());

    communityList.forEach(c -> {
      DelegateWalletTask task = new DelegateWalletTask(user, c);
      if (command.isWaitUntilCompleted())
        jobService.execute(task);
      else
        jobService.submit(user, task);
    });

    return userRepository.create(user);
  }

  private List<Community> communityList(List<Long> communityList) {
    List<Community> result = new ArrayList<>();
    communityList.forEach(id -> {
      result.add(communityRepository.findById(id).orElseThrow(BadRequestException::new));
    });
    return result;
  }

  void validate(ProvisionalAccountCreateCommand command) {
    if (command.getUsername() == null || command.getUsername().length() < 4) throw new BadRequestException("invalid username");
    if (command.getPassword() == null || command.getPassword().length() < 8) throw new BadRequestException("invalid password");
//    if (command.getFirstName() == null || command.getFirstName().length() < 1) throw new BadRequestException("invalid first name");
//    if (command.getLastName() == null || command.getLastName().length() < 1) throw new BadRequestException("invalid last name");
    if (command.getEmailAddress() == null || !validateEmailAddress(command.getEmailAddress())) throw new BadRequestException("invalid email address");
  }

  boolean validateEmailAddress(String emailAddress) {
    return EmailValidator.getInstance().isValid(emailAddress);
  }
  
  public UserView view(Long id) {
    return view(userRepository.findById(id).orElseThrow(BadRequestException::new));
  }

  public User user(Long id) {
    return userRepository.findById(id).orElseThrow(BadRequestException::new);
  }

  public UserView view(User user) {
    return userUtil.view(user);
  }

  public List<UserView> searchUsers(User user, Long communityId, String query) {
    return userRepository.search(communityId, query).stream().filter(u -> !u.getId().equals(user.getId())).map(this::view).collect(toList());
  }

  public String updateAvatar(User user, InputStream image) {
    String url = imageService.create(image);
    if (user.getAvatarUrl() != null) {
      imageService.delete(user.getAvatarUrl());
    }
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

  public User deleteUserLogically(User user) {
    // delete my ads logically
    List<Ad> myAds = adRepository.myAds(
        user.getJoinedCommunities().stream().map(Community::getId).collect(Collectors.toList()), user.getId());
    myAds.forEach(ad -> {
      ad.setDeleted(true);
      adRepository.update(ad);
    });

    // delete message thread party
    messageThreadRepository.deleteMessageThreadParty(user);
    
    // delete user logically
    user.setDeleted(true);
    return userRepository.update(user);
  }

  public void updateMobileDevice(User user, MobileDeviceUpdateCommand command) {
    user.setPushNotificationToken(command.getPushNotificationToken());
    userRepository.update(user);
  }
}

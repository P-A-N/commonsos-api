package commonsos.service.user;

import static java.util.stream.Collectors.toList;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.validator.routines.EmailValidator;
import org.web3j.crypto.Credentials;

import commonsos.AuthenticationException;
import commonsos.BadRequestException;
import commonsos.DisplayableException;
import commonsos.JobService;
import commonsos.UserSession;
import commonsos.controller.auth.DelegateWalletTask;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.message.MessageThreadRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.ImageService;
import commonsos.service.auth.AccountCreateCommand;
import commonsos.service.auth.PasswordService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.view.UserPrivateView;
import commonsos.service.view.UserView;
import commonsos.service.view.UserViewService;

@Singleton
public class UserService {
  public static final String WALLET_PASSWORD = "test";

  @Inject UserViewService userViewService;
  @Inject UserRepository repository;
  @Inject CommunityRepository communityRepository;
  @Inject MessageThreadRepository messageThreadRepository;
  @Inject AdRepository adRepository;
  @Inject BlockchainService blockchainService;
  @Inject PasswordService passwordService;
  @Inject ImageService imageService;
  @Inject JobService jobService;


  public User checkPassword(String username, String password) {
    User user = repository.findByUsername(username).orElseThrow(AuthenticationException::new);
    if (!passwordService.passwordMatchesHash(password, user.getPasswordHash())) throw new AuthenticationException();
    return user;
  }

  public UserPrivateView privateView(User user) {
    return userViewService.privateView(user);
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    return userViewService.privateView(currentUser, userId);
  }

  public String fullName(User user) {
    return userViewService.fullName(user);
  }

  public User create(AccountCreateCommand command) {
    validate(command);
    if (!blockchainService.isConnected()) throw new RuntimeException("Cannot create user, technical error with blockchain");
    if (repository.findByUsername(command.getUsername()).isPresent()) throw new DisplayableException("error.usernameTaken");
    Community community = null;
    if (command.getCommunityId() != null) {
      community = community(command);
    }
    User user = new User()
      .setCommunityId(command.getCommunityId())
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

    if (community != null) {
      DelegateWalletTask task = new DelegateWalletTask(user, community.getAdminUser());
      if (command.isWaitUntilCompleted())
        jobService.execute(task);
      else
        jobService.submit(user, task);
    }

    return repository.create(user);
  }

  private Community community(AccountCreateCommand command) {
    return communityRepository.findById(command.getCommunityId()).orElseThrow(BadRequestException::new);
  }

  void validate(AccountCreateCommand command) {
    if (command.getUsername() == null || command.getUsername().length() < 4) throw new BadRequestException("invalid username");
    if (command.getPassword() == null || command.getPassword().length() < 8) throw new BadRequestException("invalid password");
    if (command.getFirstName() == null || command.getFirstName().length() < 1) throw new BadRequestException("invalid first name");
    if (command.getLastName() == null || command.getLastName().length() < 1) throw new BadRequestException("invalid last name");
    if (command.getEmailAddress() == null || !validateEmailAddress(command.getEmailAddress())) throw new BadRequestException("invalid email address");
  }

  boolean validateEmailAddress(String emailAddress) {
    return EmailValidator.getInstance().isValid(emailAddress);
  }
  
  public UserView view(Long id) {
    return userViewService.view(id);
  }

  public User user(Long id) {
    return repository.findById(id).orElseThrow(BadRequestException::new);
  }

  public UserView view(User user) {
    return userViewService.view(user);
  }

  public List<UserView> searchUsers(User user, Long communityId, String query) {
    return repository.search(communityId, query).stream().filter(u -> !u.getId().equals(user.getId())).map(this::view).collect(toList());
  }

  public String updateAvatar(User user, InputStream image) {
    String url = imageService.create(image);
    if (user.getAvatarUrl() != null) {
      imageService.delete(user.getAvatarUrl());
    }
    user.setAvatarUrl(url);
    repository.update(user);
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
    return repository.update(user);
  }

  public User deleteUserLogically(User user) {
    // delete my ads logically
    List<Ad> myAds = adRepository.myAds(user.getCommunityId(), user.getId());
    myAds.forEach(ad -> {
      ad.setDeleted(true);
      adRepository.update(ad);
    });

    // delete message thread party
    messageThreadRepository.deleteMessageThreadParty(user);
    
    // delete user logically
    user.setDeleted(true);
    return repository.update(user);
  }

  public void updateMobileDevice(User user, MobileDeviceUpdateCommand command) {
    user.setPushNotificationToken(command.getPushNotificationToken());
    repository.update(user);
  }
}

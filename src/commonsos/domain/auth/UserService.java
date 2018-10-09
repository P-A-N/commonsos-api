package commonsos.domain.auth;

import static java.util.stream.Collectors.toList;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.validator.routines.EmailValidator;
import org.web3j.crypto.Credentials;

import commonsos.AuthenticationException;
import commonsos.BadRequestException;
import commonsos.DisplayableException;
import commonsos.ForbiddenException;
import commonsos.JobService;
import commonsos.UserSession;
import commonsos.controller.auth.DelegateWalletTask;
import commonsos.domain.ad.Ad;
import commonsos.domain.ad.AdService;
import commonsos.domain.blockchain.BlockchainService;
import commonsos.domain.community.Community;
import commonsos.domain.community.CommunityService;
import commonsos.domain.message.MessageService;
import commonsos.domain.transaction.TransactionService;

@Singleton
public class UserService {
  public static final String WALLET_PASSWORD = "test";

  @Inject UserRepository repository;
  @Inject AdService adService;
  @Inject MessageService messageService;
  @Inject BlockchainService blockchainService;
  @Inject TransactionService transactionService;
  @Inject PasswordService passwordService;
  @Inject CommunityService communityService;
  @Inject ImageService imageService;
  @Inject JobService jobService;


  public User checkPassword(String username, String password) {
    User user = repository.findByUsername(username).orElseThrow(AuthenticationException::new);
    if (!passwordService.passwordMatchesHash(password, user.getPasswordHash())) throw new AuthenticationException();
    return user;
  }

  public UserPrivateView privateView(User user) {
    BigDecimal balance = transactionService.balance(user);
    return new UserPrivateView()
      .setId(user.getId())
      .setAdmin(user.isAdmin())
      .setBalance(balance)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress());
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    if (!currentUser.getId().equals(userId) && !currentUser.isAdmin()) throw new ForbiddenException();
    User user = repository.findById(userId).orElseThrow(BadRequestException::new);
    return privateView(user);
  }

  public String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
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
      User admin = walletUser(community);
      DelegateWalletTask task = new DelegateWalletTask(user, admin);
      if (command.isWaitUntilCompleted())
        jobService.execute(task);
      else
        jobService.submit(user, task);
    }

    return repository.create(user);
  }

  public User walletUser(Community community) {
    return repository.findAdminByCommunityId(community.getId());
  }

  private Community community(AccountCreateCommand command) {
    return communityService.community(command.getCommunityId());
  }

  void validate(AccountCreateCommand command) {
    if (command.getUsername() == null || command.getUsername().length() < 4) throw new BadRequestException();
    if (command.getPassword() == null || command.getPassword().length() < 8) throw new BadRequestException();
    if (command.getFirstName() == null || command.getFirstName().length() < 1) throw new BadRequestException();
    if (command.getLastName() == null || command.getLastName().length() < 1) throw new BadRequestException();
    if (command.getEmailAddress() == null || !validateEmailAddress(command.getEmailAddress())) throw new BadRequestException();
  }

  boolean validateEmailAddress(String emailAddress) {
    return EmailValidator.getInstance().isValid(emailAddress);
  }
  
  public UserView view(Long id) {
    return view(user(id));
  }

  public User user(Long id) {
    return repository.findById(id).orElseThrow(BadRequestException::new);
  }

  public UserView view(User user) {
    return new UserView()
      .setId(user.getId())
      .setFullName(fullName(user))
      .setUsername(user.getUsername())
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl());
  }

  public List<UserView> searchUsers(User user, String query) {
    return repository.search(user.getCommunityId(), query).stream().filter(u -> !u.getId().equals(user.getId())).map(this::view).collect(toList());
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
    repository.update(user);
    return user;
  }

  public User deleteUserLogically(User user) {
    // delete my ads logically
    List<Ad> myAds = adService.myAds(user);
    myAds.forEach(ad -> adService.deleteAdLogically(ad, user));

    // delete message thread party
    messageService.deleteMessageThreadParty(user);
    
    // delete user logically
    user.setDeleted(true);
    repository.update(user);
    
    return user;
  }

  public void updateMobileDevice(User user, MobileDeviceUpdateCommand command) {
    user.setPushNotificationToken(command.getPushNotificationToken());
    repository.update(user);
  }
}

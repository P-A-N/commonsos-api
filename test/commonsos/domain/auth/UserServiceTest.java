package commonsos.domain.auth;

import static commonsos.TestId.id;
import static commonsos.domain.auth.UserService.WALLET_PASSWORD;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock UserRepository repository;
  @Mock AdService adService;
  @Mock MessageService messageService;
  @Mock TransactionService transactionService;
  @Mock PasswordService passwordService;
  @Mock BlockchainService blockchainService;
  @Mock CommunityService communityService;
  @Mock ImageService imageService;
  @Mock JobService jobService;
  @InjectMocks @Spy UserService userService;

  @Test
  public void checkPassword_withValidUser() {
    // prepare
    User user = new User().setPasswordHash("hash");
    when(repository.findByUsername("worker")).thenReturn(Optional.of(user));
    when(passwordService.passwordMatchesHash("valid password", "hash")).thenReturn(true);

    // execute
    User result = userService.checkPassword("worker", "valid password");
    
    // verify
    verify(repository, times(1)).findByUsername("worker");
    verify(passwordService, times(1)).passwordMatchesHash("valid password", "hash");
    assertThat(result).isEqualTo(user);
  }

  @Test(expected = AuthenticationException.class)
  public void checkPassword_withInvalidUsername() {
    // prepare
    when(repository.findByUsername("invalid")).thenReturn(Optional.empty());

    // execute
    userService.checkPassword("invalid", "secret");
  }

  @Test(expected = AuthenticationException.class)
  public void checkPassword_withInvalidPassword() {
    // prepare
    User user = new User().setPasswordHash("hash");
    when(repository.findByUsername("user")).thenReturn(Optional.of(user));
    when(passwordService.passwordMatchesHash("wrong password", "hash")).thenReturn(false);

    // execute
    userService.checkPassword("user", "wrong password");
  }

  @Test
  public void privateView() {
    // prepare
    User user = new User()
        .setId(id("user id"))
        .setAdmin(true)
        .setFirstName("first")
        .setLastName("last")
        .setUsername("user")
        .setLocation("Shibuya")
        .setDescription("description")
        .setAvatarUrl("/avatar.png")
        .setEmailAddress("test@test.com");
    when(transactionService.balance(user)).thenReturn(BigDecimal.TEN);

    // execute
    UserPrivateView view = userService.privateView(user);

    // verify
    verify(transactionService, times(1)).balance(user);
    assertThat(view.getId()).isEqualTo(id("user id"));
    assertThat(view.isAdmin()).isTrue();
    assertThat(view.getBalance()).isEqualTo(BigDecimal.TEN);
    assertThat(view.getFullName()).isEqualTo("last first");
    assertThat(view.getFirstName()).isEqualTo("first");
    assertThat(view.getLastName()).isEqualTo("last");
    assertThat(view.getUsername()).isEqualTo("user");
    assertThat(view.getLocation()).isEqualTo("Shibuya");
    assertThat(view.getDescription()).isEqualTo("description");
    assertThat(view.getAvatarUrl()).isEqualTo("/avatar.png");
    assertThat(view.getEmailAddress()).isEqualTo("test@test.com");
  }

  @Test
  public void privateView_adminAccessesOtherUser() {
    // prepare
    User user = new User();
    when(repository.findById(any())).thenReturn(Optional.of(user));
    UserPrivateView userView = new UserPrivateView();
    doReturn(userView).when(userService).privateView(any());
    
    when(userService.privateView(any())).thenReturn(userView);
    
    // execute
    User currentUser = new User()
        .setId(id("currentUser"))
        .setAdmin(true);
    Long userId = id("otherUSer");
    UserPrivateView result = userService.privateView(currentUser, userId);
    
    // verify
    verify(repository, times(1)).findById(userId);
    assertThat(result).isSameAs(userView);
  }

  @Test
  public void privateView_notAdminAccessOwnUser() {
    // prepare
    User user = new User();
    when(repository.findById(any())).thenReturn(Optional.of(user));
    UserPrivateView userView = new UserPrivateView();
    doReturn(userView).when(userService).privateView(any());
    
    // execute
    User currentUser = new User()
        .setId(id("currentUser"))
        .setAdmin(false);
    Long userId = id("currentUser");
    UserPrivateView result = userService.privateView(currentUser, userId);
    
    // verify
    verify(repository, times(1)).findById(userId);
    assertThat(result).isSameAs(userView);
  }

  @Test(expected = ForbiddenException.class)
  public void privateView_notAdminAccessOtherUser() {
    // execute
    User currentUser = new User()
        .setId(id("currentUser"))
        .setAdmin(false);
    Long userId = id("otherUser");
    userService.privateView(currentUser, userId);
  }

  @Test(expected = BadRequestException.class)
  public void privateView_userNotFound() {
    // prepare
    when(repository.findById(any())).thenReturn(Optional.empty());
    
    // execute
    User currentUser = new User()
        .setId(id("currentUser"))
        .setAdmin(false);
    Long userId = id("currentUser");
    userService.privateView(currentUser, userId);
  }

  @Test
  public void view() {
    // execute
    User user = new User()
        .setId(id("user"))
        .setFirstName("first")
        .setLastName("last")
        .setUsername("user")
        .setLocation("location")
        .setDescription("description")
        .setAvatarUrl("avatar url");
    UserView result = userService.view(user);
    
    // verify
    assertThat(result.getId()).isEqualTo(id("user"));
    assertThat(result.getFullName()).isEqualTo("last first");
    assertThat(result.getUsername()).isEqualTo("user");
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getLocation()).isEqualTo("location");
    assertThat(result.getAvatarUrl()).isEqualTo("avatar url");
  }

  @Test
  public void view_byId() {
    // prepare
    User user = new User();
    when(repository.findById(any())).thenReturn(Optional.of(user));
    UserView userView = new UserView();
    doReturn(userView).when(userService).view(user);

    // execute
    UserView result = userService.view(123L);

    // verify
    verify(repository, times(1)).findById(123L);
    assertThat(result).isEqualTo(userView);
  }

  @Test(expected = BadRequestException.class)
  public void view_byId_userNotFound() {
    // prepare
    when(repository.findById(any())).thenReturn(Optional.empty());

    // execute
    userService.view(123L);
  }

  @Test
  public void create() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.empty());
    Community community = new Community().setId(id("community"));
    when(communityService.community(id("community"))).thenReturn(community);
    when(passwordService.hash(any())).thenReturn("hash");
    when(blockchainService.createWallet(WALLET_PASSWORD)).thenReturn("wallet");
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);
    User communityAdmin = new User().setAdmin(true);
    when(repository.findAdminByCommunityId(id("community"))).thenReturn(communityAdmin);
    User createdUser = new User();
    when(repository.create(any())).thenReturn(createdUser);

    // execute
    AccountCreateCommand command = new AccountCreateCommand()
        .setUsername("user name")
        .setPassword("secret78")
        .setFirstName("first")
        .setLastName("last")
        .setDescription("description")
        .setLocation("Shibuya")
        .setCommunityId(id("community"))
        .setEmailAddress("test@test.com")
        .setWaitUntilCompleted(false);
    User result = userService.create(command);

    // verify
    verify(userService, times(1)).validate(command);
    verify(blockchainService, times(1)).isConnected();
    verify(repository, times(1)).findByUsername("user name");
    verify(communityService, times(1)).community(id("community"));
    verify(passwordService, times(1)).hash("secret78");
    verify(blockchainService, times(1)).createWallet(WALLET_PASSWORD);
    verify(blockchainService, times(1)).credentials("wallet", WALLET_PASSWORD);
    verify(repository, times(1)).findAdminByCommunityId(id("community"));
    
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(repository, times(1)).create(userCaptor.capture());
    User actualUser = userCaptor.getValue();
    assertThat(actualUser.getId()).isNull();
    assertThat(actualUser.getCommunityId()).isEqualTo(id("community"));
    assertThat(actualUser.isAdmin()).isEqualTo(false);
    assertThat(actualUser.getUsername()).isEqualTo(command.getUsername());
    assertThat(actualUser.getPasswordHash()).isEqualTo("hash");
    assertThat(actualUser.getFirstName()).isEqualTo(command.getFirstName());
    assertThat(actualUser.getLastName()).isEqualTo(command.getLastName());
    assertThat(actualUser.getDescription()).isEqualTo(command.getDescription());
    assertThat(actualUser.getLocation()).isEqualTo(command.getLocation());
    assertThat(actualUser.getAvatarUrl()).isEqualTo(null);
    assertThat(actualUser.getWallet()).isEqualTo("wallet");
    assertThat(actualUser.getWalletAddress()).isEqualTo("wallet address");
    assertThat(actualUser.getPushNotificationToken()).isEqualTo(null);
    assertThat(actualUser.getEmailAddress()).isEqualTo(command.getEmailAddress());
    
    DelegateWalletTask task = new DelegateWalletTask(actualUser, communityAdmin);
    verify(jobService, times(1)).submit(actualUser, task);
    verify(jobService, never()).execute(task);
    
    assertThat(result).isEqualTo(createdUser);
  }

  public void validate() {
    userService.validate(validCommand());
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_null() {
    userService.validate(validCommand().setUsername(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length() {
    userService.validate(validCommand().setUsername("123"));
  }

  @Test(expected = BadRequestException.class)
  public void validate_password_null() {
    userService.validate(validCommand().setPassword(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_password_less_length() {
    userService.validate(validCommand().setPassword("1234567"));
  }

  @Test(expected = BadRequestException.class)
  public void validate_firstName_null() {
    userService.validate(validCommand().setFirstName(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_firstName_less_length() {
    userService.validate(validCommand().setFirstName(""));
  }

  @Test(expected = BadRequestException.class)
  public void validate_lastName_null() {
    userService.validate(validCommand().setLastName(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_lastName_less_length() {
    userService.validate(validCommand().setLastName(""));
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_null() {
    userService.validate(validCommand().setEmailAddress(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid() {
    userService.validate(validCommand().setEmailAddress(""));
    userService.validate(validCommand().setEmailAddress("aaa"));
    userService.validate(validCommand().setEmailAddress("a.@test.com"));
    userService.validate(validCommand().setEmailAddress("a<b@test.com"));
    userService.validate(validCommand().setEmailAddress("a>b@test.com"));
    userService.validate(validCommand().setEmailAddress("a@test<com"));
    userService.validate(validCommand().setEmailAddress("a@test>com"));
    userService.validate(validCommand().setEmailAddress("a@a@a.com"));
  }

  @Test
  public void validate_emailAddress_valid() {
    userService.validate(validCommand().setEmailAddress("test@test.com"));
    userService.validate(validCommand().setEmailAddress("a.b.c@test.com"));
    userService.validate(validCommand().setEmailAddress("a@a.b.c.com"));
  }

  @Test
  public void create_execute_task_when_waitUntilCompleted_is_true() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.empty());
    when(communityService.community(any())).thenReturn(mock(Community.class));
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));

    // execute
    AccountCreateCommand command = new AccountCreateCommand()
        .setCommunityId(id("community"))
        .setWaitUntilCompleted(true);
    userService.create(command);

    // verify
    verify(jobService, times(1)).execute(any());
    verify(jobService, never()).submit(any(), any());
  }

  @Test
  public void create_submit_task_when_waitUntilCompleted_is_false() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.empty());
    when(communityService.community(any())).thenReturn(mock(Community.class));
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));

    // execute
    AccountCreateCommand command = new AccountCreateCommand()
        .setCommunityId(id("community"))
        .setWaitUntilCompleted(false);
    userService.create(command);

    // verify
    verify(jobService, never()).execute(any());
    verify(jobService, times(1)).submit(any(), any());
  }

  @Test
  public void create_failFastIfBlockchainIsDown() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(false);
    
    // execute
    AccountCreateCommand command = new AccountCreateCommand();
    RuntimeException thrown = catchThrowableOfType(() -> userService.create(command), RuntimeException.class);

    // verify
    assertThat(thrown).hasMessage("Cannot create user, technical error with blockchain");
  }

  @Test
  public void create_usernameAlreadyTaken() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.of(new User()));

    // execute
    AccountCreateCommand command = new AccountCreateCommand();
    DisplayableException thrown = catchThrowableOfType(() -> userService.create(command), DisplayableException.class);

    // verify
    assertThat(thrown).hasMessage("error.usernameTaken");
  }

  @Test
  public void create_communityIsOptional() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.empty());
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));
    
    // execute
    AccountCreateCommand command = new AccountCreateCommand()
        .setUsername("user name")
        .setPassword("secret78")
        .setFirstName("first")
        .setLastName("last")
        .setEmailAddress("test@test.com")
        .setCommunityId(null);
    userService.create(command);

    // verify
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(repository, times(1)).create(userCaptor.capture());
    User actualUser = userCaptor.getValue();
    
    assertThat(actualUser.getCommunityId()).isNull();
  }

  @Test
  public void viewByUserId() {
    User user = new User();
    when(repository.findById(id("user id"))).thenReturn(Optional.of(user));
    UserView view = new UserView();
    doReturn(view).when(userService).view(user);

    assertThat(userService.view(id("user id"))).isEqualTo(view);
  }

  @Test(expected = BadRequestException.class)
  public void viewByUserId_userNotFound() {
    when(repository.findById(id("invalid id"))).thenReturn(Optional.empty());

    userService.view(id("invalid id"));
  }

  @Test
  public void searchUsers() {
    User user = new User().setId(1L);
    when(repository.search(user.getCommunityId(), "foobar")).thenReturn(asList(user));
    UserView userView = new UserView();
    when(userService.view(user)).thenReturn(userView);

    List<UserView> users = userService.searchUsers(new User().setId(2L), "foobar");

    assertThat(users).isEqualTo(asList(userView));
  }

  @Test
  public void searchUsers_excludesSearchingUser() {
    User myself = new User().setId(id("myself"));
    User other = new User().setId(id("other"));
    when(repository.search(myself.getCommunityId(), "foobar")).thenReturn(asList(myself, other));
    UserView userView = new UserView();
    when(userService.view(other)).thenReturn(userView);

    List<UserView> users = userService.searchUsers(myself, "foobar");

    assertThat(users).isEqualTo(asList(userView));
  }

  @Test
  public void walletUser() {
    User admin = new User();
    when(repository.findAdminByCommunityId(id("community"))).thenReturn(admin);

    User result = userService.walletUser(new Community().setId(id("community")));

    assertThat(result).isEqualTo(admin);
  }

  @Test
  public void updateAvatar() {
    User user = new User().setAvatarUrl("/old");
    ByteArrayInputStream image = new ByteArrayInputStream(new byte[] {1, 2, 3});
    when(imageService.create(image)).thenReturn("/url");

    String result = userService.updateAvatar(user, image);

    assertThat(result).isEqualTo("/url");
    assertThat(user.getAvatarUrl()).isEqualTo("/url");
    verify(repository).update(user);
    verify(imageService).delete("/old");
  }

  @Test
  public void updateAvatar_userHasNoAvatarYet() {
    User user = new User().setAvatarUrl(null);
    ByteArrayInputStream image = new ByteArrayInputStream(new byte[] {1, 2, 3});
    when(imageService.create(image)).thenReturn("/url");

    String result = userService.updateAvatar(user, image);

    assertThat(result).isEqualTo("/url");
    assertThat(user.getAvatarUrl()).isEqualTo("/url");
    verify(repository).update(user);
    verify(imageService, never()).delete(any());
  }

  @Test
  public void session() {
    // prepare
    User user = new User().setId(id("user id")).setUsername("user name");

    // execute
    UserSession result = userService.session(user);
    
    // verify
    UserSession expected = new UserSession().setUserId(id("user id")).setUsername("user name");
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void updateUser() {
    User user = new User();
    when(repository.update(user)).thenReturn(user);
    
    // execute
    UserUpdateCommand command = new UserUpdateCommand()
        .setFirstName("first")
        .setLastName("last")
        .setDescription("description")
        .setLocation("location");
    User result = userService.updateUser(user, command);
    
    // verify
    verify(repository, times(1)).update(user);
    assertThat(user.getFirstName()).isEqualTo(command.getFirstName());
    assertThat(user.getLastName()).isEqualTo(command.getLastName());
    assertThat(user.getDescription()).isEqualTo(command.getDescription());
    assertThat(user.getLocation()).isEqualTo(command.getLocation());
    
    assertThat(result).isEqualTo(user);
  }

  @Test
  public void deleteUserLogically() {
    // prepare
    User targetUser = new User();
    
    List<Ad> myAds = new ArrayList<>();
    myAds.addAll(Arrays.asList(new Ad(), new Ad()));
    when(adService.myAds(targetUser)).thenReturn(myAds);
    
    when(repository.update(targetUser)).thenReturn(targetUser);
    
    // execute
    User result = userService.deleteUserLogically(targetUser);
    
    // verify
    assertThat(result).isEqualTo(targetUser);
    assertThat(result.isDeleted()).isEqualTo(true);
    
    verify(adService, times(2)).deleteAdLogically(any(Ad.class), any());
    verify(messageService, times(1)).deleteMessageThreadParty(targetUser);
  }

  @Test
  public void deleteUserLogically_noAds() {
    // prepare
    List<Ad> myAds = new ArrayList<>();
    when(adService.myAds(any())).thenReturn(myAds);
    
    // execute
    userService.deleteUserLogically(new User());
    
    // verify
    verify(adService, never()).deleteAdLogically(any(Ad.class), any());
  }
  
  @Test
  public void updateMobileDevice() {
    MobileDeviceUpdateCommand command = new MobileDeviceUpdateCommand().setPushNotificationToken("12345");

    userService.updateMobileDevice(new User(), command);

    verify(repository).update(new User().setPushNotificationToken("12345"));
  }

  private AccountCreateCommand validCommand() {
    return new AccountCreateCommand()
        .setUsername("1234")
        .setPassword("12345678")
        .setFirstName("1")
        .setLastName("1")
        .setLastName("test@test.com");
  }
}
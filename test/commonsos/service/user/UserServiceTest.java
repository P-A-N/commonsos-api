package commonsos.service.user;

import static commonsos.TestId.id;
import static commonsos.service.user.UserService.WALLET_PASSWORD;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
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
import commonsos.service.transaction.TransactionService;
import commonsos.service.view.UserView;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock UserRepository repository;
  @Mock CommunityRepository communityRepository;
  @Mock AdRepository adRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock TransactionService transactionService;
  @Mock PasswordService passwordService;
  @Mock BlockchainService blockchainService;
  @Mock ImageService imageService;
  @Mock JobService jobService;
  @InjectMocks @Spy UserService userService;
  @Captor ArgumentCaptor<AccountCreateCommand> accountCreatecommandCaptor;
  @Captor ArgumentCaptor<User> userCaptor;
  @Captor ArgumentCaptor<Ad> adCaptor;

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
  public void create() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(true);
    when(repository.findByUsername(any())).thenReturn(Optional.empty());
    User admin = new User().setId(id("admin")).setUsername("admin");
    Community community = new Community().setId(id("community")).setAdminUser(admin);
    when(communityRepository.findById(id("community"))).thenReturn(Optional.of(community));
    when(passwordService.hash("secret78")).thenReturn("hash");
    when(blockchainService.createWallet(WALLET_PASSWORD)).thenReturn("wallet");
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials("wallet", WALLET_PASSWORD)).thenReturn(credentials);
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
    verify(communityRepository, times(1)).findById(id("community"));
    verify(passwordService, times(1)).hash("secret78");
    verify(blockchainService, times(1)).createWallet(WALLET_PASSWORD);
    verify(blockchainService, times(1)).credentials("wallet", WALLET_PASSWORD);
    
    verify(repository, times(1)).create(userCaptor.capture());
    User actualUser = userCaptor.getValue();
    assertThat(actualUser.getId()).isNull();
    assertThat(actualUser.getCommunityId()).isEqualTo(id("community"));
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
    
    DelegateWalletTask task = new DelegateWalletTask(actualUser, admin);
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
    when(communityRepository.findById(any())).thenReturn(Optional.of(new Community()));
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
    when(communityRepository.findById(any())).thenReturn(Optional.of(new Community()));
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
  public void searchUsers() {
    User myself = new User().setId(id("myself"));
    User other = new User().setId(id("other"));
    when(repository.search(id("community"), "foobar")).thenReturn(asList(myself, other));
    UserView userView = new UserView();
    doReturn(userView).when(userService).view(other);

    List<UserView> users = userService.searchUsers(myself, id("community"), "foobar");

    assertThat(users).isEqualTo(asList(userView));
  }

  /*@Test
  public void walletUser() {
    User admin = new User();
    when(repository.findAdminByCommunityId(id("community"))).thenReturn(admin);

    User result = userService.walletUser(new Community().setId(id("community")));

    assertThat(result).isEqualTo(admin);
  }*/

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
    User targetUser = new User().setId(id("user")).setCommunityId(id("community"));
    
    List<Ad> myAds = new ArrayList<>();
    myAds.addAll(Arrays.asList(new Ad(), new Ad()));
    when(adRepository.myAds(id("community"), id("user"))).thenReturn(myAds);
    
    when(repository.update(targetUser)).thenReturn(targetUser);
    
    // execute
    User result = userService.deleteUserLogically(targetUser);
    
    // verify
    assertThat(result).isEqualTo(targetUser);
    assertThat(result.isDeleted()).isEqualTo(true);
    
    verify(adRepository, times(2)).update(adCaptor.capture());
    List<Ad> actualAdList = adCaptor.getAllValues();
    assertThat(actualAdList.size()).isEqualTo(2);
    assertThat(actualAdList.get(0).isDeleted()).isTrue();
    assertThat(actualAdList.get(1).isDeleted()).isTrue();
    
    verify(messageThreadRepository, times(1)).deleteMessageThreadParty(targetUser);
  }

  @Test
  public void deleteUserLogically_noAds() {
    // prepare
    List<Ad> myAds = new ArrayList<>();
    when(adRepository.myAds(any(), any())).thenReturn(myAds);
    
    // execute
    userService.deleteUserLogically(new User());
    
    // verify
    verify(adRepository, never()).update(any(Ad.class));
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
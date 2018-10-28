package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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

import commonsos.JobService;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.command.CreateAccountTemporaryCommand;
import commonsos.service.command.MobileDeviceUpdateCommand;
import commonsos.service.command.UserUpdateCommand;
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageService;
import commonsos.session.UserSession;
import commonsos.view.UserView;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock AdRepository adRepository;
  @Mock BlockchainService blockchainService;
  @Mock CryptoService cryptoService;
  @Mock TransactionService transactionService;
  @Mock ImageService imageService;
  @Mock EmailService EmailService;
  @Mock JobService jobService;
  @InjectMocks @Spy UserService userService;
  @Captor ArgumentCaptor<CreateAccountTemporaryCommand> accountCreatecommandCaptor;
  @Captor ArgumentCaptor<User> userCaptor;
  @Captor ArgumentCaptor<Ad> adCaptor;

  @Test
  public void checkPassword_withValidUser() {
    // prepare
    User user = new User().setPasswordHash("hash");
    when(userRepository.findByUsername("worker")).thenReturn(Optional.of(user));
    when(cryptoService.checkPassword("valid password", "hash")).thenReturn(true);

    // execute
    User result = userService.checkPassword("worker", "valid password");
    
    // verify
    verify(userRepository, times(1)).findByUsername("worker");
    verify(cryptoService, times(1)).checkPassword("valid password", "hash");
    assertThat(result).isEqualTo(user);
  }

  @Test(expected = AuthenticationException.class)
  public void checkPassword_withInvalidUsername() {
    // prepare
    when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

    // execute
    userService.checkPassword("invalid", "secret");
  }

  @Test(expected = AuthenticationException.class)
  public void checkPassword_withInvalidPassword() {
    // prepare
    User user = new User().setPasswordHash("hash");
    when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
    when(cryptoService.checkPassword("wrong password", "hash")).thenReturn(false);

    // execute
    userService.checkPassword("user", "wrong password");
  }

  @Test
  public void create_noWait() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
    when(communityRepository.findStrictById(any())).thenReturn(new Community());
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);

    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand()
        .setCommunityList(asList(1L,2L))
        .setWaitUntilCompleted(true);
    userService.create(command);
    
    // verify
    verify(jobService, never()).submit(any(), any());
    verify(jobService, times(2)).execute(any());
  }

  @Test
  public void create_wait() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
    when(communityRepository.findStrictById(any())).thenReturn(new Community());
    Credentials credentials = mock(Credentials.class);
    when(credentials.getAddress()).thenReturn("wallet address");
    when(blockchainService.credentials(any(), any())).thenReturn(credentials);

    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand()
        .setCommunityList(asList(1L,2L))
        .setWaitUntilCompleted(false);
    userService.create(command);
    
    // verify
    verify(jobService, times(2)).submit(any(), any());
    verify(jobService, never()).execute(any());
  }

  @Test
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
  public void create_failFastIfBlockchainIsDown() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(false);
    
    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand();
    RuntimeException thrown = catchThrowableOfType(() -> userService.create(command), RuntimeException.class);

    // verify
    assertThat(thrown).hasMessage("Cannot create user, technical error with blockchain");
  }

  @Test
  public void create_usernameAlreadyTaken() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand();
    DisplayableException thrown = catchThrowableOfType(() -> userService.create(command), DisplayableException.class);

    // verify
    assertThat(thrown).hasMessage("error.usernameTaken");
  }

  @Test
  public void create_communityIsOptional() {
    // prepare
    doNothing().when(userService).validate(any());
    when(blockchainService.isConnected()).thenReturn(true);
    when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));
    
    // execute
    userService.create(new CreateAccountTemporaryCommand());

    // verify
    verify(jobService, never()).submit(any(), any());
    verify(jobService, never()).execute(any());
  }

  @Test
  public void searchUsers() {
    User myself = new User().setId(id("myself"));
    User other = new User().setId(id("other"));
    when(userRepository.search(id("community"), "foobar")).thenReturn(asList(myself, other));

    List<UserView> results = userService.searchUsers(myself, id("community"), "foobar");

    assertThat(results.size()).isEqualTo(1);
    assertThat(results.get(0).getId()).isEqualTo(id("other"));
  }

  @Test
  public void updateAvatar() {
    User user = new User().setAvatarUrl("/old");
    ByteArrayInputStream image = new ByteArrayInputStream(new byte[] {1, 2, 3});
    when(imageService.create(image)).thenReturn("/url");

    String result = userService.updateAvatar(user, image);

    assertThat(result).isEqualTo("/url");
    assertThat(user.getAvatarUrl()).isEqualTo("/url");
    verify(userRepository).update(user);
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
    verify(userRepository).update(user);
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
    when(userRepository.update(user)).thenReturn(user);
    
    // execute
    UserUpdateCommand command = new UserUpdateCommand()
        .setFirstName("first")
        .setLastName("last")
        .setDescription("description")
        .setLocation("location");
    User result = userService.updateUser(user, command);
    
    // verify
    verify(userRepository, times(1)).update(user);
    assertThat(user.getFirstName()).isEqualTo(command.getFirstName());
    assertThat(user.getLastName()).isEqualTo(command.getLastName());
    assertThat(user.getDescription()).isEqualTo(command.getDescription());
    assertThat(user.getLocation()).isEqualTo(command.getLocation());
    
    assertThat(result).isEqualTo(user);
  }

  @Test
  public void updateMobileDevice() {
    MobileDeviceUpdateCommand command = new MobileDeviceUpdateCommand().setPushNotificationToken("12345");

    userService.updateMobileDevice(new User(), command);

    verify(userRepository).update(new User().setPushNotificationToken("12345"));
  }

  private CreateAccountTemporaryCommand validCommand() {
    return new CreateAccountTemporaryCommand()
        .setUsername("1234")
        .setPassword("12345678")
        .setFirstName("1")
        .setLastName("1")
        .setEmailAddress("test@test.com");
  }
}
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
import commonsos.repository.entity.TemporaryUser;
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
  public void createAccountComplete_noWait() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(true);
    
    TemporaryUser tempUser = new TemporaryUser()
        .setWaitUntilCompleted(true)
        .setCommunityList(asList(new Community(), new Community()));
    when(userRepository.findStrictTemporaryUser(any())).thenReturn(tempUser);
    
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));

    // execute
    userService.createAccountComplete("accessId");
    
    // verify
    verify(jobService, never()).submit(any(), any());
    verify(jobService, times(2)).execute(any());
  }

  @Test
  public void createAccountComplete_wait() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(true);
    
    TemporaryUser tempUser = new TemporaryUser()
        .setWaitUntilCompleted(false)
        .setCommunityList(asList(new Community(), new Community()));
    when(userRepository.findStrictTemporaryUser(any())).thenReturn(tempUser);
    
    when(blockchainService.credentials(any(), any())).thenReturn(mock(Credentials.class));

    // execute
    userService.createAccountComplete("accessId");
    
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
  public void validate_username_less_length1() {
    userService.validate(validCommand().setUsername("123"));
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length2() {
    userService.validate(validCommand().setUsername("１２３"));
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length3() {
    userService.validate(validCommand().setUsername("🍺🍺🍺"));
  }

  @Test
  public void validate_password_valid() {
    userService.validatePassword("abcdefghijklmnopqrstuvwxyz!\"#$%&'()-=~^\\|@`[{;+:*]},<.>/?_");
  }

  @Test(expected = BadRequestException.class)
  public void validate_password_null() {
    userService.validate(validCommand().setPassword(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_password_less_length() {
    userService.validate(validCommand().setPassword("1234567"));
  }

  @Test(expected = DisplayableException.class)
  public void validate_password_unicode() {
    userService.validate(validCommand().setPassword("１２３４５６７８"));
  }

  @Test(expected = DisplayableException.class)
  public void validate_password_space() {
    userService.validate(validCommand().setPassword("1 2 3 4 5 6 7 8"));
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_null() {
    userService.validate(validCommand().setEmailAddress(null));
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid1() {
    userService.validate(validCommand().setEmailAddress(""));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid2() {
    userService.validate(validCommand().setEmailAddress("aaa"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid3() {
    userService.validate(validCommand().setEmailAddress("a.@test.com"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid4() {
    userService.validate(validCommand().setEmailAddress("a<b@test.com"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid5() {
    userService.validate(validCommand().setEmailAddress("a>b@test.com"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid6() {
    userService.validate(validCommand().setEmailAddress("a@test<com"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid7() {
    userService.validate(validCommand().setEmailAddress("a@test>com"));
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid8() {
    userService.validate(validCommand().setEmailAddress("a@a@a.com"));
  }

  @Test
  public void validate_emailAddress_valid() {
    userService.validate(validCommand().setEmailAddress("test@test.com"));
    userService.validate(validCommand().setEmailAddress("a.b.c@test.com"));
    userService.validate(validCommand().setEmailAddress("a@a.b.c.com"));
  }

  @Test
  public void validate_status_valid() {
    userService.validateStatus(null);
    userService.validateStatus("");
    userService.validateStatus("12345678901234567890123456789012345678901234567890"); // length = 50, ascii
    userService.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０"); // length = 50, utf-8
    userService.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺"); // length = 50, 4 byte unicode
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid1() {
    userService.validateStatus("123456789012345678901234567890123456789012345678901"); // length = 51, ascii
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid2() {
    userService.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１"); // length = 51, utf-8
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid3() {
    userService.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺"); // length = 51, 4 byte unicode
  }

  @Test
  public void createAccountComplete_failFastIfBlockchainIsDown() {
    // prepare
    when(blockchainService.isConnected()).thenReturn(false);
    
    // execute
    RuntimeException thrown = catchThrowableOfType(() -> userService.createAccountComplete("accessId"), RuntimeException.class);

    // verify
    assertThat(thrown).hasMessage("Cannot create user, technical error with blockchain");
  }

  @Test
  public void createAccountTemporary_usernameAlreadyTaken() {
    // prepare
    doNothing().when(userService).validate(any());
    when(userRepository.isUsernameTaken(any())).thenReturn(true);

    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand();
    DisplayableException thrown = catchThrowableOfType(() -> userService.createAccountTemporary(command), DisplayableException.class);

    // verify
    assertThat(thrown).hasMessage("error.usernameTaken");
  }

  @Test
  public void createAccountTemporary_emailAddressAlreadyTaken() {
    // prepare
    doNothing().when(userService).validate(any());
    when(userRepository.isUsernameTaken(any())).thenReturn(false);
    when(userRepository.isEmailAddressTaken(any())).thenReturn(true);

    // execute
    CreateAccountTemporaryCommand command = new CreateAccountTemporaryCommand();
    DisplayableException thrown = catchThrowableOfType(() -> userService.createAccountTemporary(command), DisplayableException.class);

    // verify
    assertThat(thrown).hasMessage("error.emailAddressTaken");
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
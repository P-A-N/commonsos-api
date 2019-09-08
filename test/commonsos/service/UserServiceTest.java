package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.Credentials;

import commonsos.JobService;
import commonsos.controller.command.app.CreateUserTemporaryCommand;
import commonsos.controller.command.app.UserUpdateCommand;
import commonsos.exception.AuthenticationException;
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
import commonsos.service.crypto.CryptoService;
import commonsos.service.email.EmailService;
import commonsos.service.image.ImageUploadService;
import commonsos.service.image.QrCodeService;
import commonsos.session.UserSession;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock AdRepository adRepository;
  @Mock BlockchainService blockchainService;
  @Mock CryptoService cryptoService;
  @Mock TokenTransactionService transactionService;
  @Mock ImageUploadService imageUploadService;
  @Mock QrCodeService qrCodeService;
  @Mock EmailService EmailService;
  @Mock JobService jobService;
  @InjectMocks @Spy UserService userService;
  @Captor ArgumentCaptor<CreateUserTemporaryCommand> accountCreatecommandCaptor;
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

  @Test
  public void checkPassword_withInvalidUsername() {
    // prepare
    when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

    // execute
    assertThrows(AuthenticationException.class, () -> userService.checkPassword("invalid", "secret"));
  }

  @Test
  public void checkPassword_withInvalidPassword() {
    // prepare
    User user = new User().setPasswordHash("hash");
    when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
    when(cryptoService.checkPassword("wrong password", "hash")).thenReturn(false);

    // execute
    assertThrows(AuthenticationException.class, () -> userService.checkPassword("user", "wrong password"));
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
    CreateUserTemporaryCommand command = new CreateUserTemporaryCommand();
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
    CreateUserTemporaryCommand command = new CreateUserTemporaryCommand();
    DisplayableException thrown = catchThrowableOfType(() -> userService.createAccountTemporary(command), DisplayableException.class);

    // verify
    assertThat(thrown).hasMessage("error.emailAddressTaken");
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
}
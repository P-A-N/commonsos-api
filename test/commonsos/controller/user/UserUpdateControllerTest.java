package commonsos.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UserUpdateCommand;
import commonsos.view.UserPrivateView;
import spark.Request;
import spark.Response;

@ExtendWith(MockitoExtension.class)
public class UserUpdateControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @InjectMocks UserUpdateController controller;

  @BeforeEach
  public void setGson() {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    // prepare
    String json = "{"
        + " \"id\":3,"
        + " \"firstName\":\"John\","
        + " \"lastName\":\"Doe\","
        + " \"description\":\"Retired\","
        + " \"location\":\"Sapporo\""
        + "}";
    when(request.body()).thenReturn(json);
    User updatedUser = new User();
    when(userService.updateUser(any(), any())).thenReturn(updatedUser);
    UserPrivateView privateView = new UserPrivateView();
    when(userService.privateView(updatedUser)).thenReturn(privateView);

    // execute
    User user = new User();
    UserPrivateView result = controller.handleAfterLogin(user, request, response);

    // verify
    ArgumentCaptor<UserUpdateCommand> commandCaptor = ArgumentCaptor.forClass(UserUpdateCommand.class);
    verify(userService, times(1)).updateUser(eq(user), commandCaptor.capture());
    UserUpdateCommand actualCommand = commandCaptor.getValue();
    assertThat(actualCommand.getFirstName()).isEqualTo("John");
    assertThat(actualCommand.getLastName()).isEqualTo("Doe");
    assertThat(actualCommand.getDescription()).isEqualTo("Retired");
    assertThat(actualCommand.getLocation()).isEqualTo("Sapporo");
    
    verify(userService, times(1)).privateView(updatedUser);
    assertThat(result).isEqualTo(privateView);
  }
}
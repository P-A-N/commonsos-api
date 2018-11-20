package commonsos.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.controller.user.UserDeleteController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class UserDeleteControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @Captor ArgumentCaptor<User> userCaptor;
  @InjectMocks UserDeleteController controller;

  @Test
  public void handle_noId() {
    // execute
    User user = new User();
    Object result = controller.handleAfterLogin(user, request, response);

    // verify
    assertThat(result).isEqualTo("");
    verify(userService, times(1)).deleteUserLogically(userCaptor.capture());
    User actualUser = userCaptor.getValue();
    assertThat(actualUser).isEqualTo(user);
  }
}
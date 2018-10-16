package commonsos.controller.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import commonsos.util.UserUtil;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @Mock UserUtil userUtil;
  @InjectMocks UserController controller;

  @Test
  public void handle_noId() {
    controller.handle(new User(), request, response);
    verify(userService, times(1)).privateView(any(User.class));
  }

  @Test
  public void handle_adminUser() {
    // prepare
    when(request.params("id")).thenReturn("123");
    when(userUtil.isAdminOfUser(any(), any())).thenReturn(true);

    // execute
    controller.handle(new User(), request, response);

    // verify
    verify(userService, times(1)).privateView(any(User.class), any(Long.class));
    verify(userService, never()).view(any(Long.class));
  }

  @Test
  public void handle_generalUser() {
    // prepare
    when(request.params("id")).thenReturn("123");
    when(userUtil.isAdminOfUser(any(), any())).thenReturn(false);

    // execute
    controller.handle(new User(), request, response);

    // verify
    verify(userService, never()).privateView(any(User.class), any(Long.class));
    verify(userService, times(1)).view(any(Long.class));
  }
}
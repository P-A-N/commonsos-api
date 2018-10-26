package commonsos.controller.user;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
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

import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @InjectMocks UserController controller;

  @Test
  public void handle_noId() {
    controller.handle(new User(), request, response);
    verify(userService, times(1)).privateView(any(User.class));
  }

  @Test
  public void handle_adminUser() {
    // prepare
    User user = new User().setId(id("user")).setCommunityList(asList(new Community().setId(id("community"))));
    when(request.params("id")).thenReturn("123");
    when(userService.user(any())).thenReturn(
        new User().setId(id("other")).setCommunityList(asList(new Community().setId(id("community")).setAdminUser(user))));

    // execute
    controller.handle(user, request, response);

    // verify
    verify(userService, times(1)).privateView(any(User.class), any(Long.class));
    verify(userService, never()).view(any(Long.class));
  }

  @Test
  public void handle_generalUser() {
    // prepare
    User user = new User().setId(id("user")).setCommunityList(asList(new Community().setId(id("community"))));
    when(request.params("id")).thenReturn("123");
    when(userService.user(any())).thenReturn(
        new User().setId(id("other")).setCommunityList(asList(new Community().setId(id("community")).setAdminUser(new User().setId(id("user2"))))));

    // execute
    controller.handle(user, request, response);

    // verify
    verify(userService, never()).privateView(any(User.class), any(Long.class));
    verify(userService, times(1)).view(any(Long.class));
  }
}
package commonsos.controller.app.user;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.user.UserController;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import spark.Request;
import spark.Response;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @InjectMocks UserController controller;

  @Test
  public void handle_noId() {
    controller.handleAfterLogin(new User(), request, response);
    verify(userService, times(1)).privateView(any(User.class));
  }

  @Test
  public void handle_adminUser() {
    // prepare
    User user = new User().setId(id("user")).setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community")))));
    when(request.params("id")).thenReturn("123");
    when(userService.user(any())).thenReturn(
        new User().setId(id("other")).setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community")).setAdminUser(user)))));

    // execute
    controller.handleAfterLogin(user, request, response);

    // verify
    verify(userService, times(1)).privateView(any(User.class), any(Long.class));
    verify(userService, never()).publicUserAndCommunityView(any(Long.class));
  }

  @Test
  public void handle_generalUser() {
    // prepare
    User user = new User().setId(id("user")).setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community")))));
    when(request.params("id")).thenReturn("123");
    when(userService.user(any())).thenReturn(
        new User().setId(id("other")).setCommunityUserList(asList(new CommunityUser().setCommunity(new Community().setId(id("community")).setAdminUser(new User().setId(id("user2")))))));

    // execute
    controller.handleAfterLogin(user, request, response);

    // verify
    verify(userService, never()).privateView(any(User.class), any(Long.class));
    verify(userService, times(1)).publicUserAndCommunityView(any(Long.class));
  }
}
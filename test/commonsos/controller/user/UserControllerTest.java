package commonsos.controller.user;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.repository.user.User;
import commonsos.service.community.CommunityService;
import commonsos.service.user.UserService;
import commonsos.service.view.UserPrivateView;
import commonsos.service.view.UserView;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock UserService userService;
  @Mock CommunityService communityService;
  @InjectMocks UserController controller;

  @Test
  public void handle_noId() {
    // prepare
    when(request.params("id")).thenReturn(null);
    UserPrivateView userView = new UserPrivateView();
    when(userService.privateView(any())).thenReturn(userView);

    // execute
    User user = new User();
    Object result = controller.handle(user, request, response);

    // verify
    verify(userService, times(1)).privateView(user);
    assertThat(result).isEqualTo(userView);
  }

  @Test
  public void handle_withRequestedId() {
    // prepare
    when(request.params("id")).thenReturn("123");
    User requestedUser = new User().setId(id("requestedId")).setCommunityId(id("community"));
    when(userService.user(123L)).thenReturn(requestedUser);
    when(communityService.isAdmin(id("user"), id("community"))).thenReturn(false);
    UserView userView = new UserView();
    when(userService.view(123L)).thenReturn(userView);

    // execute
    User user = new User().setId(id("user"));
    Object result = controller.handle(user, request, response);

    // verify
    verify(userService, times(1)).view(123L);
    assertThat(result).isEqualTo(userView);
  }

  @Test
  public void handle_withOtherUserId_admin() {
    // prepare
    when(request.params("id")).thenReturn("123");
    User requestedUser = new User().setId(id("requestedId")).setCommunityId(id("community"));
    when(userService.user(123L)).thenReturn(requestedUser);
    when(communityService.isAdmin(id("user"), id("community"))).thenReturn(true);
    UserPrivateView userView = new UserPrivateView();
    when(userService.privateView(any(), any())).thenReturn(userView);

    // execute
    User user = new User().setId(id("user"));
    Object result = controller.handle(user, request, response);

    // verify
    verify(userService, times(1)).privateView(user, 123L);
    assertThat(result).isEqualTo(userView);
  }
}
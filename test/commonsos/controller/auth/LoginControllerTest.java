package commonsos.controller.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.LogFilter.USERNAME_MDC_KEY;
import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import commonsos.CSRF;
import commonsos.GsonProvider;
import commonsos.UserSession;
import commonsos.repository.user.User;
import commonsos.service.user.UserPrivateView;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;
import spark.Session;

@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock Session session;
  @Mock UserService userService;
  @Mock CSRF csrf;
  @InjectMocks LoginController controller;

  @Before
  public void setUp() throws Exception {
    controller.gson = new GsonProvider().get();
    when(request.session()).thenReturn(session);
  }

  @Test
  public void handle() {
    // prepare
    User user = new User().setUsername("john");
    when(userService.checkPassword("john", "pwd")).thenReturn(user);
    when(request.body()).thenReturn("{\"username\": \"john\", \"password\": \"pwd\"}");
    UserSession userSession = new UserSession();
    when(userService.session(user)).thenReturn(userSession);
    UserPrivateView userView = new UserPrivateView();
    when(userService.privateView(user)).thenReturn(userView);

    // execute
    UserPrivateView result = controller.handle(request, response);

    // verify
    verify(userService, times(1)).checkPassword("john", "pwd");
    verify(userService, times(1)).session(user);
    verify(session, times(1)).attribute(USER_SESSION_ATTRIBUTE_NAME, userSession);
    verify(session, times(1)).maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);
    assertThat(MDC.get(USERNAME_MDC_KEY)).isEqualTo("john");
    verify(csrf, times(1)).setToken(request, response);
    verify(userService, times(1)).privateView(user);
    assertThat(result).isSameAs(userView);
  }
}
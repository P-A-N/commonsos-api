package commonsos.controller.app.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.controller.app.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;
import static commonsos.filter.LogFilter.USERNAME_MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import commonsos.controller.app.auth.LoginController;
import commonsos.di.GsonProvider;
import commonsos.filter.CSRF;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.session.UserSession;
import commonsos.view.app.PrivateUserView;
import spark.Request;
import spark.Response;
import spark.Session;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock Session session;
  @Mock UserService userService;
  @Mock CSRF csrf;
  @InjectMocks LoginController controller;

  @BeforeEach
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
    PrivateUserView userView = new PrivateUserView();
    when(userService.privateView(user)).thenReturn(userView);

    // execute
    PrivateUserView result = controller.handle(request, response);

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
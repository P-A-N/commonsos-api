package commonsos.controller.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.LogFilter.USERNAME_MDC_KEY;
import static commonsos.controller.auth.LoginController.USER_SESSION_ATTRIBUTE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import commonsos.CSRF;
import commonsos.GsonProvider;
import commonsos.UserSession;
import commonsos.repository.user.User;
import commonsos.service.auth.AccountCreateCommand;
import commonsos.service.user.UserPrivateView;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;
import spark.Session;

@RunWith(MockitoJUnitRunner.class)
public class AccountCreateControllerTest {

  @Mock Request request;
  @Mock Response response;
  @Mock Session session;
  @Mock UserService userService;
  @Mock CSRF csrf;
  @InjectMocks AccountCreateController controller;

  @Before
  public void setUp() throws Exception {
    controller.gson = new GsonProvider().get();
    when(request.session()).thenReturn(session);
  }

  @Test
  public void handle() {
    // prepare
    when(request.body()).thenReturn(
        "{"
        + " \"username\": \"john\","
        + " \"password\": \"pwd\","
        + " \"firstName\": \"first\","
        + " \"lastName\": \"last\","
        + " \"description\": \"hello\","
        + " \"location\": \"Shibuya\","
        + " \"communityList\": [33,44],"
        + " \"emailAddress\": \"test@test.com\","
        + " \"waitUntilCompleted\": true"
        + "}");
    User user = new User().setUsername("john");
    when(userService.create(any())).thenReturn(user);
    UserSession userSession = new UserSession();
    when(userService.session(user)).thenReturn(userSession);
    UserPrivateView userView = new UserPrivateView();
    when(userService.privateView(user)).thenReturn(userView);
    
    // execute
    UserPrivateView result = controller.handle(request, response);

    // verify
    ArgumentCaptor<AccountCreateCommand> commandCaptor = ArgumentCaptor.forClass(AccountCreateCommand.class);
    verify(userService, times(1)).create(commandCaptor.capture());
    AccountCreateCommand actualCommand = commandCaptor.getValue();
    
    assertThat(actualCommand.getUsername()).isEqualTo("john");
    assertThat(actualCommand.getPassword()).isEqualTo("pwd");
    assertThat(actualCommand.getFirstName()).isEqualTo("first");
    assertThat(actualCommand.getLastName()).isEqualTo("last");
    assertThat(actualCommand.getDescription()).isEqualTo("hello");
    assertThat(actualCommand.getLocation()).isEqualTo("Shibuya");
    assertThat(actualCommand.getCommunityList().get(0)).isEqualTo(33L);
    assertThat(actualCommand.getCommunityList().get(1)).isEqualTo(44L);
    assertThat(actualCommand.getEmailAddress()).isEqualTo("test@test.com");
    assertThat(actualCommand.isWaitUntilCompleted()).isEqualTo(true);

    verify(userService, times(1)).session(user);
    verify(session, times(1)).attribute(USER_SESSION_ATTRIBUTE_NAME, userSession);
    verify(session, times(1)).maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);
    assertThat(MDC.get(USERNAME_MDC_KEY)).isEqualTo("john");
    verify(csrf, times(1)).setToken(request, response);
    verify(userService, times(1)).privateView(user);
    assertThat(result).isSameAs(userView);
  }
}
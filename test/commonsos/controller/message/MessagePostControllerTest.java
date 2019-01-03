package commonsos.controller.message;

import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.MessagePostCommand;
import commonsos.view.MessageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessagePostControllerTest {

  @Mock MessageService service;
  @Mock Request request;
  @InjectMocks MessagePostController controller;

  @Before
  public void setUp() throws Exception {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    User user = new User();
    when(request.body()).thenReturn("{\"threadId\": \"1\", \"text\": \"message text\"}");
    MessagePostCommand command = new MessagePostCommand().setThreadId(1L).setText("message text");
    MessageView messageView = new MessageView();
    when(service.postMessage(user, command)).thenReturn(messageView);

    MessageView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(messageView);
  }
}
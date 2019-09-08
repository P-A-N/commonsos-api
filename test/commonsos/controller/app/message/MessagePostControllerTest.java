package commonsos.controller.app.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.message.MessagePostController;
import commonsos.controller.command.app.MessagePostCommand;
import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.app.MessageView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class MessagePostControllerTest {

  @Mock MessageService service;
  @Mock Request request;
  @InjectMocks MessagePostController controller;

  @BeforeEach
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
package commonsos.controller.app.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.message.MessageThreadController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class MessageThreadControllerTest {

  @InjectMocks MessageThreadController controller;
  @Mock Request request;
  @Mock MessageService service;

  @Test
  public void handle() {
    User user = new User();
    when(request.params("id")).thenReturn("123");
    MessageThreadView view = new MessageThreadView();
    when(service.thread(user, 123L)).thenReturn(view);

    MessageThreadView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(view);
  }
}
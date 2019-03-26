package commonsos.controller.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class MessageListControllerTest {

  @InjectMocks MessageListController controller;
  @Mock MessageService service;
  @Mock Request request;

  @Test
  public void handle() {
    User user = new User();
    List<MessageView> messages = new ArrayList<>();
    when(request.params("id")).thenReturn("123");
    when(service.messages(user, 123L)).thenReturn(messages);

    List<MessageView> result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isSameAs(messages);
  }
}
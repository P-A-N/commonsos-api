package commonsos.controller.app.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.message.MessageThreadForAdController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.view.MessageThreadView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class MessageThreadForAdControllerTest {

  @InjectMocks MessageThreadForAdController controller;
  @Mock Request request;
  @Mock MessageService service;

  @Test
  public void handle() {
    User user = new User();
    when(request.params("adId")).thenReturn("123");
    MessageThreadView view = new MessageThreadView();
    when(service.threadForAd(user, 123L)).thenReturn(view);

    MessageThreadView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(view);
  }
}
package commonsos.controller.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.repository.entity.User;
import commonsos.service.MessageService;

@ExtendWith(MockitoExtension.class)
public class MessageThreadUnreadCountControllerTest {

  @InjectMocks MessageThreadUnreadCountController controller;
  @Mock MessageService service;

  @Test
  public void handle() {
    User user = new User();
    when(service.unreadMessageThreadCount(user)).thenReturn(3);

    Map<String, Object> result = controller.handleAfterLogin(user, null, null);

    assertThat(result.get("count")).isEqualTo(3);
  }
}
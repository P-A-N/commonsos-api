package commonsos.service.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import commonsos.repository.entity.User;

@ExtendWith(MockitoExtension.class)
public class PushNotificationServiceTest {

  @InjectMocks @Spy PushNotificationService service;
  @Mock FirebaseMessaging firebase;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS) Message.Builder messageBuilder;

  @Test
  public void sendWithoutParameters() {
    User recipient = new User();

    service.send(recipient, "message");

    verify(service).send(recipient, "message", Collections.emptyMap());
  }

  @Test
  public void sendsMessageToUserToken() throws FirebaseMessagingException {
    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);
    doReturn(firebase).when(service).getInstance();
    doReturn(messageBuilder).when(service).messageBuilder();
    Message message = mock(Message.class);
    when(messageBuilder.build()).thenReturn(message);
    when(messageBuilder.setToken(any())).thenReturn(messageBuilder);
    when(messageBuilder.setApnsConfig(any())).thenReturn(messageBuilder);
    when(messageBuilder.setAndroidConfig(any())).thenReturn(messageBuilder);
    when(messageBuilder.setNotification(notificationArgumentCaptor.capture())).thenReturn(messageBuilder);
    when(messageBuilder.putAllData(any())).thenReturn(messageBuilder);

    User recipient = new User().setPushNotificationToken("user push token");

    service.send(recipient, "message text", ImmutableMap.of("param1", "value1"));

    verify(firebase).send(message);
    verify(messageBuilder).setToken("user push token");
  }
}
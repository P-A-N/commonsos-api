package commonsos.service.notification;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import commonsos.Configuration;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.User;
import commonsos.service.AbstractService;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class PushNotificationService extends AbstractService {

  @Inject Configuration configuration;

  @Inject
  public void init() {
    try {
      FileInputStream serviceAccount = new FileInputStream(configuration.firebaseCredentialsFile());
      FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build();
      FirebaseApp.initializeApp(options);
    }
    catch (Exception e) {
      log.error("Firebase init failed", e);
      throw new RuntimeException(e);
    }
  }
  
  public void send(User senderUser, User recipient, String message, MessageThread messageThread, Integer unreadCount) {
    if (StringUtils.isEmpty(recipient.getPushNotificationToken())) return;
    log.info(String.format("Sending push notification from user: %s, to user: %s, token: %s", senderUser.getUsername(), recipient.getUsername(), recipient.getPushNotificationToken()));
    send(senderUser.getUsername(), message, messageThread.getId(), unreadCount, recipient.getPushNotificationToken());
  }
  
  public void send(Community senderCommunity, User recipient, String message, MessageThread messageThread, Integer unreadCount) {
    if (StringUtils.isEmpty(recipient.getPushNotificationToken())) return;
    log.info(String.format("Sending push notification from user: %s, to user: %s, token: %s", senderCommunity.getName(), recipient.getUsername(), recipient.getPushNotificationToken()));
    send(senderCommunity.getName(), message, messageThread.getId(), unreadCount, recipient.getPushNotificationToken());
  }

  // see https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages#AndroidConfig
  private void send(String title, String messageBody, Long messageThreadId, Integer unreadCount, String clientToken ) {
    // notification
    Notification notification = new Notification(title, messageBody);
    
    // data
    Map<String, String> data = new HashMap<>();
    data.put("type", "new_message");
    data.put("threadId", Long.toString(messageThreadId));
    
    // android
    AndroidConfig androidConfig = AndroidConfig.builder()
      .setCollapseKey("personal")
      .setTtl(Duration.ofMinutes(2).toMillis())
      .setPriority(AndroidConfig.Priority.HIGH)
      .setNotification(
          AndroidNotification.builder()
            .setColor("#000000")
            // TODO notification_count
            .setTag("personal")
            .build())
      .build();

    // apns
    ApnsConfig apnsConfig = ApnsConfig.builder()
      .setAps(
          Aps.builder()
            .setSound("default")
            .setBadge(unreadCount)
            .setCategory("personal")
            .setThreadId("personal")
            .build())
      .build();

    // create message
    com.google.firebase.messaging.Message message = messageBuilder()
      .setNotification(notification)
      .putAllData(data)
      .setAndroidConfig(androidConfig)
      .setApnsConfig(apnsConfig)
      .setToken(clientToken)
      .build();

    // send message
    try {
      String response = getInstance().send(message);
      log.info("Successfully sent message: " + response);
    }
    catch (FirebaseMessagingException e) {
      log.warn(String.format("Failed to send push notification to %s", clientToken), e);
    }
  }

  Message.Builder messageBuilder() {
    return Message.builder();
  }

  FirebaseMessaging getInstance() {
    return FirebaseMessaging.getInstance();
  }
}

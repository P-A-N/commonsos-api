package commonsos.util;

import commonsos.repository.entity.Notification;
import commonsos.view.NotificationView;

public class NotificationUtil {
  
  private NotificationUtil() {}

  public static NotificationView view(Notification notification) {
    return new NotificationView()
        .setId(notification.getId())
        .setTitle(notification.getTitle())
        .setUrl(notification.getUrl())
        .setCreatedAt(notification.getCreatedAt());
  }
}

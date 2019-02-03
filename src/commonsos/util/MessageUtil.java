package commonsos.util;

public class MessageUtil {

  private static final Long SYSTEM_MESSAGE_CREATOR_ID = -1L;
  private static final String SYSTEM_MESSAGE_UNSUBSCRIBE = "%sがグループから退出しました";

  private MessageUtil() {}

  public static Long getSystemMessageCreatorId() {
    return SYSTEM_MESSAGE_CREATOR_ID;
  }
  
  public static String getSystemMessageUnsubscribe(String username) {
    return String.format(SYSTEM_MESSAGE_UNSUBSCRIBE, username);
  }
}

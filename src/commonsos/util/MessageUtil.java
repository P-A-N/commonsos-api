package commonsos.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class MessageUtil {

  private static final Long SYSTEM_MESSAGE_CREATOR_ID = -1L;
  private static final String SYSTEM_MESSAGE_UNSUBSCRIBE = "%sがグループから退出しました";
  private static final String SYSTEM_MESSAGE_TOKEN_SEND = "%sさんから%sさんへ%f%sを送信しました。";

  private MessageUtil() {}

  public static Long getSystemMessageCreatorId() {
    return SYSTEM_MESSAGE_CREATOR_ID;
  }
  
  public static String getSystemMessageUnsubscribe(String username) {
    return String.format(SYSTEM_MESSAGE_UNSUBSCRIBE, username);
  }
  
  public static String getSystemMessageTokenSend(String remitterUsername, String beneficiaryUsername, BigDecimal amount, String symbol, String description) {
    String message = String.format(SYSTEM_MESSAGE_TOKEN_SEND, remitterUsername, beneficiaryUsername, amount.stripTrailingZeros(), symbol);
    if (StringUtils.isNotEmpty(description)) {
      message = message + String.format("\n【コメント】\n%s", description);
    }
    
    return message;
  }
}

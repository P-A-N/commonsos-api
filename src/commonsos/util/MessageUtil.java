package commonsos.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class MessageUtil {

  private static final Long SYSTEM_MESSAGE_CREATOR_ID = -1L;
  private static final String SYSTEM_MESSAGE_UNSUBSCRIBE = "%sがグループから退出しました";
  private static final String SYSTEM_MESSAGE_TOKEN_SEND_FROM_USER = "%sさんから%sさんへ%s %sを送信しました。";
  private static final String SYSTEM_MESSAGE_TOKEN_SEND_FROM_COMM = "%sから%sさんへ%s %sを送信しました。";

  private MessageUtil() {}

  public static Long getSystemMessageCreatorId() {
    return SYSTEM_MESSAGE_CREATOR_ID;
  }
  
  public static String getSystemMessageUnsubscribe(String username) {
    return String.format(SYSTEM_MESSAGE_UNSUBSCRIBE, username);
  }
  
  public static String getSystemMessageTokenSend1(String remitterUsername, String beneficiaryUsername, BigDecimal amount, String symbol, String description) {
    String message = String.format(SYSTEM_MESSAGE_TOKEN_SEND_FROM_USER, remitterUsername, beneficiaryUsername, amount.stripTrailingZeros().toPlainString(), symbol);
    if (StringUtils.isNotEmpty(description)) {
      message = message + String.format("\n【コメント】\n%s", description);
    }
    
    return message;
  }
  
  public static String getSystemMessageTokenSend2(String communityName, String beneficiaryUsername, BigDecimal amount, String symbol) {
    String message = String.format(SYSTEM_MESSAGE_TOKEN_SEND_FROM_COMM, communityName, beneficiaryUsername, amount.stripTrailingZeros().toPlainString(), symbol);
    return message;
  }
}

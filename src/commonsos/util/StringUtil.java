package commonsos.util;

public class StringUtil {
  
  private StringUtil() {}

  public static int unicodeLength(String text) {
    return text == null ? 0 : text.codePointCount(0, text.length());
  }
}

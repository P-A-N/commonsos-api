package commonsos.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;

public class ValidateUtil {
  
  private ValidateUtil() {}

  public static void validateEmailAddress(String emailAddress) {
    if (emailAddress == null || !EmailValidator.getInstance().isValid(emailAddress)) throw new BadRequestException("invalid email address");
  }
  
  public static void validateUrl(String url) {
    if (url == null || !UrlValidator.getInstance().isValid(url)) throw new BadRequestException("invalid url");
  }
  
  public static void validatePassword(String password) {
    if (password == null || password.length() < 8) throw new BadRequestException("invalid password");
    if (!StringUtils.isAsciiPrintable(password) || password.contains(" ")) throw new DisplayableException("error.invalid_character_in_password");
  }
  
  public static void validateUsername(String username) {
    if (username == null || StringUtil.unicodeLength(username) < 4) throw new BadRequestException("invalid username");
  }
  
  public static void validateStatus(String status) {
    if (status != null && StringUtil.unicodeLength(status) > 50) throw new BadRequestException("invalid status");
  }
}

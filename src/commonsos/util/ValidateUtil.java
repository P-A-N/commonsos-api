package commonsos.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;

public class ValidateUtil {
  
  private static Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  private static Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  
  private ValidateUtil() {}

  public static void validateEmailAddress(String emailAddress) {
    if (emailAddress == null || !EmailValidator.getInstance().isValid(emailAddress)) throw new BadRequestException("invalid email address");
  }
  
  public static void validateUrl(String url) {
    if (url == null || !UrlValidator.getInstance().isValid(url)) throw new BadRequestException("invalid url");
  }
  
  public static void validatePassword(String password) {
    if (password == null) throw  new BadRequestException("invalid password");
    
    Matcher m = passwordPattern.matcher(password);
    if (!m.find()) throw new DisplayableException("error.invalid_character_in_password");
    
    if (password.length() < 8) throw new DisplayableException("error.invalid_password_length");

  }
  
  public static void validateUsername(String username) {
    if (username == null) throw  new BadRequestException("invalid username");
    
    Matcher m = usernamePattern.matcher(username);
    if (!m.find()) throw new DisplayableException("error.invalid_character_in_username");
    
    if (username.length() < 4 || username.length() > 15) throw new DisplayableException("error.invalid_username_length");
  }
  
  public static void validateStatus(String status) {
    if (status != null && StringUtil.unicodeLength(status) > 50) throw new BadRequestException("invalid status");
  }
}

package commonsos.util;

import org.junit.Test;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;

public class ValidateUtilTest {

  @Test
  public void validate_username_valid() {
    ValidateUtil.validateUsername("1234");
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_null() {
    ValidateUtil.validateUsername(null);
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length1() {
    ValidateUtil.validateUsername("123");
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length2() {
    ValidateUtil.validateUsername("１２３");
  }

  @Test(expected = BadRequestException.class)
  public void validate_username_less_length3() {
    ValidateUtil.validateUsername("🍺🍺🍺");
  }

  @Test
  public void validate_password_valid() {
    ValidateUtil.validatePassword("12345678");
    ValidateUtil.validatePassword("abcdefghijklmnopqrstuvwxyz!\"#$%&'()-=~^\\|@`[{;+:*]},<.>/?_");
  }

  @Test(expected = BadRequestException.class)
  public void validate_password_null() {
    ValidateUtil.validatePassword(null);
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_password_less_length() {
    ValidateUtil.validatePassword("1234567");
  }

  @Test(expected = DisplayableException.class)
  public void validate_password_unicode() {
    ValidateUtil.validatePassword("１２３４５６７８");
  }

  @Test(expected = DisplayableException.class)
  public void validate_password_space() {
    ValidateUtil.validatePassword("1 2 3 4 5 6 7 8");
  }

  @Test
  public void validate_emailAddress_valid() {
    ValidateUtil.validateEmailAddress("test@test.com");
    ValidateUtil.validateEmailAddress("a.b.c@test.com");
    ValidateUtil.validateEmailAddress("a@a.b.c.com");
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_null() {
    ValidateUtil.validateEmailAddress(null);
  }

  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid1() {
    ValidateUtil.validateEmailAddress("");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid2() {
    ValidateUtil.validateEmailAddress("aaa");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid3() {
    ValidateUtil.validateEmailAddress("a.@test.com");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid4() {
    ValidateUtil.validateEmailAddress("a<b@test.com");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid5() {
    ValidateUtil.validateEmailAddress("a>b@test.com");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid6() {
    ValidateUtil.validateEmailAddress("a@test<com");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid7() {
    ValidateUtil.validateEmailAddress("a@test>com");
  }
  
  @Test(expected = BadRequestException.class)
  public void validate_emailAddress_invalid8() {
    ValidateUtil.validateEmailAddress("a@a@a.com");
  }

  @Test
  public void validateUrl_valid() {
    ValidateUtil.validateUrl("http://test.com");
    ValidateUtil.validateUrl("http://test.com/");
    ValidateUtil.validateUrl("http://test.com/path");
    ValidateUtil.validateUrl("http://test.com/path/");
    ValidateUtil.validateUrl("http://test.com/path/index.html");
    ValidateUtil.validateUrl("http://test.com/path/index.html#anchor");
    ValidateUtil.validateUrl("http://test.com/path/index.html?a=b&c=%E3%#ancor");
    ValidateUtil.validateUrl("http://test.com:8080/path/index.html");
    ValidateUtil.validateUrl("http://192.168.0.1/path/index.html");
    ValidateUtil.validateUrl("HTTP://TEST.COM/PATH/INDEX.HTML");
    ValidateUtil.validateUrl("https://test.com/path/index.html");
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_null() {
    ValidateUtil.validateUrl(null);
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_empty() {
    ValidateUtil.validateUrl("");
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_url1() {
    ValidateUtil.validateUrl("aaaaaaaa");
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_url2() {
    ValidateUtil.validateUrl("test.com/path/index.html");
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_url3() {
    ValidateUtil.validateUrl("http://test.com//path/index.html");
  }

  @Test(expected = BadRequestException.class)
  public void validateUrl_invalid_url4() {
    ValidateUtil.validateUrl("http://test.com:80:80/path/index.html");
  }

  @Test
  public void validate_status_valid() {
    ValidateUtil.validateStatus(null);
    ValidateUtil.validateStatus("");
    ValidateUtil.validateStatus("12345678901234567890123456789012345678901234567890"); // length = 50, ascii
    ValidateUtil.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０"); // length = 50, utf-8
    ValidateUtil.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺"); // length = 50, 4 byte unicode
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid1() {
    ValidateUtil.validateStatus("123456789012345678901234567890123456789012345678901"); // length = 51, ascii
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid2() {
    ValidateUtil.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１"); // length = 51, utf-8
  }

  @Test(expected = BadRequestException.class)
  public void validate_status_invalid3() {
    ValidateUtil.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺"); // length = 51, 4 byte unicode
  }
}
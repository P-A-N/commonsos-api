package commonsos.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.repository.entity.Role;
import commonsos.service.image.ImageType;

public class ValidateUtilTest {

  @Test
  public void validate_username_valid() {
    ValidateUtil.validateUsername("1234");
    ValidateUtil.validateUsername("abcdefghijklmno");
    ValidateUtil.validateUsername("pqrstuvwxyz_");
    ValidateUtil.validateUsername("ABCDEFGHIJKLMNO");
    ValidateUtil.validateUsername("PQRSTUVWXYZ_");
  }

  @Test
  public void validate_username_null() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUsername(null));
  }

  @Test
  public void validate_username_less_length1() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateUsername("123"));
  }

  @Test
  public void validate_username_more_length1() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateUsername("1234567890123456"));
  }

  @Test
  public void validate_username_invalid_char1() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateUsername("１２３"));
  }

  @Test
  public void validate_username_invalid_char2() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateUsername("🍺🍺🍺"));
  }

  @Test
  public void validate_password_valid() {
    ValidateUtil.validatePassword("12345678");
    ValidateUtil.validatePassword("123456789abcdefghijklmnopqrstuvwxyz_");
    ValidateUtil.validatePassword("123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_");
  }

  @Test
  public void validate_password_null() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validatePassword(null));
  }
  
  @Test
  public void validate_password_less_length() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validatePassword("1234567"));
  }

  @Test
  public void validate_password_unicode() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validatePassword("１２３４５６７８"));
  }

  @Test
  public void validate_password_space() {
    assertThrows(DisplayableException.class, () -> ValidateUtil.validatePassword("1 2 3 4 5 6 7 8"));
  }

  @Test
  public void validate_emailAddress_valid() {
    ValidateUtil.validateEmailAddress("test@test.com");
    ValidateUtil.validateEmailAddress("a.b.c@test.com");
    ValidateUtil.validateEmailAddress("a@a.b.c.com");
  }

  @Test
  public void validate_emailAddress_null() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress(null));
  }

  @Test
  public void validate_emailAddress_invalid1() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress(""));
  }
  
  @Test
  public void validate_emailAddress_invalid2() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("aaa"));
  }
  
  @Test
  public void validate_emailAddress_invalid3() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a.@test.com"));
  }
  
  @Test
  public void validate_emailAddress_invalid4() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a<b@test.com"));
  }
  
  @Test
  public void validate_emailAddress_invalid5() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a>b@test.com"));
  }
  
  @Test
  public void validate_emailAddress_invalid6() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a@test<com"));
  }
  
  @Test
  public void validate_emailAddress_invalid7() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a@test>com"));
  }
  
  @Test
  public void validate_emailAddress_invalid8() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateEmailAddress("a@a@a.com"));
  }

  @Test
  public void validate_telNo() {
    ValidateUtil.validateTelNo(null);
    ValidateUtil.validateTelNo("");
    ValidateUtil.validateTelNo("0123456789");
    ValidateUtil.validateTelNo("012-3456-7890");
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateTelNo("０１２３４５６７８９"));
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateTelNo("abcdefg"));
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateTelNo("000_0000_0000"));
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateTelNo("000 0000 0000"));
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

  @Test
  public void validateUrl_invalid_null() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl(null));
  }

  @Test
  public void validateUrl_invalid_empty() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl(""));
  }

  @Test
  public void validateUrl_invalid_url1() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl("aaaaaaaa"));
  }

  @Test
  public void validateUrl_invalid_url2() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl("test.com/path/index.html"));
  }

  @Test
  public void validateUrl_invalid_url3() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl("http://test.com//path/index.html"));
  }

  @Test
  public void validateUrl_invalid_url4() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateUrl("http://test.com:80:80/path/index.html"));
  }

  @Test
  public void validate_status_valid() {
    ValidateUtil.validateStatus(null);
    ValidateUtil.validateStatus("");
    ValidateUtil.validateStatus("12345678901234567890123456789012345678901234567890"); // length = 50, ascii
    ValidateUtil.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０"); // length = 50, utf-8
    ValidateUtil.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺"); // length = 50, 4 byte unicode
  }

  @Test
  public void validate_status_invalid1() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateStatus("123456789012345678901234567890123456789012345678901")); // length = 51, ascii
  }

  @Test
  public void validate_status_invalid2() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateStatus("１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０１")); // length = 51, utf-8
  }

  @Test
  public void validate_status_invalid3() {
    assertThrows(BadRequestException.class, () -> ValidateUtil.validateStatus("🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺🍺")); // length = 51, 4 byte unicode
  }
  
  @Test
  public void validate_imageType() {
    for (ImageType imageType : ImageType.values()) {
      ValidateUtil.validateImageType(imageType);
    }
    
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateImageType(null));
  }
  
  @Test
  public void validate_role() {
    for (Role role : Role.ROLES) {
      ValidateUtil.validateRole(role.getId());
    }
    
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateRole(null));
    assertThrows(DisplayableException.class, () -> ValidateUtil.validateRole(-1L));
  }
}
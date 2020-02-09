package commonsos.util;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import commonsos.command.UpdateEmailAddressTemporaryCommand;
import commonsos.command.UploadPhotoCommand;
import commonsos.command.admin.AdminLoginCommand;
import commonsos.command.admin.CreateEthTransactionCommand;
import commonsos.command.admin.CreateTokenTransactionFromAdminCommand;
import commonsos.command.admin.UpdateAdByAdminCommand;
import commonsos.command.admin.UpdateAdPublishStatusByAdminCommand;
import commonsos.command.admin.UpdateAdminCommand;
import commonsos.command.admin.UpdateAdminPasswordCommand;
import commonsos.command.admin.UpdateCommunityTokenNameCommand;
import commonsos.command.admin.UpdateCommunityTotalSupplyCommand;
import commonsos.command.admin.UpdateUserNameByAdminCommand;
import commonsos.command.app.CreateAdCommand;
import commonsos.command.app.UpdateAdCommand;
import commonsos.exception.DisplayableException;
import commonsos.repository.entity.Role;
import commonsos.service.image.ImageType;

public class ValidateUtil {
  
  private static Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  private static Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  private static Pattern wordpressAccountIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
  private static Pattern telNoPattern = Pattern.compile("^[0-9-]+$");
  
  private ValidateUtil() {}

  public static void validateEmailAddress(String emailAddress) {
    validateEmailAddress(emailAddress, "emailAddress");
  }

  public static void validateEmailAddress(String emailAddress, String paramName) {
    if (emailAddress == null || !EmailValidator.getInstance().isValid(emailAddress)) throw DisplayableException.getInvalidException(paramName);
  }

  public static void validateTelNo(String telNo) {
    if (StringUtils.isEmpty(telNo)) return;
    Matcher m = telNoPattern.matcher(telNo);
    if (!m.find()) throw DisplayableException.getInvalidCharacterException("telNo");
  }
  
  public static void validateUrl(String url) {
    if (url == null || !UrlValidator.getInstance().isValid(url)) throw DisplayableException.getInvalidException("url");
  }
  
  public static void validatePassword(String password) {
    if (password == null) throw DisplayableException.getInvalidException("password");
    
    Matcher m = passwordPattern.matcher(password);
    if (!m.find()) throw DisplayableException.getInvalidCharacterException("password");
    
    if (password.length() < 8) throw DisplayableException.getInvalidlengthException("password");
  }
  
  public static void validateUsername(String username) {
    if (username == null) throw DisplayableException.getInvalidException("username");
    
    Matcher m = usernamePattern.matcher(username);
    if (!m.find()) throw DisplayableException.getInvalidCharacterException("username");
    
    if (username.length() < 4 || username.length() > 15) throw DisplayableException.getInvalidlengthException("username");
  }
  
  public static void validateStatus(String status) {
    if (status != null && StringUtil.unicodeLength(status) > 50) throw DisplayableException.getInvalidException("status");
  }
  
  public static void validateImageType(ImageType imageType) {
    if (imageType == null) throw DisplayableException.getNotSupportedException("imageType");
  }
  
  public static void validateRole(Long id) {
    for (Role role : Role.ROLES) {
      if (role.getId().equals(id)) return;
    }
    throw DisplayableException.getInvalidException("roleId");
  }
  
  public static void validateFee(BigDecimal fee) {
    if (fee.compareTo(BigDecimal.valueOf(100L)) > 0 || fee.compareTo(BigDecimal.ZERO) < 0) throw DisplayableException.getInvalidException("fee");
  }

  public static void validateWordpressAccountEmailAddress(String emailAddress) {
    validateEmailAddress(emailAddress, "wordpressAccountEmailAddress");
    if (StringUtils.isEmpty(emailAddress)) throw DisplayableException.getInvalidException("wordpressAccountEmailAddress");
    if (emailAddress.length() > 60) throw DisplayableException.getInvalidlengthException("wordpressAccountEmailAddress");
  }

  public static void validateWordpressAccountId(String accountId) {
    if (StringUtils.isEmpty(accountId)) throw DisplayableException.getInvalidException("wordpressAccountId");
    if (accountId.length() < 8 || accountId.length() > 60) throw DisplayableException.getInvalidlengthException("wordpressAccountId");
    if ("root".equals(accountId)) throw DisplayableException.getInvalidException("wordpressAccountId");

    Matcher m = wordpressAccountIdPattern.matcher(accountId);
    if (!m.find()) throw DisplayableException.getInvalidCharacterException("wordpressAccountId");
  }
  
  public static void validateCommand(AdminLoginCommand command) {
    if (StringUtils.isEmpty(command.getEmailAddress())) DisplayableException.getRequiredException("emailAddress");
    if (StringUtils.isEmpty(command.getPassword())) DisplayableException.getRequiredException("password");
  }
  
  public static void validateCommand(UpdateEmailAddressTemporaryCommand command) {
    if (StringUtils.isEmpty(command.getNewEmailAddress())) DisplayableException.getRequiredException("emailAddress");
    validateEmailAddress(command.getNewEmailAddress());
  }
  
  public static void validateCommand(UpdateAdminCommand command) {
    if (StringUtils.isEmpty(command.getAdminname())) throw DisplayableException.getRequiredException("adminname");
    validateTelNo(command.getTelNo());
  }
  
  public static void validateCommand(UpdateAdminPasswordCommand command) {
    if (StringUtils.isEmpty(command.getNewPassword())) throw DisplayableException.getRequiredException("password");
    ValidateUtil.validatePassword(command.getNewPassword());
  }
  
  public static void validateCommand(UpdateCommunityTotalSupplyCommand command) {
    if (command.getTotalSupply() == null) throw DisplayableException.getRequiredException("totalSupply");
  }
  
  public static void validateCommand(UpdateCommunityTokenNameCommand command) {
    if (StringUtils.isEmpty(command.getTokenName())) throw DisplayableException.getRequiredException("tokenName");
  }
  
  public static void validateCommand(UpdateUserNameByAdminCommand command) {
    if (StringUtils.isEmpty(command.getUsername())) throw DisplayableException.getRequiredException("username");
    validateUsername(command.getUsername());
  }

  public static void validateCommand(UpdateAdByAdminCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw DisplayableException.getRequiredException("title");
    if (command.getPoints() == null) throw DisplayableException.getRequiredException("points");
    if (command.getType() == null) throw DisplayableException.getRequiredException("type");
  }

  public static void validateCommand(UpdateAdPublishStatusByAdminCommand command) {
    if (command.getPublishStatus() == null) throw DisplayableException.getRequiredException("publishStatus");
  }

  public static void validateCommande(CreateAdCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw DisplayableException.getRequiredException("title");
    if (command.getPoints() == null) throw DisplayableException.getRequiredException("points");
    if (command.getType() == null) throw DisplayableException.getRequiredException("type");
  }

  public static void validateCommand(UpdateAdCommand command) {
    if (StringUtils.isEmpty(command.getTitle())) throw DisplayableException.getRequiredException("title");
    if (command.getPoints() == null) throw DisplayableException.getRequiredException("points");
    if (command.getType() == null) throw DisplayableException.getRequiredException("type");
  }

  public static void validateCommand(CreateTokenTransactionFromAdminCommand command) {
    if (command.getCommunityId() == null) throw DisplayableException.getRequiredException("communityId");
    if (command.getAmount() == null) throw DisplayableException.getRequiredException("amount");
    if (command.getWallet() == null) throw DisplayableException.getRequiredException("wallet");
    if (command.getBeneficiaryUserId() == null) throw DisplayableException.getRequiredException("beneficiaryUser");
  }

  public static void validateCommand(CreateEthTransactionCommand command) {
    if (command.getBeneficiaryCommunityId() == null) throw DisplayableException.getRequiredException("beneficiaryCommunityId");
    if (command.getAmount() == null) throw DisplayableException.getRequiredException("amount");
  }
  
  public static void validateCommand(UploadPhotoCommand command) {
    if (command.getPhotoFile() == null) throw DisplayableException.getRequiredException("photo");
  }
}

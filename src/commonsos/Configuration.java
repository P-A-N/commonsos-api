package commonsos;

import javax.inject.Singleton;

@Singleton
public class Configuration {

  public String awsSecurityKey() {
    return environmentVariable("AWS_SECRET_KEY");
  }

  public String awsAccessKey() {
    return environmentVariable("AWS_ACCESS_KEY");
  }

  public String awsS3BucketName() {
    return environmentVariable("AWS_S3_BUCKET_NAME", "commonsos-app");
  }

  public String databaseUrl() {
    return environmentVariable("DATABASE_URL", "jdbc:postgresql://localhost:5432/commonsos_test");
  }

  public String databaseUsername() {
    return environmentVariable("DATABASE_USERNAME", "commonsos_test");
  }

  public String databasePassword() {
    return environmentVariable("DATABASE_PASSWORD", "test");
  }

  public String ethererumUrl() {
    return environmentVariable("ETHEREUM_URL", "http://localhost:8545/");
  }

  public String smtpHost() {
    return environmentVariable("SMTP_HOST", "localhost");
  }

  public String smtpPort() {
    return environmentVariable("SMTP_PORT", "587");
  }

  public String smtpUser() {
    return environmentVariable("SMTP_USER", "testuser");
  }

  public String smtpPassword() {
    return environmentVariable("SMTP_PASSWORD", "test");
  }

  public String smtpFromAddress() {
    return environmentVariable("SMTP_FROM_ADDRESS", "no-replay@app.test.commons.love");
  }

  public String commonsosHost() {
    return environmentVariable("COMMONSOS_HOST", "app.test.commons.love");
  }

  public String downloadPagePath() {
    return environmentVariable("DOWNLOAD_PAGE_PATH", "/download.html");
  }

  public String transactionQrCodeSize() {
    return environmentVariable("TRANSACTION_QR_CODE_SIZE", "492");
  }

  public String transactionQrCodeLogoFile() {
    return environmentVariable("TRANSACTION_QR_CODE_LOGO_FILE", "/images/logo_170x162.png");
  }

  public String s3QrPrefix() {
    return environmentVariable("S3_QR_PREFIX", "qr/");
  }

  public String adminPageHost() {
    return environmentVariable("ADMIN_PAGE_HOST", "admin.test.commonsos.love");
  }

  public String accessControlAllowOrigin() {
    return environmentVariable("ACCESS_CONTROL_ALLOW_ORIGIN", "http://commonspeople.localhost:8888");
  }

  public String systemWallet() {
    return environmentVariable("COMMONSOS_WALLET_FILE", "C:\\Ethereum\\localnet\\keystore\\UTC--2019-12-08T02-56-18.247787000Z--c32b42b4b9f42fb47b02105e06a5dc4af0b405f5");
  }

  public String systemWalletPassword() {
    return environmentVariable("COMMONSOS_WALLET_PASSWORD", "pass1");
  }

  public String initialEther() {
    return environmentVariable("INITIAL_ETHER", "1");
  }

  public String initialWei() {
    return environmentVariable("INITIAL_WEI", "1000000000000000000");
  }

  public String minimumNumberOfDecimalsForToken() {
    return environmentVariable("MINIMUM_NUMBER_OF_DECIMALS_FOR_TOKEN", "2");
  }

  public String minimumNumberOfDecimalsForRedistribution() {
    return environmentVariable("MINIMUM_NUMBER_OF_DECIMALS_FOR_REDISTRIBUTION", "2");
  }

  public String aesKey() {
    return environmentVariable("AES_KEY", "commonsos_aes_256bid_key_uSwVjWP");
  }

  public String aesIv() {
    return environmentVariable("AES_IV", "commonsos_aes_iv");
  }
  
  public String allowedWordpressRequestIpList() {
    return environmentVariable("ALLOWED_WORDPRESS_REQUEST_IP_LIST", "127.0.0.2,127.0.0.1");
  }
  
  public String wordpressServerIp() {
    return environmentVariable("WORDPRESS_SERVER_IP", "127.0.0.1");
  }
  
  public String wordpressServerApiPort() {
    return environmentVariable("WORDPRESS_SERVER_API_PORT", "8080");
  }
  
  public String wordpressAccountDefaultPassword() {
    return environmentVariable("WORDPRESS_ACCOUNT_DEFAULT_PASSWORD", "test");
  }
  
  public String wordpressAccountDefaultAuthority() {
    return environmentVariable("WORDPRESS_ACCOUNT_DEFAULT_AUTHORITY", "nothing");
  }
  
  public String wordpressBasicAuthorizationUsername() {
    return environmentVariable("WORDPRESS_BASIC_AUTHORIZATION_USERNAME", "basic_test");
  }
  
  public String wordpressBasicAuthorizationPassword() {
    return environmentVariable("WORDPRESS_BASIC_AUTHORIZATION_PASSWORD", "basic_test_pw");
  }

  public String maintenanceMode() {
    return environmentVariable("MAINTENANCE_MODE", "false");
  }

  public String environmentVariable(String key) {
    return environmentVariable(key, null);
  }

  public String firebaseCredentialsFile() {
    return environmentVariable("FIREBASE_CREDENTIALS");
  }

  public String environmentVariable(String key, String defaultValue) {
    String value = System.getenv(key);
    if (value == null && defaultValue != null) return defaultValue;
    if (value == null && defaultValue == null) throw new RuntimeException(String.format("Environment variable %s not defined", key));
    return value;
  }
}

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

  public String walletPassword() {
    return environmentVariable("WALLET_PASSWORD", "test");
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

  public String accessControlAllowOrigin() {
    return environmentVariable("ACCESS_CONTROL_ALLOW_ORIGIN", "http://commonspeople.localhost:8888");
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

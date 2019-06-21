package commonsos.integration;

import java.io.IOException;
import java.util.Properties;

import javax.inject.Singleton;

import commonsos.service.email.EmailService;

@Singleton
public class TestEmailService extends EmailService {

  public static int TEST_SMTP_SERVER_PORT = 5587;

  @Override
  protected Properties getEmailProperties() throws IOException {
    Properties prop = super.getEmailProperties();
    prop.setProperty("mail.smtp.starttls.required", "false");
    prop.setProperty("mail.smtp.host", "localhost");
    prop.setProperty("mail.smtp.port", Integer.toString(TEST_SMTP_SERVER_PORT));
    return prop;
  }
}

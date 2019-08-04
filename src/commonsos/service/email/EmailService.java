package commonsos.service.email;

import static commonsos.service.email.EmailTemplate.CREATE_ACCOUNT;
import static commonsos.service.email.EmailTemplate.CREATE_ADMIN;
import static commonsos.service.email.EmailTemplate.EMAIL_UPDATE;
import static commonsos.service.email.EmailTemplate.PASSWORD_RESET;
import static java.util.Arrays.asList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import lombok.extern.slf4j.Slf4j;
import spark.utils.StringUtils;

@Singleton
@Slf4j
public class EmailService {
  
  private static String PROPERTY_FILE_PATH = "/mail.properties";
  private static String TEMPLATE_FILE_DIR = "/mail/template";
  private static String TEMPLATE_FILE_ENCODING = "UTF-8";
  private static String CONTENT_TIPE = "text/html; charset=UTF-8";

  @Inject Configuration conf;
  @Inject VelocityEngine ve;
  private Session session;

  @Inject
  public void init() throws IOException {
    // javamail
    Properties prop = getEmailProperties();
    session = Session.getInstance(prop, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(conf.smtpUser(), conf.smtpPassword());
      }
    });
    
    // velocity
    ve.setProperty(Velocity.RESOURCE_LOADER, "class");
    ve.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
  }
  
  protected Properties getEmailProperties() throws IOException {
    Properties prop = new Properties();
    try (InputStream inStream = this.getClass().getResourceAsStream(PROPERTY_FILE_PATH)) {
      prop.load(inStream);
    }
    prop.put("mail.smtp.host", conf.smtpHost());
    prop.put("mail.smtp.port", conf.smtpPort());
    prop.put("mail.smtp.user", conf.smtpUser());
    prop.put("mail.smtp.from", conf.smtpFromAddress());
    
    return prop;
  }
  
  public void sendCreateAccountTemporary(String toAddress, String username, String accessId) {
    VelocityContext context = new VelocityContext();
    context.put("username", username);
    context.put("accessId", accessId);
    context.put("hostname", conf.commonsosHost());
    send(toAddress, CREATE_ACCOUNT.getSubject(), CREATE_ACCOUNT.getFilename(), context);
  }

  public void sendCreateAdminTemporary(String toAddress, String adminname, String accessId) {
    VelocityContext context = new VelocityContext();
    context.put("adminname", adminname);
    context.put("accessId", accessId);
    context.put("hostname", conf.adminPageHost());
    send(toAddress, CREATE_ADMIN.getSubject(), CREATE_ADMIN.getFilename(), context);
  }
  
  public void sendUpdateEmailTemporary(String toAddress, String username, Long userId, String accessId) {
    VelocityContext context = new VelocityContext();
    context.put("username", username);
    context.put("id", userId);
    context.put("accessId", accessId);
    context.put("hostname", conf.commonsosHost());
    send(toAddress, EMAIL_UPDATE.getSubject(), EMAIL_UPDATE.getFilename(), context);
  }
  
  public void sendPasswordReset(String toAddress, String username, String accessId) {
    VelocityContext context = new VelocityContext();
    context.put("username", username);
    context.put("accessId", accessId);
    context.put("hostname", conf.commonsosHost());
    send(toAddress, PASSWORD_RESET.getSubject(), PASSWORD_RESET.getFilename(), context);
  }

  private void send(
      String to,
      String subject,
      String templateFile,
      VelocityContext context) {
    send(null, asList(to), null, null, subject, templateFile, context);
  }
  
  private void send(
      String from,
      List<String> toList,
      List<String> ccList,
      List<String> bccList,
      String subject,
      String templateFile,
      VelocityContext context) {
    log.info(String.format("sending email. templateFile=%s", templateFile));
    try {
      String content = getContent(templateFile, context);
      Message message = getMessage(from, toList, ccList, bccList, subject, content);
      Transport.send(message);
    } catch (Throwable e) {
      log.warn(String.format("sending email failed. templateFile=%s", templateFile));
      throw new ServerErrorException(e);
    }
    log.info(String.format("sending email success. templateFile=%s", templateFile));
  }
  
  private String getContent(String templateFile, VelocityContext context) {
    Template template = ve.getTemplate(
        String.format("%s/%s", TEMPLATE_FILE_DIR, templateFile),
        TEMPLATE_FILE_ENCODING);
    StringWriter sw = new StringWriter();
    template.merge(context, sw);
    return sw.toString();
  }
  
  private Message getMessage(
      String from,
      List<String> toList,
      List<String> ccList,
      List<String> bccList,
      String subject,
      String content) throws MessagingException {
    Message message = new MimeMessage(session);
    if (StringUtils.isNotBlank(from)) {
      message.setFrom(new InternetAddress(from));
    }
    if (CollectionUtils.isNotEmpty(toList)) {
      for (String to : toList) {
        message.setRecipient(TO, new InternetAddress(to));
      }
    }
    if (CollectionUtils.isNotEmpty(ccList)) {
      for (String cc : ccList) {
        message.setRecipient(CC, new InternetAddress(cc));
      }
    }
    if (CollectionUtils.isNotEmpty(bccList)) {
      for (String bcc : bccList) {
        message.setRecipient(BCC, new InternetAddress(bcc));
      }
    }
    message.setSubject(subject);
    message.setContent(content, CONTENT_TIPE);
    return message;
  }
}

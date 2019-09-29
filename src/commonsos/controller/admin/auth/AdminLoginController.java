package commonsos.controller.admin.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;

import javax.inject.Inject;

import org.slf4j.MDC;

import com.google.gson.Gson;

import commonsos.command.admin.AdminLoginCommand;
import commonsos.controller.AbstractController;
import commonsos.filter.CSRF;
import commonsos.filter.LogFilter;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.AdminUtil;
import spark.Request;
import spark.Response;
import spark.Session;

public class AdminLoginController extends AbstractController {

  public static final String ADMIN_SESSION_ATTRIBUTE_NAME = "admin";
  @Inject Gson gson;
  @Inject AdminService adminService;
  @Inject CSRF csrf;
  
  @Override
  public Object handle(Request request, Response response) {
    AdminLoginCommand command = gson.fromJson(request.body(), AdminLoginCommand.class);
    Admin admin = adminService.checkPassword(command);
    admin = adminService.updateLoggedinAt(admin);

    Session session = request.session();
    session.attribute(ADMIN_SESSION_ATTRIBUTE_NAME, adminService.session(admin));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);

    MDC.put(LogFilter.ADMIN_MDC_KEY, admin.getEmailAddress());
    csrf.setToken(request, response);
    
    return AdminUtil.view(admin);
  }
}

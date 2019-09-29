package commonsos.controller.admin.admin;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;
import static commonsos.annotation.SyncObject.ADMIN_EMAIL_ADDRESS;
import static commonsos.controller.admin.auth.AdminLoginController.ADMIN_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import org.slf4j.MDC;

import commonsos.annotation.Synchronized;
import commonsos.controller.AbstractController;
import commonsos.filter.CSRF;
import commonsos.filter.LogFilter;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.AdminUtil;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;
import spark.Session;

@Synchronized(ADMIN_EMAIL_ADDRESS)
public class CreateAdminCompleteController extends AbstractController {

  @Inject AdminService adminService;
  @Inject CSRF csrf;

  @Override
  public Object handle(Request request, Response response) {
    String accessId = RequestUtil.getPathParamString(request, "accessId");
    Admin admin = adminService.createAdminComplete(accessId);

    Session session = request.session();
    session.attribute(ADMIN_SESSION_ATTRIBUTE_NAME, adminService.session(admin));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);

    MDC.put(LogFilter.ADMIN_MDC_KEY, admin.getEmailAddress());
    csrf.setToken(request, response);
    
    return AdminUtil.view(admin);
  }
}

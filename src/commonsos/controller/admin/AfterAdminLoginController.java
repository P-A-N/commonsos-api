package commonsos.controller.admin;

import static commonsos.controller.admin.auth.AdminLoginController.ADMIN_SESSION_ATTRIBUTE_NAME;

import javax.inject.Inject;

import commonsos.controller.AbstractController;
import commonsos.exception.AuthenticationException;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.session.AdminSession;
import spark.Request;
import spark.Response;

public abstract class AfterAdminLoginController extends AbstractController {

  @Inject AdminService adminService;

  @Override
  public Object handle(Request request, Response response) {
    if (request.session().attribute(ADMIN_SESSION_ATTRIBUTE_NAME) == null) throw new AuthenticationException();
    
    AdminSession session = request.session().attribute(ADMIN_SESSION_ATTRIBUTE_NAME);
    Admin admin = adminService.getAdmin(session.getAdminId());
    return handleAfterLogin(admin, request, response);
  }

  abstract protected Object handleAfterLogin(Admin admin, Request request, Response response);
}

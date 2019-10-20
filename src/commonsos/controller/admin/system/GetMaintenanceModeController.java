package commonsos.controller.admin.system;

import javax.inject.Inject;

import commonsos.Cache;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Role;
import spark.Request;
import spark.Response;

public class GetMaintenanceModeController extends AfterAdminLoginController {

  @Inject Cache cache;

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    if (!admin.getRole().equals(Role.NCL)) throw new ForbiddenException();
    return cache.getSystemConfig(Cache.SYS_CONFIG_KEY_MAINTENANCE_MODE);
  }
}

package commonsos.controller.admin.system;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.Cache;
import commonsos.command.admin.UpdateMaintenanceModeCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Role;
import spark.Request;
import spark.Response;

public class UpdateMaintenanceModeController extends AfterAdminLoginController {

  @Inject Gson gson;
  @Inject Cache cache;

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    UpdateMaintenanceModeCommand command = gson.fromJson(request.body(), UpdateMaintenanceModeCommand.class);
    if (command == null || (
           !command.getMaintenanceMode().toUpperCase().equals("TRUE")
        && !command.getMaintenanceMode().toUpperCase().equals("FALSE"))
        ) throw DisplayableException.getRequiredException("maintenanceMode");
    String value = command.getMaintenanceMode();
    
    if (!admin.getRole().equals(Role.NCL)) throw new ForbiddenException();
    
    cache.setSystemConfig(Cache.SYS_CONFIG_KEY_MAINTENANCE_MODE, value);
    
    return cache.getSystemConfig(Cache.SYS_CONFIG_KEY_MAINTENANCE_MODE);
  }
}

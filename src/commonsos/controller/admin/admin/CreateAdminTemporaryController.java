package commonsos.controller.admin.admin;

import static commonsos.annotation.SyncObject.ADMIN_EMAIL_ADDRESS;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import commonsos.annotation.Synchronized;
import commonsos.controller.admin.MultipartFormdataController;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.service.command.CreateAdminTemporaryCommand;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

@Synchronized(ADMIN_EMAIL_ADDRESS)
public class CreateAdminTemporaryController extends MultipartFormdataController {

  @Inject AdminService adminService;

  @Override
  protected Object handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response) {
    CreateAdminTemporaryCommand command = getCommand(fileItemMap);
    
    try {
      command.setUploadPhotoCommand(getUploadPhotoCommand(fileItemMap, "photo"));
      adminService.createAdminTemporary(admin, command);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      deleteTmpFiles(command.getUploadPhotoCommand());
    }
    
    return null;
  }
  
  private CreateAdminTemporaryCommand getCommand(Map<String, List<FileItem>> fileItemMap) {
    return new CreateAdminTemporaryCommand()
        .setAdminname(RequestUtil.getFileItemString(fileItemMap, "adminname", true))
        .setPassword(RequestUtil.getFileItemString(fileItemMap, "password", true))
        .setCommunityId(RequestUtil.getFileItemLong(fileItemMap, "communityId", false))
        .setRoleId(RequestUtil.getFileItemLong(fileItemMap, "roleId", true))
        .setEmailAddress(RequestUtil.getFileItemString(fileItemMap, "emailAddress", true))
        .setTelNo(RequestUtil.getFileItemString(fileItemMap, "telNo", false))
        .setDepartment(RequestUtil.getFileItemString(fileItemMap, "department", false));
  }
}

package commonsos.controller.admin.admin;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import commonsos.command.UploadPhotoCommand;
import commonsos.controller.admin.MultipartFormdataController;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.Admin;
import commonsos.service.AdminService;
import commonsos.util.RequestUtil;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;

public class UpdateAdminPhotoController extends MultipartFormdataController {

  @Inject AdminService adminService;

  @Override
  protected Object handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response) {
    Long adminId = RequestUtil.getPathParamLong(request, "id");
    UploadPhotoCommand command = null;
    Admin targetAdmin = null;
    
    try {
      command = getUploadPhotoCommand(fileItemMap, "photo");
      targetAdmin = adminService.updateAdminPhoto(admin, adminId, command);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      deleteTmpFiles(command);
    }
    
    UrlView view = new UrlView().setUrl(targetAdmin.getPhotoUrl());
    return view;
  }
}

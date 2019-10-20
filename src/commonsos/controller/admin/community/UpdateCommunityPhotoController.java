package commonsos.controller.admin.community;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import commonsos.command.UploadPhotoCommand;
import commonsos.controller.admin.MultipartFormdataController;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;

public class UpdateCommunityPhotoController extends MultipartFormdataController {

  @Inject CommunityService communityService;
  
  @Override
  protected CommunityView handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response) {
    Long communityId = RequestUtil.getPathParamLong(request, "id");
    
    UploadPhotoCommand command = null;
    Community community;
    try {
      command = getUploadPhotoCommand(fileItemMap, "photo");
      community = communityService.updatePhoto(admin, command, communityId);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      deleteTmpFiles(command);
    }
    
    return communityService.viewForAdmin(community);
  }
}

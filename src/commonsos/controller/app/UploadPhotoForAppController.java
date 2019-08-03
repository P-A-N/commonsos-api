package commonsos.controller.app;

import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import commonsos.exception.BadRequestException;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.User;
import commonsos.service.command.UploadPhotoCommand;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public abstract class UploadPhotoForAppController extends AfterAppLoginController {

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    Map<String, List<FileItem>> fileItemMap = RequestUtil.getFileItemMap(request);
    if (!fileItemMap.containsKey("photo")) {
      throw new BadRequestException("photo is required");
    }
    
    UploadPhotoCommand command = null;
    Object result = null;
    try {
      command = getUploadPhotoCommand(fileItemMap);
      
      result = handleUploadPhoto(user, command, request, response);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException("fail to upload file", e);
    } finally {
      deleteTmpFiles(command);
    }
    
    // delete fileItems
    fileItemMap.values().forEach(fileItems -> fileItems.forEach(fileItem -> fileItem.delete()));
    
    return result;
  }
  
  abstract protected Object handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response);
}

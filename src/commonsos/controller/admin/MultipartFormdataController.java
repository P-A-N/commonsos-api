package commonsos.controller.admin;

import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import commonsos.repository.entity.Admin;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public abstract class MultipartFormdataController extends AfterAdminLoginController {

  @Override
  protected Object handleAfterLogin(Admin admin, Request request, Response response) {
    Map<String, List<FileItem>> fileItemMap = RequestUtil.getFileItemMap(request);
    
    Object result = null;
    try {
      result = handleMultipartFormdata(admin, fileItemMap, request, response);
    } finally {
      // delete fileItems
      fileItemMap.values().forEach(fileItems -> fileItems.forEach(fileItem -> fileItem.delete()));
    }
    
    return result;
  }

  abstract protected Object handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response);
}

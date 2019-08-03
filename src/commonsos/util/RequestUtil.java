package commonsos.util;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.ServerErrorException;
import spark.Request;

public class RequestUtil {

  private RequestUtil() {}
  
  public static Long getQueryParamLong(Request request, String param, boolean isRequired) {
    String value = request.queryParams(param);
    
    if (isEmpty(value)) {
      if (!isRequired) {
        return null;
      } else {
        throw new BadRequestException(String.format("%s is required", param));
      }
    }
    
    if (NumberUtils.isParsable(value)) {
      return parseLong(value);
    } else {
      throw new BadRequestException(String.format("invalid %s", param));
    }
  }
  
  public static Map<String, List<FileItem>> getFileItemMap(Request request) {
    ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
    try {
      return fileUpload.parseParameterMap(request.raw());
    } catch (FileUploadException e) {
      throw new ServerErrorException("fail to read multipart/form-data stream.", e);
    }
  }
}

package commonsos.util;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.WalletType;
import spark.Request;

public class RequestUtil {

  private RequestUtil() {}

  public static String getPathParamString(Request request, String param) {
    String value = request.params(param);
    
    if (isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    }
    
    return value;
  }

  public static Long getPathParamLong(Request request, String param) {
    String value = request.params(param);
    
    if (isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    }
    
    if (NumberUtils.isParsable(value)) {
      return parseLong(value);
    } else {
      throw new BadRequestException(String.format("invalid %s", param));
    }
  }

  public static String getQueryParamString(Request request, String param, boolean isRequired) {
    String value = request.queryParams(param);
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    return value;
  }

  public static Long getQueryParamLong(Request request, String param, boolean isRequired) {
    String value = request.queryParams(param);
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    if (NumberUtils.isParsable(value)) {
      return parseLong(value);
    } else {
      throw new BadRequestException(String.format("invalid %s", param));
    }
  }

  public static LocalDate getQueryParamLocalDate(Request request, String param, boolean isRequired) {
    String value = request.queryParams(param);
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException e) {
      throw new BadRequestException(String.format("invalid %s", param), e);
    }
  }

  public static WalletType getQueryParamWallet(Request request, String param, boolean isRequired) {
    String value = request.queryParams(param);
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    WalletType walletType = WalletType.of(value);
    if (walletType != null) {
      return walletType;
    } else {
      throw new BadRequestException(String.format("invalid %s", param));
    }
  }

  public static String getFileItemString(Map<String, List<FileItem>> fileItemMap, String param, boolean isRequired) {
    String value = null;
    if (fileItemMap.containsKey(param)) {
      value = fileItemMap.get(param).get(0).getString();
    }
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    return value;
  }
  
  public static Long getFileItemLong(Map<String, List<FileItem>> fileItemMap, String param, boolean isRequired) {
    String value = null;
    if (fileItemMap.containsKey(param)) {
      value = fileItemMap.get(param).get(0).getString();
    }
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    if (NumberUtils.isParsable(value)) {
      return parseLong(value);
    } else {
      throw new BadRequestException(String.format("invalid %s [value=%s]", param, value));
    }
  }
  
  public static BigDecimal getFileItemBigDecimal(Map<String, List<FileItem>> fileItemMap, String param, boolean isRequired) {
    String value = null;
    if (fileItemMap.containsKey(param)) {
      value = fileItemMap.get(param).get(0).getString();
    }
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    if (NumberUtils.isParsable(value)) {
      return new BigDecimal(value);
    } else {
      throw new BadRequestException(String.format("invalid %s [value=%s]", param, value));
    }
  }

  public static List<Long> getFileItemLongList(Map<String, List<FileItem>> fileItemMap, String param, boolean isRequired) {
    String value = null;
    if (fileItemMap.containsKey(param)) {
      value = fileItemMap.get(param).get(0).getString();
    }
    
    if (isRequired && isEmpty(value)) {
      throw new BadRequestException(String.format("%s is required", param));
    } else if (isEmpty(value)) {
      return null;
    }
    
    String[] valueArray = value.split(",");
    List<Long> result = new ArrayList<>();
    for (String val : valueArray) {
      if (!isEmpty(val.trim())) {
        if (NumberUtils.isParsable(val.trim())) {
          result.add(parseLong(val.trim()));
        } else {
          throw new BadRequestException(String.format("invalid %s [value=%s]", param, value));
        }
      }
    }
    
    return result;
  }
  public static Map<String, List<FileItem>> getFileItemMap(Request request) {
    DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
    fileItemFactory.setDefaultCharset("UTF-8");
    ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
    fileUpload.setHeaderEncoding("UTF-8");
    try {
      return fileUpload.parseParameterMap(request.raw());
    } catch (FileUploadException e) {
      throw new ServerErrorException("fail to read multipart/form-data stream.", e);
    }
  }
}

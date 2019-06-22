package commonsos.util;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
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
}

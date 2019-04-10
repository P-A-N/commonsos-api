package commonsos.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.SortType;
import commonsos.service.command.PaginationCommand;
import commonsos.view.PaginationView;
import spark.Request;

public class PaginationUtil {

  private PaginationUtil() {}
  
  public static PaginationCommand getCommand(Request request) {
    String pageStr = request.queryParams("pagination[page]");
    String sizeStr = request.queryParams("pagination[size]");
    String sortStr = request.queryParams("pagination[sort]");
    
    if (StringUtils.isNotEmpty(pageStr)) {
      if(!NumberUtils.isParsable(pageStr)) throw new BadRequestException("invalid page number");
      if(StringUtils.isEmpty(sizeStr)) throw new BadRequestException("page size is required");
      if(!NumberUtils.isParsable(sizeStr)) throw new BadRequestException("invalid page size");
      Long page = Long.parseLong(pageStr);
      Long size = Long.parseLong(sizeStr);
      SortType sort = SortType.of(sortStr, SortType.ASC);
      return new PaginationCommand()
          .setPage(page)
          .setSize(size)
          .setSort(sort);
    }
    return new PaginationCommand();
  }
  
  public static PaginationView toView(PaginationCommand command) {
    return new PaginationView()
        .setPage(command.getPage())
        .setSize(command.getSize())
        .setSort(command.getSort())
        .setLastPage(20L);
  }
}

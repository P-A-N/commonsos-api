package commonsos.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.ResultList;
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
      int page = Integer.parseInt(pageStr);
      int size = Integer.parseInt(sizeStr);
      SortType sort = SortType.of(sortStr, SortType.ASC);
      
      PaginationCommand command = new PaginationCommand()
          .setPage(page)
          .setSize(size)
          .setSort(sort);
      validateCommand(command);
      
      return command;
    } else {
      return null;
    }
  }
  
  public static <V> PaginationView toView(ResultList<V> result) {
    if (result.getPage() == null) {
      return new PaginationView(); 
    }
    
    return new PaginationView()
        .setPage(result.getPage())
        .setSize(result.getSize())
        .setSort(result.getSort())
        .setLastPage(result.getLastPage());
  }
  
  public static void validateCommand(PaginationCommand command) {
    if (command.getPage() < 0) throw new BadRequestException(String.format("page can't be less than 0. [page=%d]", command.getPage()));
    if (command.getSize() <= 0) throw new BadRequestException(String.format("size can't be less than 1. [size=%d]", command.getSize()));
    if (command.getSort() == null) throw new BadRequestException("sort can't be null");
  }
}

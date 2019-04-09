package commonsos.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.SortType;
import commonsos.service.command.PagenationCommand;
import commonsos.view.PagenationView;
import spark.Request;

public class PagenationUtil {

  private PagenationUtil() {}
  
  public static PagenationCommand getCommand(Request request) {
    String pageStr = request.queryParams("pagenation.page");
    String sizeStr = request.queryParams("pagenation.size");
    String sortStr = request.queryParams("pagenation.sort");
    
    if (StringUtils.isNotEmpty(pageStr)) {
      if(!NumberUtils.isParsable(pageStr)) throw new BadRequestException("invalid page number");
      if(StringUtils.isEmpty(sizeStr)) throw new BadRequestException("page size is required");
      if(!NumberUtils.isParsable(sizeStr)) throw new BadRequestException("invalid page size");
      Long page = Long.parseLong(pageStr);
      Long size = Long.parseLong(sizeStr);
      SortType sort = SortType.of(sortStr, SortType.ASC);
      return new PagenationCommand()
          .setPage(page)
          .setSize(size)
          .setSort(sort);
    }
    return new PagenationCommand();
  }
  
  public static PagenationView toView(PagenationCommand command) {
    return new PagenationView()
        .setPage(command.getPage())
        .setSize(command.getSize())
        .setSort(command.getSort());
  }
}

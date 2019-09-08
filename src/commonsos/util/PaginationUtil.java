package commonsos.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.command.PaginationCommand;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.SortType;
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
  
  public static <V> PaginationView toView(List<V> list, PaginationCommand command) {
    if (command == null) {
      return new PaginationView(); 
    }
    
    int lastPage = 0;
    if (!list.isEmpty()) {
      lastPage = (list.size() - 1) / command.getSize();
    }
    
    return new PaginationView()
        .setPage(command.getPage())
        .setSize(command.getSize())
        .setSort(command.getSort())
        .setLastPage(lastPage);
  }
  
  public static void validateCommand(PaginationCommand command) {
    if (command.getPage() < 0) throw new BadRequestException(String.format("page can't be less than 0. [page=%d]", command.getPage()));
    if (command.getSize() <= 0) throw new BadRequestException(String.format("size can't be less than 1. [size=%d]", command.getSize()));
    if (command.getSort() == null) throw new BadRequestException("sort can't be null");
  }
  
  public static <V> List<V> pagination(List<V> list, PaginationCommand pagination) {
    if (pagination == null) return list;
    
    switch (pagination.getSort()) {
    case DESC:
      return paginationDesc(list, pagination);
    case ASC:
    default:
      return paginationAsc(list, pagination);
    }
  }
  
  private static <V> List<V> paginationAsc(List<V> list, PaginationCommand pagination) {
    List<V> newList = new ArrayList<>();
    int start = pagination.getPage() * pagination.getSize();
    int end = start + pagination.getSize();
    for (int i = start; i < end; i++) {
      if (i < list.size()) newList.add(list.get(i));
      else break;
    }
    return newList;
  }
  
  private static <V> List<V> paginationDesc(List<V> list, PaginationCommand pagination) {
    List<V> newList = new ArrayList<>();
    int start = (list.size() - 1) - (pagination.getPage() * pagination.getSize());
    int end = start - pagination.getSize();
    for (int i = start; end < i; i--) {
      if (0 <= i) newList.add(list.get(i));
      else break;
    }
    return newList;
  }
}

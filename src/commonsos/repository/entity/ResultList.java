package commonsos.repository.entity;

import java.util.List;

import commonsos.controller.command.PaginationCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class ResultList<T> {
  private List<T> list;
  private Integer page;
  private Integer size;
  private SortType sort;
  private Integer lastPage;
  
  public ResultList() {}
  
  public ResultList(PaginationCommand command) {
    if (command != null) {
      this.page = command.getPage();
      this.size = command.getSize();
      this.sort = command.getSort();
    }
  }
}

package commonsos.view;

import commonsos.repository.entity.SortType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PaginationView {
  private Integer page;
  private Integer size;
  private SortType sort;
  private Integer lastPage;
}

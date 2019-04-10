package commonsos.view;

import commonsos.repository.entity.SortType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PaginationView {
  private Long page;
  private Long size;
  private SortType sort;
  private Long lastPage;
}

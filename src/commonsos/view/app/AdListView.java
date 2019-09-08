package commonsos.view.app;

import java.util.List;

import commonsos.view.CommonView;
import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdListView extends CommonView {
  private List<AdView> adList;
  private PaginationView pagination;
}

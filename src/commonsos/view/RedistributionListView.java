package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class RedistributionListView extends CommonView {
  private List<RedistributionView> redistributionList;
  private PaginationView pagination;
}

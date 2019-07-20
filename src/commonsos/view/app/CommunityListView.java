package commonsos.view.app;

import java.util.List;

import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityListView {
  private List<CommunityView> communityList;
  private PaginationView pagination;
}

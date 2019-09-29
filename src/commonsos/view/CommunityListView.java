package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityListView extends CommonView {
  private List<CommunityView> communityList;
  private PaginationView pagination;
}

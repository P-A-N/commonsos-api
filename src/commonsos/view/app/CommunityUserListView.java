package commonsos.view.app;

import java.util.List;

import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserListView {
  private List<CommunityUserView> communityList;
  private PaginationView pagination;
}

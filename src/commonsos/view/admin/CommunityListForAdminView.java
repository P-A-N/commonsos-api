package commonsos.view.admin;

import java.util.List;

import commonsos.view.CommonView;
import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityListForAdminView extends CommonView {
  private List<CommunityForAdminView> communityList;
  private PaginationView pagination;
}

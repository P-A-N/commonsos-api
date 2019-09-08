package commonsos.view.admin;

import java.util.List;

import commonsos.view.CommonView;
import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UserListForAdminView extends CommonView {
  private List<UserForAdminView> userList;
  private PaginationView pagination;
}
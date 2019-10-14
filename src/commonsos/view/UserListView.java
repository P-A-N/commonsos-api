package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UserListView extends CommonView {
  private List<UserView> userList;
  private PaginationView pagination;
}
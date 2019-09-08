package commonsos.view.app;

import java.util.List;

import commonsos.view.CommonView;
import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageThreadListView extends CommonView {
  private List<MessageThreadView> messageThreadList;
  private PaginationView pagination;
}

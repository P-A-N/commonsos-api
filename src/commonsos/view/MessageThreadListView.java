package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageThreadListView extends CommonView {
  private List<MessageThreadView> messageThreadList;
  private PaginationView pagination;
}

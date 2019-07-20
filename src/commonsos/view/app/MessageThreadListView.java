package commonsos.view.app;

import java.util.List;

import commonsos.view.PaginationView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class MessageThreadListView {
  private List<MessageThreadView> messageThreadList;
  private PaginationView pagination;
}

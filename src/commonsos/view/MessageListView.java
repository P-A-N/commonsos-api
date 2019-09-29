package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageListView extends CommonView {
  private List<MessageView> messageList;
  private PaginationView pagination;
}

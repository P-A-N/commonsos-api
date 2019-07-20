package commonsos.view.app;

import java.util.List;

import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionListView {
  private List<TransactionView> transactionList;
  private PaginationView pagination;
}

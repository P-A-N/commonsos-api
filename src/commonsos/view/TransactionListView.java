package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionListView extends CommonView {
  private List<TransactionView> transactionList;
  private PaginationView pagination;
}

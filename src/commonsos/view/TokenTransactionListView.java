package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TokenTransactionListView extends CommonView {
  private List<TokenTransactionView> transactionList;
  private PaginationView pagination;
}

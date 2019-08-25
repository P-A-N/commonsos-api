package commonsos.view.admin;

import java.util.List;

import commonsos.view.PaginationView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionListForAdminView {
  private List<TransactionForAdminView> transactionList;
  private PaginationView pagination;
}

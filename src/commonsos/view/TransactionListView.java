package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionListView {
  private List<TransactionView> transactionList;
  private PagenationView pagenation;
}

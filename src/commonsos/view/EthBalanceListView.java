package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class EthBalanceListView extends CommonView {
  private List<EthBalanceView> balanceList;
  private PaginationView pagination;
}

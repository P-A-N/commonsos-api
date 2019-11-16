package commonsos.command.admin;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class SearchEthBalanceHistoriesCommand {
  private Long communityId;
  private LocalDate from;
  private LocalDate to;
}

package commonsos.service.command;

import java.math.BigDecimal;

import commonsos.repository.entity.AdType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdUpdateCommand {
  private Long id;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private AdType type;
}

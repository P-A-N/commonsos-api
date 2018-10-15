package commonsos.service.view;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UserPrivateView {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String username;
  private String description;
  private BigDecimal balance;
  private String location;
  private String avatarUrl;
  private String emailAddress;
}
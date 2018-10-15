package commonsos.repository.community;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import commonsos.repository.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity @Table(name = "communities")
@Getter @Setter @Accessors(chain=true)
public class Community {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  String name;
  String tokenContractAddress;
  @OneToOne
  @JoinColumn(name = "admin_user_id")
  private User adminUser;
}

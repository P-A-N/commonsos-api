package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "roles")
@Getter @Setter @Accessors(chain=true) @ToString
public class Role extends AbstractEntity {
  public final static Role NCL = new Role().setId(1L);
  public final static Role COMMUNITY_ADMIN = new Role().setId(2L);
  public final static Role TELLER = new Role().setId(3L);
  public final static Role[] ROLES = {NCL, COMMUNITY_ADMIN, TELLER};
  
  public static Role of(Long id) {
    for (Role role : Role.ROLES) {
      if (role.getId().equals(id)) return role;
    }
    return null;
  }
  
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String rolename;
}

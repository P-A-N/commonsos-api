package commonsos.repository.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "roles")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
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
  
  @Id private Long id;
  private String rolename;
}

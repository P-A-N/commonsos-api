package commonsos.service.httprequest.body;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class WordpressRequestBody {
  private String username;
  private String email;
  private String name;
  private String password;
  private String roles;
  private Long community_id;
}

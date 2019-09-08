package commonsos.view;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UrlView extends CommonView {
  private String url;
}
package commonsos.service.command;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageThreadPhotoUpdateCommand {
  private Long threadId;
  private InputStream photo;
}

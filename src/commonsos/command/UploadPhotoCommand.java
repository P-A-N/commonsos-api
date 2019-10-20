package commonsos.command;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UploadPhotoCommand {
  private File photoFile;
  private File cropedPhotoFile;
  private Integer width;
  private Integer height;
  private Integer x;
  private Integer y;
}

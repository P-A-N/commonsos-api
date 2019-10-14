package commonsos.service.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.inject.Singleton;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.ImageCommand;
import org.im4java.process.OutputConsumer;

import commonsos.exception.ServerErrorException;
import commonsos.service.AbstractService;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ImageService extends AbstractService {

  public ImageType getImageType(File image) {
    // prepare
    IdentifyCmd cmd = new IdentifyCmd();
    cmd.setAsyncMode(false);
    
    IdentifyOutputConsumer consumer = new IdentifyOutputConsumer();
    cmd.setOutputConsumer(consumer);
    cmd.setErrorConsumer(e -> {});
    
    IMOperation op = new IMOperation();
    op.format("%m");
    op.addImage(image.getAbsolutePath());
    
    // execute command
    try {
      cmd.run(op);
    } catch (Exception e) {
      log.info(String.format("fail to execute command. [%s]", getCommandLine(cmd, op)), e);
    }
    
    // get result
    String cmdResult = consumer.getResult();
    ImageType imageType = ImageType.valueOf(cmdResult, false);
    
    return imageType;
  }
  
  public File crop(File image, int width, int height, int x, int y) throws IOException {
    // prepare
    File cropedPhotoFile = Files.createTempFile(String.format("%s_%s_", "commonsos" ,Thread.currentThread().getName()), null).toFile();
    
    ConvertCmd cmd = new ConvertCmd();
    cmd.setAsyncMode(false);
    cmd.setOutputConsumer(o -> {});
    cmd.setErrorConsumer(e -> {});
    
    IMOperation op = new IMOperation();
    op.addImage(image.getAbsolutePath());
    op.crop(width, height, x, y);
    op.addImage(cropedPhotoFile.getAbsolutePath());
    
    // execute command
    try {
      cmd.run(op);
    } catch (Exception e) {
      cropedPhotoFile.delete();
      log.info(String.format("deleted temporary file(cropedPhotoFile). [%s]", cropedPhotoFile.getAbsolutePath()));
      throw new ServerErrorException(String.format("fail to execute command. [%s]", getCommandLine(cmd, op)), e);
    }
    
    return cropedPhotoFile;
  }
  
  private class IdentifyOutputConsumer implements OutputConsumer {
    
    private String result;
    
    @Override
    public void consumeOutput(InputStream in) throws IOException {
      try(InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);) {
        result = br.readLine();
      }
    }
    
    public String getResult() {
      return result;
    }
  }
  
  private String getCommandLine(ImageCommand cmd, IMOperation op) {
    StringBuilder sb = new StringBuilder();
    
    for (String command : cmd.getCommand()) {
      sb.append(command).append(' ');
    }
    
    sb.append(op.toString());
    
    return sb.toString();
  }
}

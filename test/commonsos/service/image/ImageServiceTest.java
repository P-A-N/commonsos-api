package commonsos.service.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.exception.ServerErrorException;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {
  
  @InjectMocks @Spy private ImageService imageService;

  @Test
  public void getImageType_JPEG() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    ImageType result = imageService.getImageType(file);
    
    // verify
    assertThat(result).isEqualTo(ImageType.JPEG);
  }
  
  @Test
  public void getImageType_PNG() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.png");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    ImageType result = imageService.getImageType(file);
    
    // verify
    assertThat(result).isEqualTo(ImageType.PNG);
  }
  
  @Test
  public void getImageType_SVG() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.svg");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    ImageType result = imageService.getImageType(file);

    // verify
    assertThat(result).isNull();
  }
  
  @Test
  public void getImageType_TXT() throws URISyntaxException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.txt");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    ImageType result = imageService.getImageType(file);

    // verify
    assertThat(result).isNull();
  }

  @Test
  public void crop_JPEG() throws URISyntaxException, IOException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.jpg");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    File cropedFile = imageService.crop(file, 850, 2000, 1800, 700);
    
    // verify
    BufferedImage bufferedImage = ImageIO.read(cropedFile);
    assertThat(bufferedImage.getWidth()).isEqualTo(850);
    assertThat(bufferedImage.getHeight()).isEqualTo(2000);
    
    // clean up
    cropedFile.delete();
  }

  @Test
  public void crop_PNG() throws URISyntaxException, IOException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.png");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    File cropedFile = imageService.crop(file, 210, 90, 150, 280);
    
    // verify
    BufferedImage bufferedImage = ImageIO.read(cropedFile);
    assertThat(bufferedImage.getWidth()).isEqualTo(210);
    assertThat(bufferedImage.getHeight()).isEqualTo(90);
    
    // clean up
    cropedFile.delete();
  }

  @Test
  public void crop_SVG() throws URISyntaxException, IOException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.svg");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    File cropedFile = imageService.crop(file, 70, 400, 80, 160);

    // clean up
    cropedFile.delete();
  }

  @Test
  public void crop_TXT() throws URISyntaxException, IOException {
    // prepare
    URL url = this.getClass().getResource("/images/testImage.txt");
    URI uri = url.toURI();
    File file = new File(uri);

    // execute
    assertThrows(ServerErrorException.class, () -> imageService.crop(file, 70, 400, 80, 160));
  }
}
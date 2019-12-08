package commonsos.service.image;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.s3.AmazonS3;

@ExtendWith(MockitoExtension.class)
public class ImageUploadServiceTest {
  
  @Mock private AmazonS3 s3client;
  @InjectMocks private ImageUploadService imageUploadService;

  @Test
  public void delete() throws URISyntaxException {
    // execute & verify
    imageUploadService.delete("https://hogehoge.s3.amazonaws.com/test");
    verify(s3client, times(1)).deleteObject(nullable(String.class), eq("test"));
    
    // execute & verify
    imageUploadService.delete("https://hogehoge.s3.amazonaws.com/test/test2");
    verify(s3client, times(1)).deleteObject(nullable(String.class), eq("test/test2"));
  }
  
}
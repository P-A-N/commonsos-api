package commonsos.service.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import commonsos.service.command.UploadPhotoCommand;

@Singleton
public class ImageUploadService {

  private AWSCredentials credentials;
  private AmazonS3 s3client;
  private String bucketName;

  @Inject Configuration config;

  @Inject void init() {
    bucketName = config.awsS3BucketName();
    credentials = new BasicAWSCredentials(config.awsAccessKey(), config.awsSecurityKey());
    s3client = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .withRegion(Regions.AP_NORTHEAST_1)
      .withForceGlobalBucketAccessEnabled(true)
      .build();
  }

  public String create(InputStream inputStream) {
    String filename = UUID.randomUUID().toString();
    s3client.putObject(new PutObjectRequest(bucketName, filename, inputStream, null)
      .withMetadata(metadata())
      .withCannedAcl(CannedAccessControlList.PublicRead));
    return "https://" + bucketName + ".s3.amazonaws.com/" + filename;
  }
  
  public String create(File file) {
    try (FileInputStream in = new FileInputStream(file)) {
      return create(in);
    } catch (Exception e) {
      throw new ServerErrorException(e);
    }
  }
  
  public String create(UploadPhotoCommand command) {
    if (command.getCropedPhotoFile() != null) {
      return create(command.getCropedPhotoFile());
    }
    
    return create(command.getPhotoFile());
  }

  private ObjectMetadata metadata() {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType("image");
    objectMetadata.setCacheControl("public, max-age=3600000");
    return objectMetadata;
  }

  public void delete(String url) {
    if (StringUtils.isNotBlank(url)) {
      s3client.deleteObject(bucketName, url.substring(url.lastIndexOf('/') + 1));
    }
  }
}

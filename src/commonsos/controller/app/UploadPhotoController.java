package commonsos.controller.app;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.CommonsOSException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.User;
import commonsos.service.command.UploadPhotoCommand;
import commonsos.service.image.ImageService;
import commonsos.service.image.ImageType;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

@Slf4j
public abstract class UploadPhotoController extends AfterLoginController {

  @Inject private ImageService imageService;
  private Random random = new Random();

  @Override public Object handleAfterLogin(User user, Request request, Response response) {
    Map<String, List<FileItem>> fileItemMap = getFileItemMap(request);
    if (!fileItemMap.containsKey("photo")) {
      throw new BadRequestException("photo is required");
    }
    
    // extract width, height, x, y
    UploadPhotoCommand command = getCommand(fileItemMap);
    
    // extract photo input stream and process the handler.
    File photoFile = null;
    File cropedPhotoFile = null;
    Object result = null;
    try {
      photoFile = getPhotoFile(fileItemMap);
      ImageType imageType = imageService.getImageType(photoFile);
      if (imageType == null) throw new DisplayableException("error.imageType_not_supported.");
      log.info(String.format("imageType=%s", imageType.name()));
      
      cropedPhotoFile = getCropedPhotoFile(photoFile, command);
      command.setPhotoFile(photoFile);
      command.setCropedPhotoFile(cropedPhotoFile);
      
      result = handleUploadPhoto(user, command, request, response);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException("fail to upload file", e);
    } finally {
      if (photoFile != null) {
        photoFile.delete();
        log.info(String.format("deleted temporary file(photoFile). [%s]", photoFile.getAbsolutePath()));
      }
      if (cropedPhotoFile != null) {
        cropedPhotoFile.delete();
        log.info(String.format("deleted temporary file(cropedPhotoFile). [%s]", photoFile.getAbsolutePath()));
      }
    }
    
    // delete fileItems
    fileItemMap.values().forEach(fileItems -> fileItems.forEach(fileItem -> fileItem.delete()));
    
    return result;
  }
  
  abstract protected Object handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response);
  
  private Map<String, List<FileItem>> getFileItemMap(Request request) {
    ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
    try {
      return fileUpload.parseParameterMap(request.raw());
    } catch (FileUploadException e) {
      throw new ServerErrorException("fail to read multipart/form-data stream.", e);
    }
  }
  
  private UploadPhotoCommand getCommand(Map<String, List<FileItem>> fileItemMap) {
    UploadPhotoCommand command = new UploadPhotoCommand();
    if (fileItemMap.containsKey("width")) {
      String width = fileItemMap.get("width").get(0).getString();
      if (!NumberUtils.isParsable(width)) throw new BadRequestException("invalid width");
      command.setWidth(Integer.parseInt(width));
    }
    if (fileItemMap.containsKey("height")) {
      String height = fileItemMap.get("height").get(0).getString();
      if (!NumberUtils.isParsable(height)) throw new BadRequestException("invalid height");
      command.setHeight(Integer.parseInt(height));
    }
    if (fileItemMap.containsKey("x")) {
      String x = fileItemMap.get("x").get(0).getString();
      if (!NumberUtils.isParsable(x)) throw new BadRequestException("invalid x");
      command.setX(Integer.parseInt(x));
    }
    if (fileItemMap.containsKey("y")) {
      String y = fileItemMap.get("y").get(0).getString();
      if (!NumberUtils.isParsable(y)) throw new BadRequestException("invalid y");
      command.setY(Integer.parseInt(y));
    }
    
    return command;
  }
  
  private File getPhotoFile(Map<String, List<FileItem>> fileItemMap) throws Exception {
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    if (!tmpDirPath.endsWith(File.separator)) tmpDirPath = tmpDirPath + File.separator;
    
    String filePath = String.format("%s%s_%s_%d.tmp",
        tmpDirPath,
        "commonsos",
        Thread.currentThread().getName(),
        random.nextInt(Integer.MAX_VALUE));
    File photoFile = new File(filePath);
    fileItemMap.get("photo").get(0).write(photoFile);

    log.info(String.format("created temporary file(photoFile). [%s]", photoFile.getAbsolutePath()));
    
    return photoFile;
  }

  
  private File getCropedPhotoFile(File photoFile, UploadPhotoCommand command) throws Exception {
    if (command.getWidth() != null
        && command.getHeight() != null
        && command.getX() != null
        && command.getY() != null) {
      log.info(String.format("start cropping"));
      File cropedPhotoFile = imageService.crop(photoFile, command.getWidth(), command.getHeight(), command.getX(), command.getY());

      log.info(String.format("created temporary file(cropedPhotoFile). [%s]", cropedPhotoFile.getAbsolutePath()));

      return cropedPhotoFile;
    }
    
    return null;
  }
}

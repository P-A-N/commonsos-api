package commonsos.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.command.app.UploadPhotoCommand;
import commonsos.exception.BadRequestException;
import commonsos.service.image.ImageService;
import commonsos.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import spark.Route;

@Slf4j
public abstract class AbstractController implements Route {
  
  @Inject private ImageService imageService;
  @Inject private Random random;

  protected UploadPhotoCommand getUploadPhotoCommand(Map<String, List<FileItem>> fileItemMap) throws Exception {
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

    File photoFile = getPhotoFile(fileItemMap, "photo");
    if (photoFile != null) {
      ValidateUtil.validateImageType(imageService.getImageType(photoFile));
    }

    File cropedPhotoFile = getCropedPhotoFile(photoFile, command);
    command.setPhotoFile(photoFile);
    command.setCropedPhotoFile(cropedPhotoFile);
    return command;
  }
  
  protected UploadPhotoCommand getUploadPhotoCommand(Map<String, List<FileItem>> fileItemMap, String paramName) throws Exception {
    String widthKey = String.format("%s[%s]", paramName,"width");
    String heightKey = String.format("%s[%s]", paramName,"height");
    String xKey = String.format("%s[%s]", paramName,"x");
    String yKey = String.format("%s[%s]", paramName,"y");
    
    UploadPhotoCommand command = new UploadPhotoCommand();
    if (fileItemMap.containsKey(widthKey)) {
      String width = fileItemMap.get(widthKey).get(0).getString();
      if (!NumberUtils.isParsable(width)) throw new BadRequestException("invalid width");
      command.setWidth(Integer.parseInt(width));
    }
    if (fileItemMap.containsKey(heightKey)) {
      String height = fileItemMap.get(heightKey).get(0).getString();
      if (!NumberUtils.isParsable(height)) throw new BadRequestException("invalid height");
      command.setHeight(Integer.parseInt(height));
    }
    if (fileItemMap.containsKey(xKey)) {
      String x = fileItemMap.get(xKey).get(0).getString();
      if (!NumberUtils.isParsable(x)) throw new BadRequestException("invalid x");
      command.setX(Integer.parseInt(x));
    }
    if (fileItemMap.containsKey(yKey)) {
      String y = fileItemMap.get(yKey).get(0).getString();
      if (!NumberUtils.isParsable(y)) throw new BadRequestException("invalid y");
      command.setY(Integer.parseInt(y));
    }

    File photoFile = getPhotoFile(fileItemMap, paramName);
    if (photoFile != null) {
      ValidateUtil.validateImageType(imageService.getImageType(photoFile));
    }

    File cropedPhotoFile = getCropedPhotoFile(photoFile, command);
    command.setPhotoFile(photoFile);
    command.setCropedPhotoFile(cropedPhotoFile);
    return command;
  }
  
  protected void deleteTmpFiles(UploadPhotoCommand... commands) {
    if (commands == null) return;
    for (int i = 0; i < commands.length; i++) {
      if (commands[i] == null) continue;
      if (commands[i].getPhotoFile() != null) {
        commands[i].getPhotoFile().delete();
        log.info(String.format("deleted temporary file(photoFile). [%s]", commands[i].getPhotoFile().getAbsolutePath()));
      }
      if (commands[i].getCropedPhotoFile() != null) {
        commands[i].getCropedPhotoFile().delete();
        log.info(String.format("deleted temporary file(photoFile). [%s]", commands[i].getCropedPhotoFile().getAbsolutePath()));
      }
    }
  }

  private File getPhotoFile(Map<String, List<FileItem>> fileItemMap, String paramName) throws Exception {
    if (!fileItemMap.containsKey(paramName)) return null;
    
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    if (!tmpDirPath.endsWith(File.separator)) tmpDirPath = tmpDirPath + File.separator;
    
    String filePath = String.format("%s%s_%s_%d.tmp",
        tmpDirPath,
        "commonsos",
        Thread.currentThread().getName(),
        random.nextInt(Integer.MAX_VALUE));
    File photoFile = new File(filePath);
    fileItemMap.get(paramName).get(0).write(photoFile);

    log.info(String.format("created temporary file(photoFile). [%s]", photoFile.getAbsolutePath()));
    
    return photoFile;
  }

  private File getCropedPhotoFile(File photoFile, UploadPhotoCommand command) throws Exception {
    if (photoFile != null
        && command != null
        && command.getWidth() != null
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

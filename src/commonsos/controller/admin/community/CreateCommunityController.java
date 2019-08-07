package commonsos.controller.admin.community;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import commonsos.controller.admin.MultipartFormdataController;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.service.command.CreateCommunityCommand;
import commonsos.util.RequestUtil;
import spark.Request;
import spark.Response;

public class CreateCommunityController extends MultipartFormdataController {

  @Inject CommunityService communityService;

  @Override
  protected Object handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response) {
    CreateCommunityCommand command = getCommand(fileItemMap);
    
    Community community;
    try {
      command.setUploadPhotoCommand(getUploadPhotoCommand(fileItemMap, "photo"));
      command.setUploadCoverPhotoCommand(getUploadPhotoCommand(fileItemMap, "coverPhoto"));
      community = communityService.createCommunity(admin, command);
    } catch (CommonsOSException e) {
      throw e;
    } catch (Exception e) {
      throw new ServerErrorException(e);
    } finally {
      deleteTmpFiles(command.getUploadPhotoCommand(), command.getUploadCoverPhotoCommand());
    }
    
    return communityService.viewForAdmin(community, command.getAdminIdList());
  }
  
  private CreateCommunityCommand getCommand(Map<String, List<FileItem>> fileItemMap) {
    return new CreateCommunityCommand()
        .setCommunityName(RequestUtil.getFileItemString(fileItemMap, "communityName", true))
        .setTokenName(RequestUtil.getFileItemString(fileItemMap, "tokenName", true))
        .setTokenSymbol(RequestUtil.getFileItemString(fileItemMap, "tokenSymbol", true))
        .setTransactionFee(RequestUtil.getFileItemDouble(fileItemMap, "transactionFee", false))
        .setDescription(RequestUtil.getFileItemString(fileItemMap, "description", false))
        .setAdminIdList(RequestUtil.getFileItemLongList(fileItemMap, "adminIdList", false));
  }
}

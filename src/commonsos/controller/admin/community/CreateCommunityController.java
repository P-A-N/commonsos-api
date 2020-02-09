package commonsos.controller.admin.community;

import static commonsos.annotation.SyncObject.COMMUNITY;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import commonsos.annotation.Synchronized;
import commonsos.command.admin.CreateCommunityCommand;
import commonsos.controller.admin.MultipartFormdataController;
import commonsos.exception.CommonsOSException;
import commonsos.exception.ServerErrorException;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.service.CommunityService;
import commonsos.util.RequestUtil;
import commonsos.view.CommunityView;
import spark.Request;
import spark.Response;

@Synchronized(COMMUNITY)
public class CreateCommunityController extends MultipartFormdataController {

  @Inject CommunityService communityService;

  @Override
  protected CommunityView handleMultipartFormdata(Admin admin, Map<String, List<FileItem>> fileItemMap, Request request, Response response) {
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
    
    return communityService.viewForAdmin(community);
  }
  
  private CreateCommunityCommand getCommand(Map<String, List<FileItem>> fileItemMap) {
    return new CreateCommunityCommand()
        .setCommunityName(RequestUtil.getFileItemString(fileItemMap, "communityName", true))
        .setTokenName(RequestUtil.getFileItemString(fileItemMap, "tokenName", true))
        .setTokenSymbol(RequestUtil.getFileItemString(fileItemMap, "tokenSymbol", true))
        .setWordpressAccountId(RequestUtil.getFileItemString(fileItemMap, "wordpressAccountId", true))
        .setWordpressAccountEmailAddress(RequestUtil.getFileItemString(fileItemMap, "wordpressAccountEmailAddress", true))
        .setTransactionFee(RequestUtil.getFileItemBigDecimal(fileItemMap, "transactionFee", false))
        .setDescription(RequestUtil.getFileItemString(fileItemMap, "description", false))
        .setAdminIdList(RequestUtil.getFileItemLongList(fileItemMap, "adminIdList", false));
  }
}

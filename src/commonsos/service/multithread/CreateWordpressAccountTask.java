package commonsos.service.multithread;

import javax.inject.Inject;

import commonsos.command.admin.CreateCommunityCommand;
import commonsos.service.httprequest.WordpressRequestService;

public class CreateWordpressAccountTask extends AbstractTask {

  @Inject WordpressRequestService wordpressRequestService;

  private final Long communityId;
  private final CreateCommunityCommand command;

  public CreateWordpressAccountTask(Long communityId, CreateCommunityCommand command) {
    this.communityId = communityId;
    this.command = command;
  }

  @Override
  public void runTask() {
    wordpressRequestService.sendCreateWordPressAccount(communityId, command.getWordpressAccountId(), command.getWordpressAccountEmailAddress(), command.getCommunityName());
  }
  
  @Override
  public String toString() {
    return String.format("%s, communityId=%d", this.getClass().getSimpleName(), communityId);
  }
}

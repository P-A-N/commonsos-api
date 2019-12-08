package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeleteServiceTest {

  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock MessageRepository messageRepository;
  @Mock ImageUploadService imageService;
  @InjectMocks @Spy DeleteService service;

  @BeforeEach
  public void setup() {
    ResultList<MessageThread> mtResult = new ResultList<>();
    mtResult.setList(asList(new MessageThread()));
    when(messageThreadRepository.searchByAdId(any(), any())).thenReturn(mtResult);
  }
  
  @Test
  public void deleteAdByUser() {
    // prepare
    User user = new User().setId(id("user"));
    Ad ad1 = new Ad().setCreatedUserId(id("user"));
    Ad ad2 = new Ad().setCreatedUserId(id("othreUser"));
    
    // execute (checking error)
    service.deleteAdByUser(user, ad1);
    assertThrows(ForbiddenException.class, () -> service.deleteAdByUser(user, ad2));
  }
  
  @Test
  public void deleteAdByAdmin() {
    // prepare
    Community community = new Community().setId(id("community"));
    User user = new User().setId(id("user")).setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    User admin = new User().setId(id("admin")).setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    community.setAdminUser(admin);
    Ad ad = new Ad().setCreatedUserId(id("othreUser")).setCommunityId(id("community"));
    
    // execute (checking error)
    service.deleteAdByAdminUser(admin, ad);
    assertThrows(ForbiddenException.class, () -> service.deleteAdByAdminUser(user, ad));
  }

  @Test
  public void deleteMessageThreadParty_allow() {
    // prepare
    User user = new User().setId(id("user"));
    MessageThread mt = new MessageThread().setParties(asList(new MessageThreadParty().setUser(user)));
    when(messageThreadRepository.findStrictById(any())).thenReturn(mt);
    
    // execute
    service.deleteMessageThreadParty(user, mt.getId());
  }

  @Test
  public void deleteMessageThreadParty_not_member() {
    // prepare
    User user = new User().setId(id("user"));
    User otherUser = new User().setId(id("otherUser"));
    MessageThread mt = new MessageThread().setParties(asList(new MessageThreadParty().setUser(otherUser)));
    when(messageThreadRepository.findStrictById(any())).thenReturn(mt);
    
    // execute
    assertThrows(BadRequestException.class, () -> service.deleteMessageThreadParty(user, mt.getId()));
  }
}
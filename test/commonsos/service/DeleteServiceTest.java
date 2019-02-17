package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DeleteServiceTest {

  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock MessageRepository messageRepository;
  @Mock ImageUploadService imageService;
  @InjectMocks @Spy DeleteService service;

  @Test
  public void deleteAd_allow() {
    // prepare
    User user = new User().setId(id("user"));
    Ad ad = new Ad().setCreatedBy(id("user"));
    
    // execute
    service.deleteAd(user, ad);
  }

  @Test(expected = ForbiddenException.class)
  public void deleteAd_forbidden() {
    // prepare
    User user = new User().setId(id("user"));
    Ad ad = new Ad().setCreatedBy(id("othreUser"));

    // execute
    service.deleteAd(user, ad);
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

  @Test(expected = BadRequestException.class)
  public void deleteMessageThreadParty_not_member() {
    // prepare
    User user = new User().setId(id("user"));
    User otherUser = new User().setId(id("otherUser"));
    MessageThread mt = new MessageThread().setParties(asList(new MessageThreadParty().setUser(otherUser)));
    when(messageThreadRepository.findStrictById(any())).thenReturn(mt);
    
    // execute
    service.deleteMessageThreadParty(user, mt.getId());
  }
}
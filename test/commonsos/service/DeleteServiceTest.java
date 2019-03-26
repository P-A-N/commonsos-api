package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
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

  @Test
  public void deleteAd_forbidden() {
    // prepare
    User user = new User().setId(id("user"));
    Ad ad = new Ad().setCreatedBy(id("othreUser"));

    // execute
    assertThrows(ForbiddenException.class, () -> service.deleteAd(user, ad));
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
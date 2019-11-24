package commonsos.service;

import static commonsos.TestId.id;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.command.app.UpdateAdCommand;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;

@ExtendWith(MockitoExtension.class)
public class AdServiceTest {

  @Mock AdRepository adRepository;
  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock TokenTransactionRepository transactionRepository;
  @Mock ImageUploadService imageService;
  @Captor ArgumentCaptor<Ad> adCaptor;
  @InjectMocks @Spy AdService service;

  @Test
  public void updateAd() {
    User user = new User().setId(id("creator"));
    doReturn(new Ad().setCreatedUserId(id("creator"))).when(service).getAd(any());
    when(transactionRepository.hasPaid(any())).thenReturn(false);
    service.updateAd(user, new UpdateAdCommand().setTitle("test").setPoints(ONE).setType(AdType.GIVE));
  }

  @Test
  public void updateAd_otherUser() {
    User user = new User().setId(id("user"));
    doReturn(new Ad().setCreatedUserId(id("creator"))).when(service).getAd(any());
    
    assertThrows(ForbiddenException.class, () -> service.updateAd(user, new UpdateAdCommand().setTitle("test").setPoints(ONE).setType(AdType.GIVE)));
  }

  @Test
  public void updateAd_hasPaid() {
    User user = new User().setId(id("creator"));
    doReturn(new Ad().setCreatedUserId(id("creator"))).when(service).getAd(any());
    when(transactionRepository.hasPaid(any())).thenReturn(true);

    assertThrows(DisplayableException.class, () -> service.updateAd(user, new UpdateAdCommand().setTitle("test").setPoints(ONE).setType(AdType.GIVE)));
  }

  @Test
  public void updatePhoto_requiresCreatorUser() {
    User user = new User().setId(id("other user id"));
    Ad ad = new Ad().setCreatedUserId(id("creator user id"));
    when(adRepository.findStrictById(any())).thenReturn(ad);

    assertThrows(ForbiddenException.class, () -> service.updatePhoto(user, null, id("ad id")));
  }
}
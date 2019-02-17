package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;
import commonsos.service.command.AdCreateCommand;
import commonsos.service.command.AdUpdateCommand;
import commonsos.service.image.ImageUploadService;
import commonsos.view.AdView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AdServiceTest {

  @Mock AdRepository adRepository;
  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock TransactionRepository transactionRepository;
  @Mock ImageUploadService imageService;
  @Captor ArgumentCaptor<Ad> adCaptor;
  @InjectMocks @Spy AdService service;

  @Test
  public void create_isMemberOfCommunity() {
    when(adRepository.create(any())).thenReturn(new Ad());
    doReturn(new AdView()).when(service).view(any(Ad.class), any(User.class));

    service.create(
        new User().setCommunityList(asList(new Community().setId(id("community1")))),
        new AdCreateCommand().setCommunityId(id("community1")));

    verify(adRepository, times(1)).create(any());
  }

  @Test(expected = ForbiddenException.class)
  public void create_isNotMemberOfCommunity() {
    service.create(
        new User().setCommunityList(asList(new Community().setId(id("community1")))),
        new AdCreateCommand().setCommunityId(id("community2")));
  }

  @Test
  public void listFor_filer_not_null() {
    service.listFor(new User(), id("community"), "filter");
    verify(adRepository, times(1)).ads(any(), any());
    verify(adRepository, never()).ads(any());
  }

  @Test
  public void listFor_filer_null() {
    service.listFor(new User(), id("community"), null);
    verify(adRepository, never()).ads(any(), any());
    verify(adRepository, times(1)).ads(any());
  }

  @Test
  public void updateAd() {
    User user = new User().setId(id("creator"));
    doReturn(new Ad().setCreatedBy(id("creator"))).when(service).ad(any());
    when(transactionRepository.hasPaid(any())).thenReturn(false);
    service.updateAd(user, new AdUpdateCommand());
  }

  @Test(expected = ForbiddenException.class)
  public void updateAd_otherUser() {
    User user = new User().setId(id("user"));
    doReturn(new Ad().setCreatedBy(id("creator"))).when(service).ad(any());
    service.updateAd(user, new AdUpdateCommand());
  }

  @Test(expected = BadRequestException.class)
  public void updateAd_hasPaid() {
    User user = new User().setId(id("creator"));
    doReturn(new Ad().setCreatedBy(id("creator"))).when(service).ad(any());
    when(transactionRepository.hasPaid(any())).thenReturn(true);
    service.updateAd(user, new AdUpdateCommand());
  }

  @Test(expected = ForbiddenException.class)
  public void updatePhoto_requiresCreatorUser() {
    User user = new User().setId(id("other user id"));
    Ad ad = new Ad().setCreatedBy(id("creator user id"));
    when(adRepository.findStrict(any())).thenReturn(ad);

    service.updatePhoto(user, null, id("ad id"));
  }
}
package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Optional;

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
import commonsos.service.AdService;
import commonsos.service.command.AdCreateCommand;
import commonsos.service.command.AdPhotoUpdateCommand;
import commonsos.service.command.AdUpdateCommand;
import commonsos.service.image.ImageService;
import commonsos.view.AdView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AdServiceTest {

  @Mock AdRepository adRepository;
  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock TransactionRepository transactionRepository;
  @Mock ImageService imageService;
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
  
  @Test
  public void updatePhoto() {
    User user = new User().setId(id("creator id"));
    InputStream photo = mock(InputStream.class);
    when(imageService.create(photo)).thenReturn("/url");
    Ad ad = new Ad().setCreatedBy(id("creator id")).setPhotoUrl("/old");
    when(adRepository.find(id("ad id"))).thenReturn(Optional.of(ad));

    String result = service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")).setPhoto(photo));

    assertThat(result).isEqualTo("/url");
    assertThat(ad.getPhotoUrl()).isEqualTo("/url");
    verify(imageService).delete("/old");
    verify(adRepository).update(ad);
  }

 @Test
  public void updatePhoto_adWithoutPhoto() {
    User user = new User().setId(id("creator id"));
    InputStream photo = mock(InputStream.class);
    when(imageService.create(photo)).thenReturn("/url");
    Ad ad = new Ad().setCreatedBy(id("creator id")).setPhotoUrl(null);
    when(adRepository.find(id("ad id"))).thenReturn(Optional.of(ad));

    String result = service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")).setPhoto(photo));

    assertThat(result).isEqualTo("/url");
    assertThat(ad.getPhotoUrl()).isEqualTo("/url");
    verify(imageService, never()).delete(any());
    verify(adRepository).update(ad);
  }

  @Test(expected = ForbiddenException.class)
  public void updatePhoto_requiresCreatorUser() {
    User user = new User().setId(id("other user id"));
    Ad ad = new Ad().setCreatedBy(id("creator user id"));
    when(adRepository.find(id("ad id"))).thenReturn(Optional.of(ad));

    service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")));
  }

  @Test(expected = BadRequestException.class)
  public void updatePhoto_adNotFound() {
    when(adRepository.find(id("ad id"))).thenReturn(empty());

    service.updatePhoto(new User(), new AdPhotoUpdateCommand().setAdId(id("ad id")));
  }

  @Test
  public void deleteAdLogically() {
    // prepare
    User operator = new User().setId(id("operator"));
    Ad targetAd = new Ad().setCreatedBy(id("operator"));
    when(adRepository.update(targetAd)).thenReturn(targetAd);
    
    // execute
    Ad result = service.deleteAdLogically(targetAd, operator);
    
    // verify
    assertThat(result).isEqualTo(targetAd);
    assertThat(result.isDeleted()).isEqualTo(true);
  }

  @Test(expected = ForbiddenException.class)
  public void deleteAdLogically_forbidden() {
    // prepare
    User operator = new User().setId(id("operator"));
    Ad targetAd = new Ad().setCreatedBy(id("otherUser"));
    
    // execute
    service.deleteAdLogically(targetAd, operator);
  }
}
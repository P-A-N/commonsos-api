package commonsos.service.ad;

import static commonsos.TestId.id;
import static commonsos.repository.ad.AdType.GIVE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
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

import commonsos.ForbiddenException;
import commonsos.exception.BadRequestException;
import commonsos.exception.UserNotFoundException;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdPhotoUpdateCommand;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.ad.AdType;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.ImageService;
import commonsos.util.UserUtil;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AdServiceTest {

  @Mock AdRepository adRepository;
  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock TransactionRepository transactionRepository;
  @Mock UserUtil userUtil;
  @Mock ImageService imageService;
  @Captor ArgumentCaptor<Ad> adCaptor;
  @InjectMocks @Spy AdService service;

  @Test
  public void create_isMemberOfCommunity() {
    when(userUtil.isMember(any(User.class), any(Long.class))).thenReturn(true);
    when(adRepository.create(any())).thenReturn(new Ad());
    doReturn(new AdView()).when(service).view(any(Ad.class), any(User.class));
    service.create(new User(), new AdCreateCommand().setCommunityId(1L));
    verify(adRepository, times(1)).create(any());
  }

  @Test(expected = ForbiddenException.class)
  public void create_isNotMemberOfCommunity() {
    when(userUtil.isMember(any(), any(Long.class))).thenReturn(false);
    service.create(new User(), new AdCreateCommand());
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

  @Test(expected = UserNotFoundException.class)
  public void view_user_not_found() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    service.view(new Ad(), new User());
  }

  @Test
  public void view_user_found() {
    when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
    doReturn(false).when(service).isOwnAd(any(), any());
    doReturn(false).when(service).isPayableByUser(any(), any());
    service.view(new Ad(), new User());
  }

  @Test
  public void isOwn() {
    Ad ad = new Ad().setCreatedBy(id("worker"));

    assertThat(service.isOwnAd(new User().setId(id("worker")), ad)).isTrue();
    assertThat(service.isOwnAd(new User().setId(id("stranger")), ad)).isFalse();
  }

  @Test
  public void isPayable() {
    User me = new User().setId(id("me"));
    User otherUser = new User().setId(id("other"));

    Ad buyAd = new Ad().setCreatedBy(id("other")).setType(AdType.WANT).setPoints(ONE);
    Ad sellAd = new Ad().setCreatedBy(id("other")).setType(GIVE).setPoints(ONE);
    Ad sellAdWithZeroPrice = new Ad().setCreatedBy(id("other")).setType(GIVE).setPoints(ZERO);

    assertThat(service.isPayableByUser(me, sellAd)).isTrue();
    assertThat(service.isPayableByUser(otherUser, buyAd)).isTrue();

    assertThat(service.isPayableByUser(me, sellAdWithZeroPrice)).isFalse();
    assertThat(service.isPayableByUser(me, buyAd)).isFalse();
    assertThat(service.isPayableByUser(otherUser, sellAd)).isFalse();
    assertThat(service.isPayableByUser(me, buyAd)).isFalse();
  }

  @Test
  public void ad() {
    Ad ad = new Ad();
    when(adRepository.find(id("ad id"))).thenReturn(Optional.of(ad));

    assertThat(service.ad(id("ad id"))).isEqualTo(ad);
  }

  @Test(expected=BadRequestException.class)
  public void ad_notFound() {
    when(adRepository.find(id("ad id"))).thenReturn(empty());

    service.ad(id("ad id"));
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
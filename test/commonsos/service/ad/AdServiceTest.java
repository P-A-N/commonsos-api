package commonsos.service.ad;

import static commonsos.TestId.id;
import static commonsos.repository.ad.AdType.GIVE;
import static commonsos.repository.ad.AdType.WANT;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.BadRequestException;
import commonsos.ForbiddenException;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdPhotoUpdateCommand;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.ad.AdType;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.service.ImageService;
import commonsos.service.user.UserService;
import commonsos.service.view.UserView;

@RunWith(MockitoJUnitRunner.class)
public class AdServiceTest {

  @Mock AdRepository repository;
  @Mock UserService userService;
  @Mock ImageService imageService;
  @Mock TransactionRepository transactionRepository;
  @Captor ArgumentCaptor<Ad> adCaptor;
  @InjectMocks @Spy AdService service;

  @Test
  public void create() {
    AdCreateCommand command = new AdCreateCommand()
      .setTitle("title")
      .setDescription("description")
      .setPoints(TEN)
      .setLocation("location")
      .setType(WANT)
      .setPhotoUrl("url://photo");
    User user = new User().setId(id("user id")).setCommunityId(id("community id"));
    Ad createdAd = new Ad();
    when(repository.create(adCaptor.capture())).thenReturn(createdAd);
    AdView adView = new AdView();
    doReturn(adView).when(service).view(createdAd, user);

    AdView result = service.create(user, command);

    assertThat(result).isEqualTo(adView);
    Ad ad = adCaptor.getValue();
    assertThat(ad.getCreatedBy()).isEqualTo(id("user id"));
    assertThat(ad.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
    assertThat(ad.getTitle()).isEqualTo("title");
    assertThat(ad.getDescription()).isEqualTo("description");
    assertThat(ad.getPoints()).isEqualTo(TEN);
    assertThat(ad.getLocation()).isEqualTo("location");
    assertThat(ad.getType()).isEqualTo(WANT);
    assertThat(ad.getPhotoUrl()).isEqualTo("url://photo");
    assertThat(ad.getCommunityId()).isEqualTo(id("community id"));
  }

  @Test
  public void listForUser() {
    Ad ad = new Ad();
    AdView view = new AdView();
    User user = new User();
    AdService service = spy(this.service);
    when(repository.ads(user.getCommunityId())).thenReturn(asList(ad));
    doReturn(view).when(service).view(ad, user);

    List<AdView> result = service.listFor(user, null);

    assertThat(result).containsExactly(view);
  }

  @Test
  public void listForUser_filtered() {
    Ad ad = new Ad();
    AdView view = new AdView();
    User user = new User();
    AdService service = spy(this.service);
    when(repository.ads(user.getCommunityId(), "filter text")).thenReturn(asList(ad));
    doReturn(view).when(service).view(ad, user);

    List<AdView> result = service.listFor(user, "filter text");

    assertThat(result).containsExactly(view);
  }

  @Test
  public void myAdsView() {
    User user = new User().setId(id("worker")).setCommunityId(id("community"));
    Ad ad = new Ad().setCreatedBy(id("worker"));
    AdView adView = new AdView();
    when(repository.ads(id("community"))).thenReturn(asList(ad, new Ad().setCreatedBy(id("elderly"))));
    doReturn(adView).when(service).view(ad, user);

    List<AdView> result = service.myAdsView(user);

    assertThat(result.size()).isEqualTo(1);
    assertThat(result).containsExactly(adView);
  }

  @Test
  public void myAds() {
    User user = new User().setId(id("worker")).setCommunityId(id("community"));
    Ad ad = new Ad().setCreatedBy(id("worker"));
    when(repository.ads(id("community"))).thenReturn(asList(ad, new Ad().setCreatedBy(id("elderly"))));

    List<Ad> result = service.myAds(user);

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getCreatedBy()).isEqualTo(id("worker"));
  }

  @Test
  public void view() {
    Instant createdAt = now();
    Ad ad = new Ad()
      .setPoints(TEN)
      .setLocation("home")
      .setDescription("description")
      .setCreatedBy(id("worker"))
      .setId(11L)
      .setTitle("title")
      .setCreatedAt(createdAt)
      .setPhotoUrl("photo url")
      .setType(WANT);
    UserView userView = new UserView();
    when(userService.view(id("worker"))).thenReturn(userView);

    AdView view = service.view(ad, new User().setId(id("worker")));

    assertThat(view.getCreatedBy()).isEqualTo(userView);
    assertThat(view.getId()).isEqualTo(11L);
    assertThat(view.getDescription()).isEqualTo("description");
    assertThat(view.getLocation()).isEqualTo("home");
    assertThat(view.getPoints()).isEqualTo(TEN);
    assertThat(view.getTitle()).isEqualTo("title");
    assertThat(view.isOwn()).isEqualTo(true);
    assertThat(view.isPayable()).isEqualTo(true);
    assertThat(view.getCreatedAt()).isEqualTo(createdAt);
    assertThat(view.getPhotoUrl()).isEqualTo("photo url");
    assertThat(view.getType()).isEqualTo(WANT);
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
    when(repository.find(id("ad id"))).thenReturn(Optional.of(ad));

    assertThat(service.ad(id("ad id"))).isEqualTo(ad);
  }

  @Test(expected=BadRequestException.class)
  public void ad_notFound() {
    when(repository.find(id("ad id"))).thenReturn(empty());

    service.ad(id("ad id"));
  }

  @Test
  public void viewById() {
    User user = new User();
    AdView adView = new AdView();
    Ad ad = new Ad();
    doReturn(adView).when(service).view(ad, user);
    doReturn(ad).when(service).ad(id("ad id"));

    AdView result = service.view(user, id("ad id"));

    assertThat(result).isEqualTo(adView);
  }

  @Test
  public void updateAd() {
    // prepare
    Ad targetAd = new Ad()
        .setId(id("targetAd"))
        .setCreatedBy(id("operator"));
    doReturn(targetAd).when(service).ad(id("targetAd"));
    when(transactionRepository.hasPaid(targetAd)).thenReturn(false);
    when(repository.update(targetAd)).thenReturn(targetAd);
    
    // execute
    User operator = new User().setId(id("operator"));
    AdUpdateCommand command = new AdUpdateCommand()
        .setId(id("targetAd"))
        .setTitle("title")
        .setDescription("description")
        .setPoints(TEN)
        .setLocation("location")
        .setType(AdType.GIVE);
    Ad result = service.updateAd(operator, command);
    
    // verify
    assertThat(result).isEqualTo(targetAd);
    assertThat(result.getTitle()).isEqualTo(command.getTitle());
    assertThat(result.getDescription()).isEqualTo(command.getDescription());
    assertThat(result.getPoints()).isEqualTo(command.getPoints());
    assertThat(result.getLocation()).isEqualTo(command.getLocation());
    assertThat(result.getType()).isEqualTo(command.getType());
  }

  @Test(expected = ForbiddenException.class)
  public void updateAd_otherUser() {
    // prepare
    Ad targetAd = new Ad().setCreatedBy(id("otherUser"));
    doReturn(targetAd).when(service).ad(any());
    
    // execute
    User operator = new User().setId(id("operator"));
    service.updateAd(operator, new AdUpdateCommand());
  }

  @Test(expected = BadRequestException.class)
  public void updateAd_hasPaid() {
    // prepare
    Ad targetAd = new Ad().setCreatedBy(id("operator"));
    doReturn(targetAd).when(service).ad(any());
    when(transactionRepository.hasPaid(any())).thenReturn(true);
    
    // execute
    User operator = new User().setId(id("operator"));
    service.updateAd(operator, new AdUpdateCommand());
  }
  
  @Test
  public void updatePhoto() {
    User user = new User().setId(id("creator id"));
    InputStream photo = mock(InputStream.class);
    when(imageService.create(photo)).thenReturn("/url");
    Ad ad = new Ad().setCreatedBy(id("creator id")).setPhotoUrl("/old");
    when(repository.find(id("ad id"))).thenReturn(Optional.of(ad));

    String result = service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")).setPhoto(photo));

    assertThat(result).isEqualTo("/url");
    assertThat(ad.getPhotoUrl()).isEqualTo("/url");
    verify(imageService).delete("/old");
    verify(repository).update(ad);
  }

 @Test
  public void updatePhoto_adWithoutPhoto() {
    User user = new User().setId(id("creator id"));
    InputStream photo = mock(InputStream.class);
    when(imageService.create(photo)).thenReturn("/url");
    Ad ad = new Ad().setCreatedBy(id("creator id")).setPhotoUrl(null);
    when(repository.find(id("ad id"))).thenReturn(Optional.of(ad));

    String result = service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")).setPhoto(photo));

    assertThat(result).isEqualTo("/url");
    assertThat(ad.getPhotoUrl()).isEqualTo("/url");
    verify(imageService, never()).delete(any());
    verify(repository).update(ad);
  }

  @Test(expected = ForbiddenException.class)
  public void updatePhoto_requiresCreatorUser() {
    User user = new User().setId(id("other user id"));
    Ad ad = new Ad().setCreatedBy(id("creator user id"));
    when(repository.find(id("ad id"))).thenReturn(Optional.of(ad));

    service.updatePhoto(user, new AdPhotoUpdateCommand().setAdId(id("ad id")));
  }

  @Test(expected = BadRequestException.class)
  public void updatePhoto_adNotFound() {
    when(repository.find(id("ad id"))).thenReturn(empty());

    service.updatePhoto(new User(), new AdPhotoUpdateCommand().setAdId(id("ad id")));
  }
  
  @Test
  public void deleteAdLogically_byId() {
    // prepare
    User operator = new User();
    Ad targetAd = new Ad();
    doReturn(targetAd).when(service).ad(id("ad"));
    doReturn(targetAd).when(service).deleteAdLogically(targetAd, operator);
    
    // execute
    Ad result = service.deleteAdLogically(id("ad"), operator);
    
    // verify
    assertThat(result).isEqualTo(targetAd);
  }

  @Test
  public void deleteAdLogically() {
    // prepare
    User operator = new User().setId(id("operator"));
    Ad targetAd = new Ad().setCreatedBy(id("operator"));
    when(repository.update(targetAd)).thenReturn(targetAd);
    
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
package commonsos.domain.ad;

import commonsos.BadRequestException;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserService;
import commonsos.domain.auth.UserView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static commonsos.TestId.id;
import static commonsos.domain.ad.AdType.GIVE;
import static commonsos.domain.ad.AdType.WANT;
import static java.math.BigDecimal.*;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdServiceTest {

  @Mock AdRepository repository;
  @Mock UserService userService;
  @Captor ArgumentCaptor<Ad> adCaptor;
  @InjectMocks @Spy AdService service;

  @Test
  public void create() {
    AdCreateCommand command = new AdCreateCommand()
      .setTitle("title")
      .setDescription("description")
      .setAmount(TEN)
      .setLocation("location")
      .setType(WANT)
      .setPhotoUrl("url://photo");

    service.create(new User().setId(id("user id")).setCommunityId(id("community id")), command);

    verify(repository).create(adCaptor.capture());
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
  public void all() {
    Ad ad = new Ad();
    AdView view = new AdView();
    User user = new User();
    AdService service = spy(this.service);
    when(repository.list()).thenReturn(asList(ad));
    doReturn(view).when(service).view(ad, user);

    List<AdView> result = service.all(user);

    assertThat(result).containsExactly(view);
  }

  @Test
  public void adsByOwner() {
    User user = new User().setId(id("worker"));
    Ad ad = new Ad().setCreatedBy(id("worker"));
    AdView adView = new AdView();
    AdService service = spy(this.service);
    when(repository.list()).thenReturn(asList(ad, new Ad().setCreatedBy(id("elderly"))));
    doReturn(adView).when(service).view(ad, user);

    List<AdView> result = service.adsByOwner(user);

    assertThat(result).containsExactly(adView);
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
    when(repository.find(id("ad id"))).thenReturn(Optional.empty());

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
}
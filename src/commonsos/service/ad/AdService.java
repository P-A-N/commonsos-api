package commonsos.service.ad;

import static commonsos.repository.ad.AdType.GIVE;
import static commonsos.repository.ad.AdType.WANT;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.BadRequestException;
import commonsos.ForbiddenException;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdPhotoUpdateCommand;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.service.ImageService;
import commonsos.service.user.UserService;

@Singleton
public class AdService {
  @Inject AdRepository repository;
  @Inject UserService userService;
  @Inject TransactionRepository transactionRepository;
  @Inject ImageService imageService;

  public AdView create(User user, AdCreateCommand command) {
    Ad ad = new Ad()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setType(command.getType())
      .setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setPoints(command.getAmount())
      .setPhotoUrl(command.getPhotoUrl())
      .setCommunityId(user.getCommunityId());

    return view(repository.create(ad), user);
  }

  public List<AdView> listFor(User user, String filter) {
    List<Ad> ads = filter != null ?
      repository.ads(user.getCommunityId(), filter) :
      repository.ads(user.getCommunityId());

    return ads.stream().map(ad -> view(ad, user)).collect(toList());
  }

  public List<AdView> myAdsView(User user) {
    return myAds(user).stream().map(ad -> view(ad, user)).collect(toList());
  }

  public List<Ad> myAds(User user) {
    return repository.ads(user.getCommunityId()).stream().filter(a -> a.getCreatedBy().equals(user.getId())).collect(toList());
  }

  public AdView view(Ad ad, User user) {
    return new AdView()
      .setId(ad.getId())
      .setCreatedBy(userService.view(ad.getCreatedBy()))
      .setType(ad.getType())
      .setTitle(ad.getTitle())
      .setDescription(ad.getDescription())
      .setPoints(ad.getPoints())
      .setLocation(ad.getLocation())
      .setOwn(isOwnAd(user, ad))
      .setPayable(isPayableByUser(user, ad))
      .setCreatedAt(ad.getCreatedAt())
      .setPhotoUrl(ad.getPhotoUrl());
  }

  public AdView view(User user, Long adId) {
    return view(ad(adId), user);
  }

  boolean isOwnAd(User user, Ad ad) {
    return ad.getCreatedBy().equals(user.getId());
  }

  public boolean isPayableByUser(User user, Ad ad) {
    if (ZERO.compareTo(ad.getPoints()) >= 0) return false;
    if (isOwnAd(user, ad) && WANT == ad.getType()) return true;
    if (!isOwnAd(user, ad) && GIVE == ad.getType()) return true;
    return false;
  }

  public Ad ad(Long id) {
    return repository.find(id).orElseThrow(BadRequestException::new);
  }

  public Ad updateAd(User operator, AdUpdateCommand command) {
    Ad ad = ad(command.getId());
    if (!ad.getCreatedBy().equals(operator.getId())) throw new ForbiddenException();
    if (transactionRepository.hasPaid(ad)) throw new BadRequestException();
    
    ad.setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setPoints(command.getAmount())
      .setLocation(command.getLocation())
      .setType(command.getType());
    return repository.update(ad);
  }

  public String updatePhoto(User user, AdPhotoUpdateCommand command) {
    Ad ad = repository.find(command.getAdId()).orElseThrow(BadRequestException::new);
    if (!ad.getCreatedBy().equals(user.getId())) throw new ForbiddenException();
    String url = imageService.create(command.getPhoto());
    if (ad.getPhotoUrl() != null) {
      imageService.delete(ad.getPhotoUrl());
    }
    ad.setPhotoUrl(url);
    repository.update(ad);
    return url;
  }

  public Ad deleteAdLogically(Long adId, User operator) {
    return deleteAdLogically(ad(adId), operator);
  }

  public Ad deleteAdLogically(Ad ad, User operator) {
    // operator have to be the creator
    if (!ad.getCreatedBy().equals(operator.getId())) throw new ForbiddenException();

    ad.setDeleted(true);
    return repository.update(ad);
  }
}

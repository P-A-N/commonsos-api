package commonsos.service.ad;

import static commonsos.repository.ad.AdType.GIVE;
import static commonsos.repository.ad.AdType.WANT;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.ForbiddenException;
import commonsos.exception.BadRequestException;
import commonsos.exception.UserNotFoundException;
import commonsos.repository.ad.Ad;
import commonsos.repository.ad.AdPhotoUpdateCommand;
import commonsos.repository.ad.AdRepository;
import commonsos.repository.community.Community;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.transaction.TransactionRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.ImageService;
import commonsos.util.UserUtil;

@Singleton
public class AdService {
  @Inject AdRepository adRepository;
  @Inject UserRepository userRepository;
  @Inject CommunityRepository communityRepository;
  @Inject TransactionRepository transactionRepository;
  @Inject UserUtil userUtil;
  @Inject ImageService imageService;

  public AdView create(User user, AdCreateCommand command) {
    if (!userUtil.isMember(user, command.getCommunityId())) throw new ForbiddenException("only a member of community is allow to create ads");

    Ad ad = new Ad()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setType(command.getType())
      .setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setPoints(command.getPoints())
      .setPhotoUrl(command.getPhotoUrl())
      .setCommunityId(command.getCommunityId());

    return view(adRepository.create(ad), user);
  }

  public List<AdView> listFor(User user, Long communityId, String filter) {
    List<Ad> ads = filter != null ?
      adRepository.ads(communityId, filter) :
      adRepository.ads(communityId);

    return ads.stream().map(ad -> view(ad, user)).collect(toList());
  }

  public List<AdView> myAdsView(User user) {
    return myAds(user).stream().map(ad -> view(ad, user)).collect(toList());
  }

  public List<Ad> myAds(User user) {
    return adRepository.myAds(
        user.getJoinedCommunities().stream().map(Community::getId).collect(Collectors.toList()),
        user.getId());
  }

  public AdView view(Ad ad, User user) {
    User createdBy = userRepository.findById(ad.getCreatedBy()).orElseThrow(UserNotFoundException::new);
    
    return new AdView()
      .setId(ad.getId())
      .setCommunityId(ad.getCommunityId())
      .setCreatedBy(userUtil.view(createdBy))
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
    return adRepository.find(id).orElseThrow(BadRequestException::new);
  }

  public Ad updateAd(User operator, AdUpdateCommand command) {
    Ad ad = ad(command.getId());
    if (!ad.getCreatedBy().equals(operator.getId())) throw new ForbiddenException();
    if (transactionRepository.hasPaid(ad)) throw new BadRequestException();
    
    ad.setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setPoints(command.getPoints())
      .setLocation(command.getLocation())
      .setType(command.getType());
    return adRepository.update(ad);
  }

  public String updatePhoto(User user, AdPhotoUpdateCommand command) {
    Ad ad = adRepository.find(command.getAdId()).orElseThrow(BadRequestException::new);
    if (!ad.getCreatedBy().equals(user.getId())) throw new ForbiddenException();
    String url = imageService.create(command.getPhoto());
    if (ad.getPhotoUrl() != null) {
      imageService.delete(ad.getPhotoUrl());
    }
    ad.setPhotoUrl(url);
    adRepository.update(ad);
    return url;
  }

  public Ad deleteAdLogically(Long adId, User operator) {
    return deleteAdLogically(ad(adId), operator);
  }

  public Ad deleteAdLogically(Ad ad, User operator) {
    // operator have to be the creator
    if (!ad.getCreatedBy().equals(operator.getId())) throw new ForbiddenException();

    ad.setDeleted(true);
    return adRepository.update(ad);
  }
}

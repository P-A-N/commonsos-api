package commonsos.service;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.TransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.User;
import commonsos.service.command.AdCreateCommand;
import commonsos.service.command.AdPhotoUpdateCommand;
import commonsos.service.command.AdUpdateCommand;
import commonsos.service.image.ImageService;
import commonsos.util.AdUtil;
import commonsos.util.UserUtil;
import commonsos.view.AdView;

@Singleton
public class AdService {
  @Inject private AdRepository adRepository;
  @Inject private UserRepository userRepository;
  @Inject private TransactionRepository transactionRepository;
  @Inject private DeleteService deleteService;
  @Inject private ImageService imageService;

  public AdView create(User user, AdCreateCommand command) {
    if (!UserUtil.isMember(user, command.getCommunityId())) throw new ForbiddenException("only a member of community is allow to create ads");

    Ad ad = new Ad()
      .setCreatedBy(user.getId())
      .setCreatedAt(now())
      .setType(command.getType())
      .setTitle(command.getTitle())
      .setDescription(command.getDescription())
      .setLocation(command.getLocation())
      .setPoints(command.getPoints())
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
    return adRepository.myAds(user.getId());
  }

  public AdView view(Ad ad, User user) {
    User createdBy = userRepository.findStrictById(ad.getCreatedBy());
    
    return AdUtil.view(ad, createdBy, user);
  }

  public AdView view(User user, Long adId) {
    return view(ad(adId), user);
  }

  public Ad ad(Long id) {
    return adRepository.findStrict(id);
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
    if (StringUtils.isNotBlank(ad.getPhotoUrl())) {
      imageService.delete(ad.getPhotoUrl());
    }
    ad.setPhotoUrl(url);
    adRepository.update(ad);
    return url;
  }

  public void deleteAdLogically(User user, Long adId) {
    deleteService.deleteAd(user, ad(adId));
  }
}

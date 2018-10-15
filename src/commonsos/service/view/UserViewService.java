package commonsos.service.view;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.BadRequestException;
import commonsos.ForbiddenException;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.community.CommunityService;
import commonsos.service.community.CommunityView;
import commonsos.service.transaction.BalanceView;
import commonsos.service.transaction.TransactionService;

@Singleton
public class UserViewService {

  @Inject UserRepository userRepository;
  @Inject CommunityService communityService;
  @Inject CommunityRepository communityRepository;
  @Inject TransactionService transactionService;

  public UserPrivateView privateView(User user) {
    List<BalanceView> balanceList = new ArrayList<>();
    List<CommunityView> communityList = new ArrayList<>();
    if (user.getJoinedCommunities() != null) {
      user.getJoinedCommunities().forEach(c -> {
        balanceList.add(transactionService.balance(user, c.getId()));
        communityList.add(communityService.view(c));
      });
    }
    
    return new UserPrivateView()
      .setId(user.getId())
      .setBalanceList(balanceList)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setCommunityList(communityList)
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress());
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("user not found"));
    boolean isAdmin = user.getJoinedCommunities().stream().anyMatch(c -> {
      return c.getAdminUser() != null && c.getAdminUser().getId().equals(currentUser.getId());
    });
    if (!currentUser.getId().equals(user.getId()) && !isAdmin) throw new ForbiddenException();
    
    // TODO filter balance
    return privateView(user);
  }

  public UserView view(User user) {
    return new UserView()
      .setId(user.getId())
      .setFullName(fullName(user))
      .setUsername(user.getUsername())
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl());
  }

  public UserView view(Long id) {
    return view(userRepository.findById(id).orElseThrow(BadRequestException::new));
  }

  public String fullName(User user) {
    return String.format("%s %s", user.getLastName(), user.getFirstName());
  }
}

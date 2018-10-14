package commonsos.service.view;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.BadRequestException;
import commonsos.ForbiddenException;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.transaction.TransactionService;

@Singleton
public class UserViewService {

  @Inject UserRepository userRepository;
  @Inject TransactionService transactionService;

  public UserPrivateView privateView(User user) {
    BigDecimal balance = transactionService.balance(user, user.getCommunityId());
    return new UserPrivateView()
      .setId(user.getId())
      .setAdmin(user.isAdmin())
      .setBalance(balance)
      .setFullName(fullName(user))
      .setFirstName(user.getFirstName())
      .setLastName(user.getLastName())
      .setUsername(user.getUsername())
      .setLocation(user.getLocation())
      .setDescription(user.getDescription())
      .setAvatarUrl(user.getAvatarUrl())
      .setEmailAddress(user.getEmailAddress());
  }

  public UserPrivateView privateView(User currentUser, Long userId) {
    if (!currentUser.getId().equals(userId) && !currentUser.isAdmin()) throw new ForbiddenException();
    User user = userRepository.findById(userId).orElseThrow(BadRequestException::new);
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

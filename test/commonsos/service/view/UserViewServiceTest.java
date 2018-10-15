package commonsos.service.view;

import static commonsos.TestId.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.BadRequestException;
import commonsos.repository.community.CommunityRepository;
import commonsos.repository.user.User;
import commonsos.repository.user.UserRepository;
import commonsos.service.transaction.TransactionService;

@RunWith(MockitoJUnitRunner.class)
public class UserViewServiceTest {

  @Mock UserRepository userRepository;
  @Mock CommunityRepository communityRepository;
  @Mock TransactionService transactionService;
  @InjectMocks @Spy UserViewService viewService;

  // TODO
  /*@Test
  public void privateView() {
    // prepare
    User user = new User()
        .setId(id("user id"))
        .setFirstName("first")
        .setLastName("last")
        .setUsername("user")
        .setLocation("Shibuya")
        .setDescription("description")
        .setAvatarUrl("/avatar.png")
        .setEmailAddress("test@test.com");
    when(transactionService.balance(user, user.getCommunityId())).thenReturn(new BalanceView().setBalance(BigDecimal.TEN).setCommunityId(id("community")));

    // execute
    UserPrivateView view = viewService.privateView(user);

    // verify
    verify(transactionService, times(1)).balance(user, user.getCommunityId());
    assertThat(view.getId()).isEqualTo(id("user id"));
    assertThat(view.getBalanceList().get(0).getBalance()).isEqualTo(BigDecimal.TEN);
    assertThat(view.getBalanceList().get(0).getCommunityId()).isEqualTo(id("community"));
    assertThat(view.getFullName()).isEqualTo("last first");
    assertThat(view.getFirstName()).isEqualTo("first");
    assertThat(view.getLastName()).isEqualTo("last");
    assertThat(view.getUsername()).isEqualTo("user");
    assertThat(view.getLocation()).isEqualTo("Shibuya");
    assertThat(view.getDescription()).isEqualTo("description");
    assertThat(view.getAvatarUrl()).isEqualTo("/avatar.png");
    assertThat(view.getEmailAddress()).isEqualTo("test@test.com");
  }*/

  // TODO
  /*@Test
  public void privateView_adminAccessesOtherUser() {
    // prepare
    User user = new User().setCommunityId(id("community")).setId(id("otherUSer"));
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(communityRepository.isAdmin(id("currentUser"), id("community"))).thenReturn(true);
    UserPrivateView userView = new UserPrivateView();
    doReturn(userView).when(viewService).privateView(any());
    
    // execute
    User currentUser = new User().setId(id("currentUser"));
    UserPrivateView result = viewService.privateView(currentUser, id("otherUSer"));
    
    // verify
    verify(userRepository, times(1)).findById(id("otherUSer"));
    assertThat(result).isSameAs(userView);
  }*/

  // TODO
  /*@Test
  public void privateView_ownUser() {
    // prepare
    User user = new User().setCommunityId(id("community")).setId(id("currentUser"));
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    UserPrivateView userView = new UserPrivateView();
    doReturn(userView).when(viewService).privateView(any());
    
    // execute
    User currentUser = new User().setId(id("currentUser"));
    UserPrivateView result = viewService.privateView(currentUser, id("currentUser"));
    
    // verify
    verify(userRepository, times(1)).findById(id("currentUser"));
    assertThat(result).isSameAs(userView);
  }*/

  // TODO
  /*@Test(expected = ForbiddenException.class)
  public void privateView_notAdminAccessOtherUser() {
    // prepare
    User user = new User().setCommunityId(id("community")).setId(id("otherUser"));
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(communityRepository.isAdmin(any(), any())).thenReturn(false);
    
    // execute
    User currentUser = new User()
        .setId(id("currentUser"));
    Long userId = id("otherUser");
    viewService.privateView(currentUser, userId);
  }*/

  @Test(expected = BadRequestException.class)
  public void privateView_userNotFound() {
    // prepare
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    
    // execute
    User currentUser = new User()
        .setId(id("currentUser"));
//        .setAdmin(false);
    Long userId = id("currentUser");
    viewService.privateView(currentUser, userId);
  }

  @Test
  public void view() {
    // execute
    User user = new User()
        .setId(id("user"))
        .setFirstName("first")
        .setLastName("last")
        .setUsername("user")
        .setLocation("location")
        .setDescription("description")
        .setAvatarUrl("avatar url");
    UserView result = viewService.view(user);
    
    // verify
    assertThat(result.getId()).isEqualTo(id("user"));
    assertThat(result.getFullName()).isEqualTo("last first");
    assertThat(result.getUsername()).isEqualTo("user");
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getLocation()).isEqualTo("location");
    assertThat(result.getAvatarUrl()).isEqualTo("avatar url");
  }

  @Test
  public void view_byId() {
    // prepare
    User user = new User();
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    UserView userView = new UserView();
    doReturn(userView).when(viewService).view(user);

    // execute
    UserView result = viewService.view(123L);

    // verify
    verify(userRepository, times(1)).findById(123L);
    assertThat(result).isEqualTo(userView);
  }

  @Test(expected = BadRequestException.class)
  public void view_byId_userNotFound() {
    // prepare
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    // execute
    viewService.view(123L);
  }

  @Test
  public void viewByUserId() {
    User user = new User();
    when(userRepository.findById(id("user id"))).thenReturn(Optional.of(user));
    UserView view = new UserView();
    doReturn(view).when(viewService).view(user);

    assertThat(viewService.view(id("user id"))).isEqualTo(view);
  }

  @Test(expected = BadRequestException.class)
  public void viewByUserId_userNotFound() {
    when(userRepository.findById(id("invalid id"))).thenReturn(Optional.empty());

    viewService.view(id("invalid id"));
  }
}
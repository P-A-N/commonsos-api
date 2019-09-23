package commonsos.service;

import static commonsos.TestId.id;
import static commonsos.repository.entity.AdType.WANT;
import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static commonsos.repository.entity.WalletType.MAIN;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import commonsos.command.admin.CreateTokenTransactionFromAdminCommand;
import commonsos.command.app.TransactionCreateCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.CommunityToken;
import commonsos.service.blockchain.TokenBalance;
import commonsos.service.notification.PushNotificationService;
import commonsos.service.sync.SyncService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TokenTransactionServiceTest {

  @Mock TokenTransactionRepository repository;
  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @Mock CommunityRepository communityRepository;
  @Mock MessageThreadRepository messageThreadRepository;
  @Mock MessageRepository messageRepository;
  @Mock BlockchainService blockchainService;
  @Mock BlockchainEventService blockchainEventService;
  @Mock SyncService syncService;
  @Mock PushNotificationService pushNotificationService;
  @Captor ArgumentCaptor<TokenTransaction> captor;
  @InjectMocks @Spy TokenTransactionService service;

  @Test
  public void createTransaction_app() {
    // prepare
    Community community = new Community().setFee(BigDecimal.ONE).setId(id("community"));
    User user = new User().setId(id("user")).setUsername("user").setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    User beneficiary = new User().setId(id("beneficiary")).setUsername("beneficiary").setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    Ad ad = new Ad().setPoints(new BigDecimal("10")).setCommunityId(id("community")).setCreatedUserId(id("user")).setType(WANT);
    TokenBalance tokenBalance = new TokenBalance().setBalance(TEN).setToken(new CommunityToken().setTokenSymbol("sys"));
    when(userRepository.findStrictById(any())).thenReturn(beneficiary);
    when(communityRepository.findPublicStrictById(any())).thenReturn(community);
    when(adRepository.findStrict(any())).thenReturn(ad);
    when(blockchainService.getTokenBalance(any(User.class), any(Long.class))).thenReturn(tokenBalance);
    when(messageThreadRepository.byCreaterAndAdId(any(), any())).thenReturn(Optional.of(new MessageThread().setId(id("messageThread"))));
    
    // community is null
    TransactionCreateCommand command = command("community", "beneficiary", "10", "description", "ad id").setTransactionFee(BigDecimal.ONE);
    command.setCommunityId(null);
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    command.setCommunityId(id("community"));
    
    // negative point
    command.setAmount(new BigDecimal("-1"));
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    command.setAmount(new BigDecimal("10"));
    
    // user is beneficiary
    user.setId(id("beneficiary"));
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    user.setId(id("user"));
    
    // ad belongs to other community
    ad.setCommunityId(id("otherCommunity"));
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    ad.setCommunityId(id("community"));
    
    // user is not a member of community
    user.setCommunityUserList(asList());
    assertThrows(DisplayableException.class, () -> service.create(user, command));
    user.setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    
    // beneficiary is not a member of community
    beneficiary.setCommunityUserList(asList());
    assertThrows(DisplayableException.class, () -> service.create(user, command));
    beneficiary.setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    
    // not enough funds
    tokenBalance.setBalance(new BigDecimal("9"));
    assertThrows(DisplayableException.class, () -> service.create(user, command));
    tokenBalance.setBalance(new BigDecimal("10"));
    
    // nothing to throw
    service.create(user, command);
  }


  @Test
  public void createTransaction_admin() {
    // prepare
    Admin admin = new Admin().setRole(NCL);
    Community community = new Community().setId(id("community")).setStatus(PUBLIC);
    User beneficiary = new User().setId(id("beneficiary")).setUsername("beneficiary").setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    TokenBalance tokenBalance = new TokenBalance().setBalance(TEN).setToken(new CommunityToken().setTokenSymbol("sys"));
    when(userRepository.findStrictById(any())).thenReturn(beneficiary);
    when(communityRepository.findStrictById(any())).thenReturn(community);
    doReturn(tokenBalance).when(service).getTokenBalanceForAdmin(any(), any(), any());
    when(messageThreadRepository.betweenUsers(any(), any(), any())).thenReturn(Optional.of(new MessageThread().setId(id("messageThread"))));

    // community is null
    CreateTokenTransactionFromAdminCommand command = new CreateTokenTransactionFromAdminCommand()
        .setCommunityId(id("communityId"))
        .setAmount(TEN)
        .setWallet(MAIN)
        .setBeneficiaryUserId(id("beneficiaryUserId"));
    command.setCommunityId(null);
    assertThrows(BadRequestException.class, () -> service.create(admin, command));
    command.setCommunityId(id("communityId"));
    
    // amount is null
    command.setAmount(null);
    assertThrows(BadRequestException.class, () -> service.create(admin, command));
    command.setAmount(TEN);
    
    // wallet is null
    command.setWallet(null);
    assertThrows(BadRequestException.class, () -> service.create(admin, command));
    command.setWallet(MAIN);
    
    // beneficiaryUserId is null
    command.setBeneficiaryUserId(null);
    assertThrows(BadRequestException.class, () -> service.create(admin, command));
    command.setBeneficiaryUserId(id("beneficiaryUserId"));
    
    // community is not public
    community.setStatus(PRIVATE);
    assertThrows(DisplayableException.class, () -> service.create(admin, command));
    community.setStatus(PUBLIC);
    
    // admin is forbidden
    admin.setRole(TELLER);
    assertThrows(ForbiddenException.class, () -> service.create(admin, command));
    admin.setRole(NCL);
    
    // negative amount
    command.setAmount(ZERO);
    assertThrows(BadRequestException.class, () -> service.create(admin, command));
    command.setAmount(TEN);
    
    // not enough fund
    tokenBalance.setBalance(TEN.subtract(ONE));
    assertThrows(DisplayableException.class, () -> service.create(admin, command));
    tokenBalance.setBalance(TEN);
    
    // nothing to throw
    service.create(admin, command);
  }

  private TransactionCreateCommand command(String communityId, String beneficiary, String amount, String description, String adId) {
    return new TransactionCreateCommand()
      .setCommunityId(id(communityId))
      .setBeneficiaryId(id(beneficiary))
      .setAmount(new BigDecimal(amount))
      .setDescription(description)
      .setAdId(id(adId));
  }

  @Test
  public void markTransactionCompleted_txNotFound() {
    when(repository.findByBlockchainTransactionHash("hash")).thenReturn(empty());

    service.markTransactionCompleted("hash");

    verify(repository).findByBlockchainTransactionHash("hash");
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void markTransactionCompleted_alreadyCompleted() {
    TokenTransaction transaction = new TokenTransaction().setBlockchainCompletedAt(now());
    when(repository.findByBlockchainTransactionHash("hash")).thenReturn(of(transaction));

    service.markTransactionCompleted("hash");

    verify(repository).findByBlockchainTransactionHash("hash");
    verifyNoMoreInteractions(repository);
  }
}
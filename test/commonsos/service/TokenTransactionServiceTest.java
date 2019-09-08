package commonsos.service;

import static commonsos.TestId.id;
import static commonsos.repository.entity.AdType.WANT;
import static java.math.BigDecimal.TEN;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

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

import commonsos.controller.command.app.TransactionCreateCommand;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.repository.AdRepository;
import commonsos.repository.CommunityRepository;
import commonsos.repository.TokenTransactionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.TokenTransaction;
import commonsos.repository.entity.User;
import commonsos.service.blockchain.BlockchainEventService;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.TokenBalance;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TokenTransactionServiceTest {

  @Mock TokenTransactionRepository repository;
  @Mock UserRepository userRepository;
  @Mock AdRepository adRepository;
  @Mock CommunityRepository communityRepository;
  @Mock BlockchainService blockchainService;
  @Mock BlockchainEventService blockchainEventService;
  @Captor ArgumentCaptor<TokenTransaction> captor;
  @InjectMocks @Spy TokenTransactionService service;

  @Test
  public void createTransaction() {
    // prepare
    Community community = new Community().setFee(BigDecimal.ONE).setId(id("community"));
    User user = new User().setId(id("user")).setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    User beneficiary = new User().setId(id("beneficiary")).setCommunityUserList(asList(new CommunityUser().setCommunity(community)));
    Ad ad = new Ad().setPoints(new BigDecimal("10")).setCommunityId(id("community")).setCreatedBy(id("user")).setType(WANT);
    TokenBalance tokenBalance = new TokenBalance().setBalance(TEN);
    when(userRepository.findStrictById(any())).thenReturn(beneficiary);
    when(communityRepository.findPublicStrictById(any())).thenReturn(community);
    when(adRepository.findStrict(any())).thenReturn(ad);
    when(blockchainService.getTokenBalance(any(User.class), any(Long.class))).thenReturn(tokenBalance);
    
    // community is null
    TransactionCreateCommand command = command("community", "beneficiary", "10", "description", "ad id").setTransactionFee(BigDecimal.ONE);
    command.setCommunityId(null);
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    command.setCommunityId(id("community"));
    
    // description is blank
    command.setDescription("");
    assertThrows(BadRequestException.class, () -> service.create(user, command));
    command.setDescription("description");
    
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
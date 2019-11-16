package commonsos.service;

import static commonsos.TestId.id;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import commonsos.command.batch.CreateTokenTransactionForRedistributionCommand;
import commonsos.command.batch.RedistributionBatchCommand;
import commonsos.repository.CommunityRepository;
import commonsos.repository.RedistributionRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RedistributionServiceTest {

  @Mock private RedistributionRepository repository;
  @Mock private CommunityRepository communityRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks RedistributionService service;

  @Test
  public void createRedistributionCommand() {
    // prepare
    Community com1 = new Community().setId(id("com1"));
    User com1User1 = new User().setId(id("com1User1")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1)));
    User com1User2 = new User().setId(id("com1User2")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1)));
    User com1User3 = new User().setId(id("com1User3")).setCommunityUserList(asList(new CommunityUser().setCommunity(com1)));
    Redistribution com1Red1 = new Redistribution().setAll(true).setUser(null).setRate(new BigDecimal("1"));
    Redistribution com1Red2 = new Redistribution().setAll(false).setUser(com1User1).setRate(new BigDecimal("1"));
    when(communityRepository.searchPublic(any())).thenReturn(new ResultList<Community>().setList(asList(com1)));
    when(userRepository.search(any(), any(), any())).thenReturn(new ResultList<User>().setList(asList(com1User1, com1User2, com1User3)));
    when(repository.searchByCommunityId(any(), any())).thenReturn(new ResultList<Redistribution>().setList(asList(com1Red1, com1Red2)));
    
    // execute
    RedistributionBatchCommand command = service.createRedistributionCommand();
    Map<Community, List<CreateTokenTransactionForRedistributionCommand>> commandMap = command.getCommandMap();
    List<CreateTokenTransactionForRedistributionCommand> commandList = commandMap.get(new Community().setId(id("com1")));
    
    // verify
    assertThat(commandList.size()).isEqualTo(3);
    assertThat(commandList.get(0).getUser().getId()).isEqualTo(com1User1.getId());
    assertThat(commandList.get(0).getRate()).isEqualByComparingTo(new BigDecimal("1.333333333333333333"));
    assertThat(commandList.get(1).getUser().getId()).isEqualTo(com1User2.getId());
    assertThat(commandList.get(1).getRate()).isEqualByComparingTo(new BigDecimal("0.333333333333333333"));
    assertThat(commandList.get(2).getUser().getId()).isEqualTo(com1User3.getId());
    assertThat(commandList.get(2).getRate()).isEqualByComparingTo(new BigDecimal("0.333333333333333333"));
  }
}
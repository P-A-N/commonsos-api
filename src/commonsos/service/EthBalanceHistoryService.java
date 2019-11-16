package commonsos.service;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.command.PaginationCommand;
import commonsos.command.admin.SearchEthBalanceHistoriesCommand;
import commonsos.exception.ForbiddenException;
import commonsos.repository.CommunityRepository;
import commonsos.repository.EthBalanceHistoryRepository;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.EthBalanceHistory;
import commonsos.repository.entity.ResultList;
import commonsos.service.blockchain.BlockchainService;
import commonsos.service.blockchain.EthBalance;
import commonsos.util.AdminUtil;
import commonsos.util.EthBalanceUtil;
import commonsos.util.PaginationUtil;
import commonsos.view.EthBalanceListView;

@Singleton
public class EthBalanceHistoryService extends AbstractService {

  @Inject private EthBalanceHistoryRepository repository;
  @Inject private CommunityRepository communityRepository;
  @Inject private BlockchainService blockchainService;

  public void createEthBalanceHistory() {
    LocalDate today = LocalDate.now();
    List<Community> communityList = communityRepository.searchAll(null).getList();
    communityList.forEach(c -> {
      Optional<EthBalanceHistory> todaysBalance = repository.findByCommunityIdAndBaseDate(c.getId(), today);
      if (todaysBalance.isPresent()) return;
      
      EthBalance ethBalance = blockchainService.getEthBalance(c);
      
      // create
      EthBalanceHistory ethBalanceHistory = new EthBalanceHistory()
          .setCommunity(c)
          .setBaseDate(today)
          .setEthBalance(ethBalance.getBalance());
      repository.create(ethBalanceHistory);

      // commit
      commitAndStartNewTran();
    });
  }
  
  public EthBalanceListView searchEthBalanceHistory(Admin admin, SearchEthBalanceHistoriesCommand command, PaginationCommand paginationCommand) {
    if (!AdminUtil.isSeeableCommunity(admin, command.getCommunityId(), false)) throw new ForbiddenException();

    ResultList<EthBalanceHistory> result = repository.searchByCommunityIdAndBaseDate(command.getCommunityId(), command.getFrom(), command.getTo(), paginationCommand);

    EthBalanceListView listView = new EthBalanceListView();
    listView.setBalanceList(result.getList().stream().map(EthBalanceUtil::toHistoryView).collect(toList()));
    listView.setPagination(PaginationUtil.toView(result));
    
    return listView;
  }
}

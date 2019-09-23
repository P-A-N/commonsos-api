package commonsos.controller.admin.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonsos.controller.AbstractController;
import commonsos.repository.entity.SortType;
import commonsos.view.PaginationView;
import spark.Request;
import spark.Response;

public class SearchTokenTransactionsController extends AbstractController {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> transactionList = new ArrayList<>();

    Map<String, Object> transaction3 = new HashMap<>();
    transaction3.put("communityId", 1);
    transaction3.put("isFromAdmin", true);
    transaction3.put("wallet", "MAIN");
    transaction3.put("amount", new BigDecimal("1000"));
    transaction3.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    transaction3.put("completed", true);
    transaction3.put("debit", false);
    Map<String, Object> beneficiary3 = new HashMap<>();
    beneficiary3.put("id", 1);
    beneficiary3.put("username", "suzuki");
    transaction3.put("beneficiary", beneficiary3);

    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    transactionList.add(transaction3);
    
    PaginationView pagination = new PaginationView();
    pagination.setPage(0);
    pagination.setSize(10);
    pagination.setSort(SortType.ASC);
    pagination.setLastPage(5);
    

    Map<String, Object> result = new HashMap<>();
    result.put("transactionList", transactionList);
    result.put("pagination", pagination);
    
    return result;
  }
}

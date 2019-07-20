package commonsos.controller.admin.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commonsos.annotation.ReadOnly;
import commonsos.repository.entity.SortType;
import commonsos.view.PaginationView;
import spark.Request;
import spark.Response;
import spark.Route;

@ReadOnly
public class SearchUserTransactionsController implements Route {

  @Override
  public Object handle(Request request, Response response) {
    List<Object> transactionList = new ArrayList<>();

    Map<String, Object> transaction1 = new HashMap<>();
    transaction1.put("communityId", 1);
    transaction1.put("isFromAdmin", false);
    transaction1.put("amount", new BigDecimal("1000"));
    transaction1.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    transaction1.put("completed", true);
    transaction1.put("debit", true);
    Map<String, Object> remitter1 = new HashMap<>();
    remitter1.put("id", 1);
    remitter1.put("username", "suzuki");
    transaction1.put("remitter", remitter1);
    Map<String, Object> beneficiary1 = new HashMap<>();
    beneficiary1.put("id", 1);
    beneficiary1.put("username", "tanaka");
    transaction1.put("beneficiary", beneficiary1);

    Map<String, Object> transaction2 = new HashMap<>();
    transaction2.put("communityId", 1);
    transaction2.put("isFromAdmin", false);
    transaction2.put("amount", new BigDecimal("1000"));
    transaction2.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    transaction2.put("completed", true);
    transaction2.put("debit", false);
    Map<String, Object> remitter2 = new HashMap<>();
    remitter2.put("id", 1);
    remitter2.put("username", "tanaka");
    transaction2.put("remitter", remitter2);
    Map<String, Object> beneficiary2 = new HashMap<>();
    beneficiary2.put("id", 1);
    beneficiary2.put("username", "suzuki");
    transaction2.put("beneficiary", beneficiary2);

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

    Map<String, Object> transaction4 = new HashMap<>();
    transaction4.put("communityId", 1);
    transaction4.put("isFromAdmin", true);
    transaction4.put("wallet", "FEE");
    transaction4.put("amount", new BigDecimal("1000"));
    transaction4.put("createdAt", Instant.parse("2019-02-02T12:06:00Z"));
    transaction4.put("completed", true);
    transaction4.put("debit", false);
    Map<String, Object> beneficiary4 = new HashMap<>();
    beneficiary4.put("id", 1);
    beneficiary4.put("username", "suzuki");
    transaction4.put("beneficiary", beneficiary4);

    transactionList.add(transaction1);
    transactionList.add(transaction2);
    transactionList.add(transaction1);
    transactionList.add(transaction2);
    transactionList.add(transaction3);
    transactionList.add(transaction4);
    transactionList.add(transaction1);
    transactionList.add(transaction2);
    transactionList.add(transaction1);
    transactionList.add(transaction2);
    
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

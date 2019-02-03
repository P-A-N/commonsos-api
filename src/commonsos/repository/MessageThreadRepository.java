package commonsos.repository;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.MessageThreadNotFoundException;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.User;

@Singleton
public class MessageThreadRepository extends Repository {

  @Inject
  public MessageThreadRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<MessageThread> byAdId(User user, Long adId) {
    try {
      return Optional.of(em().createQuery("FROM MessageThread WHERE adId = :adId AND createdBy = :createdBy", MessageThread.class)
        .setLockMode(lockMode())
        .setParameter("adId", adId)
        .setParameter("createdBy", user.getId())
        .getSingleResult());
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public Optional<MessageThread> betweenUsers(Long userId1, Long userId2) {
    String sql = "SELECT * FROM message_threads mt " +
      "WHERE mt.ad_id IS NULL AND mt.is_group = FALSE AND " +
            "mt.id IN (SELECT mtp.message_thread_id FROM message_thread_parties mtp WHERE mtp.user_id = :user1 AND mt.id = mtp.message_thread_id) AND " +
            "mt.id IN (SELECT mtp.message_thread_id FROM message_thread_parties mtp WHERE mtp.user_id = :user2 AND mt.id = mtp.message_thread_id)";
    try {
      Object singleResult = em().createNativeQuery(sql, MessageThread.class)
        .setParameter("user1", userId1)
        .setParameter("user2", userId2)
        .getSingleResult();

      em().lock(singleResult, lockMode());
      
      return Optional.of((MessageThread)singleResult);
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public MessageThread create(MessageThread messageThread) {
    em().persist(messageThread);
    return messageThread;
  }

  public List<MessageThread> listByUser(User user) {
    return em()
      .createQuery("SELECT mt FROM MessageThread mt JOIN mt.parties p WHERE p.user = :user ORDER BY mt.id", MessageThread.class)
      .setLockMode(lockMode())
      .setParameter("user", user)
      .getResultList();
  }

  public List<MessageThread> listByUserAndMemberAndMessage(User user, String memberFilter, String messageFilter) {
    if (StringUtils.isBlank(memberFilter) && StringUtils.isBlank(messageFilter)) {
      return new ArrayList<>();
    }
    
    StringBuilder sql = new StringBuilder();
    sql.append(
        "SELECT " +
        "    mt.* " +
        "FROM message_threads mt " +
        "INNER JOIN message_thread_parties p " +
        "        ON p.message_thread_id = mt.id " +
        "       AND p.user_id = :userId " +
        "WHERE TRUE ");
    if (StringUtils.isNotBlank(memberFilter)) {
      sql.append(
          "AND EXISTS ( " +
          "    SELECT * FROM message_thread_parties mtp " +
          "    INNER JOIN users u " +
          "            ON u.id = mtp.user_id " +
          "    WHERE mtp.message_thread_id = mt.id " +
          "    AND LOWER(u.username) LIKE LOWER(:memberFilter) " +
          ") ");
    }
    if (StringUtils.isNotBlank(messageFilter)) {
      sql.append(
          "AND EXISTS ( " +
          "    SELECT * FROM messages m " +
          "    WHERE m.thread_id = mt.id " +
          "    AND m.text LIKE :messageFilter" +
          ") ");
    }
    sql.append("ORDER BY mt.id");

    Query query = em().createNativeQuery(sql.toString(), MessageThread.class);
    query.setParameter("userId", user.getId());
    if (StringUtils.isNotBlank(memberFilter)) {
      query.setParameter("memberFilter", "%" + memberFilter + "%");
    }
    if (StringUtils.isNotBlank(messageFilter)) {
      query.setParameter("messageFilter", "%" + messageFilter + "%");
    }
    
    try {
      @SuppressWarnings("unchecked")
      List<MessageThread> resultList = query.getResultList();
      resultList.forEach(r -> em().lock(r, lockMode()));
      
      return resultList;
    } catch (NoResultException e) {
      return new ArrayList<>();
    }
  }

  public Optional<MessageThread> findById(Long id) {
    return ofNullable(em().find(MessageThread.class, id, lockMode()));
  }

  public MessageThread findStrictById(Long id) {
    return findById(id).orElseThrow(MessageThreadNotFoundException::new);
  }

  public void update(MessageThreadParty party) {
    em().merge(party);
  }

  public void update(MessageThread messageThread) {
    em().merge(messageThread);
  }

  public int deleteMessageThreadParty(User user) {
    return em().createQuery(
      "DELETE FROM MessageThreadParty mtp WHERE mtp.user = :user " +
      "AND EXISTS (SELECT mt FROM MessageThread mt WHERE mt.id = mtp.messageThreadId AND (mt.adId IS NOT NULL OR mt.group IS TRUE))")
      .setParameter("user", user).executeUpdate();
  }

  public List<Long> unreadMessageThreadIds(User user) {
    return em().createQuery(
      "SELECT mt.id " +
        "FROM MessageThread mt JOIN MessageThreadParty mtp ON mt.id = mtp.messageThreadId " +
        "WHERE mtp.user = :user " +
        "AND mt.id IN(SELECT threadId FROM Message WHERE threadId = mt.id) "+
        "AND (mtp.visitedAt IS NULL OR mtp.visitedAt < (SELECT MAX(m.createdAt) FROM Message m WHERE m.threadId = mt.id)) " +
        "ORDER BY mt.id", Long.class)
      .setParameter("user", user).getResultList();
  }
}

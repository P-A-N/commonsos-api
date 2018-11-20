package commonsos.repository;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.repository.entity.Message;

@Singleton
public class MessageRepository extends Repository {

  @Inject
  public MessageRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Message create(Message message) {
    em().persist(message);
    return message;
  }

  public List<Message> listByThread(Long threadId) {
    return em()
      .createQuery("FROM Message WHERE threadId = :threadId ORDER BY createdAt", Message.class)
      .setLockMode(lockMode())
      .setParameter("threadId", threadId)
      .getResultList();
  }

  public Optional<Message> lastMessage(Long threadId) {
    List<Message> messages = em().createQuery("FROM Message WHERE threadId = :threadId ORDER BY createdAt DESC", Message.class)
      .setLockMode(lockMode())
      .setParameter("threadId", threadId)
      .setMaxResults(1)
      .getResultList();

    return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
  }
}

package se.kth.csc.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import se.kth.csc.model.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.LinkedList;
import java.util.List;

@Repository
public class JPAStore implements QueuePositionStore, QueueStore, AccountStore {
    private static final Logger log = LoggerFactory.getLogger(JPAStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Queue fetchQueueWithName(String name) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> q = cb.createQuery(Queue.class);
        Root<Queue> queue = q.from(Queue.class);
        try {
            return entityManager.createQuery(q.select(queue).where(cb.equal(queue.get(Queue_.name), name))).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Queue> fetchAllQueues() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> q = cb.createQuery(Queue.class);
        return entityManager.createQuery(q.select(q.from(Queue.class))).getResultList();
    }

    @Override
    public void storeQueue(Queue queue) {
        entityManager.persist(queue);
        log.info("Created a new queue with id {}", queue.getId());
    }

    @Override
    public void removeQueue(Queue queue) {
        for (Account owner : queue.getOwners()) {
            owner.getOwnedQueues().remove(queue);
        }
        for (Account moderator : queue.getModerators()) {
            moderator.getModeratedQueues().remove(queue);
        }
        for (QueuePosition position : queue.getPositions()) {
            removeQueuePosition(position);
        }
        queue.getOwners().clear();
        queue.getModerators().clear();
        queue.getPositions().clear();
        entityManager.remove(queue);
        log.info("Removed queue with id {}", queue.getId());
    }

    @Override
    public Account fetchAccountWithId(int id) {
        return entityManager.find(Account.class, id);
    }

    @Override
    public Account fetchAccountWithPrincipalName(String principalName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> q = cb.createQuery(Account.class);

        Root<Account> account = q.from(Account.class);

        try {
            return entityManager.createQuery(q.select(account).where(cb.equal(account.get(Account_.principalName), principalName))).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Account fetchNewestAccount() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> q = cb.createQuery(Account.class);

        Root<Account> account = q.from(Account.class);

        List<Account> candidates = entityManager.createQuery(q.select(account).orderBy(cb.desc(account.get(Account_.id))))
                .setMaxResults(1).getResultList();

        if (candidates.isEmpty()) {
            return null;
        } else {
            return candidates.get(0);
        }
    }

    @Override
    public void storeAccount(Account account) {
        entityManager.persist(account);
        log.info("Created a new account with id {}", account.getId());
    }

    @Override
    public Iterable<Account> findAccounts(boolean onlyAdmin, String query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> q = cb.createQuery(Account.class);
        Root<Account> account = q.from(Account.class);

        Expression<Boolean> expression = null;

        if (onlyAdmin) {
            // This looks like it could be replaced with account.get(Account_.admin), but it can't because of syntax
            expression = cb.equal(account.get(Account_.admin), true);
        }

        if (query != null) {
            Expression<Boolean> queryExpression =
                    cb.like(cb.lower(account.get(Account_.name)), "%" + query.toLowerCase() + "%");

            if (expression == null) {
                expression = queryExpression;
            } else {
                expression = cb.and(expression, queryExpression);
            }
        }

        if (expression != null) {
            q.where(expression);
        }

        return entityManager.createQuery(q.select(account)).getResultList();
    }

    @Override
    public QueuePosition fetchQueuePositionWithId(int id) {
        return entityManager.find(QueuePosition.class, id);
    }

    @Override
    public QueuePosition fetchQueuePositionWithQueueAndUser(String queueName, String userName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<QueuePosition> q = cb.createQuery(QueuePosition.class);

        Root<QueuePosition> queuePosition = q.from(QueuePosition.class);
        Join<QueuePosition, Queue> queue = queuePosition.join(QueuePosition_.queue);
        Join<QueuePosition, Account> account = queuePosition.join(QueuePosition_.account);

        try {
            return entityManager.createQuery(
                    q.select(queuePosition)
                            .where(cb.and(cb.equal(queue.get(Queue_.name), queueName),
                                    cb.equal(account.get(Account_.name), userName)))
            ).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void storeQueuePosition(QueuePosition queuePosition) {
        entityManager.persist(queuePosition);
        log.info("Created a new queue position with id {}", queuePosition.getId());
    }

    @Override
    public void removeQueuePosition(QueuePosition queuePosition) {
        queuePosition.getAccount().getPositions().remove(queuePosition);
        queuePosition.getQueue().getPositions().remove(queuePosition);
        queuePosition.setQueue(null);
        queuePosition.setAccount(null);
        entityManager.remove(queuePosition);
        log.info("Removed a queue position with id {}", queuePosition.getId());
    }
}

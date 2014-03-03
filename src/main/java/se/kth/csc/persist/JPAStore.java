package se.kth.csc.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import se.kth.csc.model.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

@Repository
public class JPAStore implements QueuePositionStore, QueueStore, AccountStore {
    private static final Logger log = LoggerFactory.getLogger(JPAStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Queue fetchQueueWithId(int id) {
        return entityManager.find(Queue.class, id);
    }

    @Override
    public List<Queue> fetchAllQueues() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> q = cb.createQuery(Queue.class);
        return entityManager.createQuery(q.select(q.from(Queue.class))).getResultList();
    }

    @Override
    public List<Queue> fetchAllActiveQueues() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> q = cb.createQuery(Queue.class);
        Root<Queue> queueRoot = q.from(Queue.class);
        q.select(queueRoot).where(queueRoot.get(Queue_.active));
        return entityManager.createQuery(q).getResultList();
    }

    @Override
    public List<Queue> fetchAllModeratedQueues(Account account) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> q = cb.createQuery(Queue.class);
        //Root<Queue> queueRoot = q.from(Queue.class);
        q.select(q.from(Queue.class));
        List<Queue> list = entityManager.createQuery(q).getResultList();
        List<Queue> tmpList = new LinkedList<Queue>();
        for(Queue que : list){
            if(que.getModerators().contains(account)){
                tmpList.add(que);
            }
        }
        return tmpList;
    }

    @Override
    public void storeQueue(Queue queue) {
        entityManager.persist(queue);
        log.info("Created a new queue with id {}", queue.getId());
    }

    @Override
    public void removeQueue(Queue queue) {
        for(Account a : queue.getOwners()) {
           a.getOwnedQueues().remove(queue);
        }
        queue.setOwners(null);
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
    public QueuePosition fetchQueuePositionWithId(int id) {
        return entityManager.find(QueuePosition.class, id);
    }

    @Override
    public void storeQueuePosition(QueuePosition queuePosition) {
        entityManager.persist(queuePosition);
        log.info("Created a new queue position with id {}", queuePosition.getId());
    }

    @Override
    public void removeQueuePosition(QueuePosition queuePosition) {
        entityManager.remove(queuePosition);
        log.info("Removed a queue position with id {}", queuePosition.getId());
    }
}

package se.kth.csc.persist;

import org.springframework.stereotype.Repository;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.model.User;
import se.kth.csc.model.User_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class JPAStore implements QueuePositionStore, QueueStore, UserStore {

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
    public void storeQueue(Queue queue) {
        entityManager.persist(queue);
    }

    @Override
    public void removeQueue(Queue queue) {
        entityManager.remove(queue);
    }

    @Override
    public User fetchUserWithId(int id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User fetchNewestUser() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> q = cb.createQuery(User.class);

        Root<User> user = q.from(User.class);

        List<User> candidates = entityManager.createQuery(q.select(user).orderBy(cb.desc(user.get(User_.id))))
                .setMaxResults(1).getResultList();

        if (candidates.isEmpty()) {
            return null;
        } else {
            return candidates.get(0);
        }
    }

    @Override
    public void storeUser(User user) {
        entityManager.persist(user);
    }

    @Override
    public QueuePosition fetchQueuePositionWithId(int id) {
        return entityManager.find(QueuePosition.class, id);
    }

    @Override
    public void storeQueuePosition(QueuePosition queuePosition) {
        entityManager.persist(queuePosition);
    }

    @Override
    public void removeQueuePosition(QueuePosition queuePosition) {
        entityManager.remove(queuePosition);
    }
}

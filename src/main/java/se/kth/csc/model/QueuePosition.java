package se.kth.csc.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "queue_position")
public class QueuePosition {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false)
    private int id;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "queue_id")
    private Queue queue;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "start_time")
    private DateTime startTime;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "location")
    private String location;

    @Column(name = "comment")
    private String comment;

    public int getId() {
        return id;
    }

    @JsonView({QueuePosition.class, Account.class})
    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    @JsonView({QueuePosition.class, Queue.class})
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}

package se.kth.csc.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "queue")
public class Queue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false)
    private int id;

    @Column(name = "name", unique = true)
    private String name;

    //@JoinColumn(name = "owner_id")
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    private Set<Account> owner = Sets.newHashSet();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queue", cascade = CascadeType.ALL)
    private Set<QueuePosition> positions = Sets.newHashSet();

    @Column(name = "active")
    private boolean active;

    @Column(name = "locked")
    private boolean locked;

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public boolean isLocked () {
        return locked;
    }

    public void setLocked (boolean locked) {
        this.locked = locked;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView(Queue.class)
    public Set<Account> getOwner() {
        return owner;
    }

    public void setOwner(Set<Account> owner) {
        this.owner = owner;
    }

    @JsonView(Queue.class)
    public Set<QueuePosition> getPositions() {
        return positions;
    }

    public void setPositions(Set<QueuePosition> positions) {
        this.positions = Sets.newHashSet(positions);
    }
}

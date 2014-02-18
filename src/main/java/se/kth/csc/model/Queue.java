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

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    private Set<Account> owners = Sets.newHashSet();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queue", cascade = CascadeType.ALL)
    private Set<QueuePosition> positions = Sets.newHashSet();

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
    public Set<Account> getOwners() {
        return owners;
    }

    public void setOwners(Set<Account> owners) {
        this.owners = owners;
    }

    public void addOwner(Account owner) {
        this.owners.add(owner);
    }

    public void removeOwner(Account owner) {
        this.owners.remove(owner);
    }

    @JsonView(Queue.class)
    public Set<QueuePosition> getPositions() {
        return positions;
    }

    public void setPositions(Set<QueuePosition> positions) {
        this.positions = Sets.newHashSet(positions);
    }
}

package se.kth.csc.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable = false)
    private int id;

    @Column(name = "principal_name", unique = true)
    private String principalName;

    @Column(name = "name")
    private String name;

    @Column(name = "admin")
    private boolean admin;

    @Column(name = "super_admin")
    private boolean superAdmin;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queue", cascade = CascadeType.ALL)
    private Set<QueuePosition> positions = Sets.newHashSet();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "owners", cascade = CascadeType.ALL)
    private Set<Queue> queues = Sets.newHashSet();

    public int getId() {
        return id;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() { return admin; }

    public boolean isSuperAdmin() { return superAdmin; }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setSuperAdmin(boolean superAdmin) { this.superAdmin = superAdmin; }

    @JsonView(Account.class)
    public Set<QueuePosition> getPositions() {
        return positions;
    }

    public void setPositions(Set<QueuePosition> positions) {
        this.positions = Sets.newHashSet(positions);
    }

    @JsonView(Account.class)
    public Set<Queue> getQueues() {
        return queues;
    }

    public void setQueues(Set<Queue> queues) {
        this.queues = Sets.newHashSet(queues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!principalName.equals(account.principalName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return principalName.hashCode();
    }
}

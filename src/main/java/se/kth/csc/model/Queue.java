package se.kth.csc.model;

/*
 * #%L
 * QWait
 * %%
 * Copyright (C) 2013 - 2014 KTH School of Computer Science and Communication
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableSet;
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

    @Column(name = "title")
    private String title;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "account_queue",
            joinColumns =
            @JoinColumn(name = "queue_id", referencedColumnName = "id"),
            inverseJoinColumns =
            @JoinColumn(name = "account_id", referencedColumnName = "id"))
    private Set<Account> owners = Sets.newHashSet();


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "moderator_queue",
            joinColumns =
            @JoinColumn(name = "queue_id", referencedColumnName = "id"),
            inverseJoinColumns =
            @JoinColumn(name = "account_id", referencedColumnName = "id"))
    private Set<Account> moderators = Sets.newHashSet();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queue", cascade = CascadeType.ALL)
    private Set<QueuePosition> positions = Sets.newHashSet();

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "locked")
    private boolean locked;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonView(Queue.class)
    public Set<Account> getOwners() {
        return owners;
    }

    public void setOwners(Set<Account> owners) {
        this.owners = owners;
    }

    @JsonView(Queue.class)
    public Set<Account> getModerators() {
        return moderators;
    }

    public void setModerators(Set<Account> moderators) {
        this.moderators = moderators;
    }

    public ImmutableSet<String> getOwnerNames() {
        ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();

        for (Account owner : owners) {
            resultBuilder.add(owner.getPrincipalName());
        }

        return resultBuilder.build();
    }

    public void addOwner(Account owner) {
        this.owners.add(owner);
    }

    public void removeOwner(Account owner) {
        this.owners.remove(owner);
    }

    public ImmutableSet<String> getModeratorNames() {
        ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();

        for (Account moderator : moderators) {
            resultBuilder.add(moderator.getPrincipalName());
        }

        return resultBuilder.build();
    }

    public void addModerator(Account moderator) {
        this.moderators.add(moderator);
    }

    public void removeModerator(Account moderator) {
        this.moderators.remove(moderator);
    }

    @JsonView(Queue.class)
    public Set<QueuePosition> getPositions() {
        return positions;
    }

    public void setPositions(Set<QueuePosition> positions) {
        this.positions = Sets.newHashSet(positions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Queue queue = (Queue) o;

        if (id != queue.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

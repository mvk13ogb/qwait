package se.kth.csc.controller;

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

import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;

public interface ApiProvider {
    Iterable<Account> findAccounts(boolean onlyAdmin, String query);

    Account fetchAccount(String userName) throws NotFoundException;

    Queue fetchQueue(String queueName) throws NotFoundException;

    Iterable<Queue> fetchAllQueues();

    QueuePosition fetchQueuePosition(String queueName, String userName);

    void createQueue(String queueName, String title);

    void setAdmin(Account account, boolean admin) throws ForbiddenException;

    void deleteQueue(Queue queue);

    void addQueuePosition(Queue queue, Account account);

    void deleteQueuePosition(QueuePosition queuePosition);

    void setComment(QueuePosition queuePosition, String comment) throws BadRequestException;

    void setLocation(QueuePosition queuePosition, String location) throws BadRequestException;

    void clearQueue(Queue queue);

    void setHidden(Queue queue, boolean hidden);

    void setLocked(Queue queue, boolean locked);

    void addOwner(Queue queue, Account owner);

    void removeOwner(Queue queue, Account owner);

    void addModerator(Queue queue, Account owner);

    void removeModerator(Queue queue, Account owner);
}

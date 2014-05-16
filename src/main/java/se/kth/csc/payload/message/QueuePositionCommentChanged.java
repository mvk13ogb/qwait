package se.kth.csc.payload.message;

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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueuePositionCommentChanged {
    private final String queueName;
    private final String userName;
    private final String comment;

    public QueuePositionCommentChanged(String queueName, String userName, String comment) {
        this.queueName = queueName;
        this.userName = userName;
        this.comment = comment;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getUserName() {
        return userName;
    }

    public String getComment() {
        return comment;
    }
}

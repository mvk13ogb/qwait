package se.kth.csc.payload.api;

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

import org.joda.time.DateTime;

public class QueuePositionInQueueSnapshot extends NormalizedQueuePositionSnapshot {
    private final String userName;
    private final String readableName;

    public QueuePositionInQueueSnapshot(DateTime startTime, String location, String comment, String userName, String readableName) {
        super(startTime, location, comment);
        this.userName = userName;
        this.readableName = readableName;
    }

    public String getUserName() {
        return userName;
    }

    public String getReadableName() {
        return readableName;
    }
}

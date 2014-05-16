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

public class NormalizedQueueSnapshot {
    private final String name;
    private final String title;
    private final boolean hidden;
    private final boolean locked;

    public NormalizedQueueSnapshot(String name, String title, boolean hidden, boolean locked) {
        this.name = name;
        this.title = title;
        this.hidden = hidden;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLocked() {
        return locked;
    }
}

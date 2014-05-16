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

import com.google.common.collect.ImmutableSet;

public class NormalizedAccountSnapshot {
    private final String name;
    private final String readableName;
    private final boolean admin;
    private final boolean anonymous;
    private final ImmutableSet<String> roles;

    public NormalizedAccountSnapshot(String name, String readableName, boolean admin, boolean anonymous,
                                     ImmutableSet<String> roles) {
        this.name = name;
        this.readableName = readableName;
        this.admin = admin;
        this.anonymous = anonymous;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getReadableName() {
        return readableName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public ImmutableSet<String> getRoles() {
        return roles;
    }
}

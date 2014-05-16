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

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import se.kth.csc.config.WebSecurityConfigurationAware;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ApiControllerIT extends WebSecurityConfigurationAware {
    @Test
    public void testGetUserAdmin() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(true)))
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
    }

    @Test
    public void testGetUserNormal() throws Exception {
        MockHttpSession session = signInAs("u1testUser");
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)))
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
    }

    @Test
    public void testPutUserRoleAdminAuthorize() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");

        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/u1testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(true)));
    }

    @Test
    public void testPutUserRoleAdminRevoke() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(true)));

        mockMvc.perform(put("/api/user/u1testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    // It should not be allowed to remove the only existing admin
    public void testRevokeOnlyAdmin() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(true)));

        mockMvc.perform(put("/api/user/u1testUser/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPutUserRoleAdminForbidden() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser");
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/u1testUser/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    public void testPutUserRoleAdminForbiddenOther() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/u1testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    public void testPutUserRoleAdminForbiddenRevokeOther() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(true)));

        mockMvc.perform(put("/api/user/u1testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser2")))
                .andExpect(jsonPath("admin", is(true)));
    }

    @Test
    public void testAddQueue() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("hidden", is(false)))
                .andExpect(jsonPath("locked", is(false)))
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("owners[0]", is("u1testUser")))
                .andExpect(jsonPath("moderators", hasSize(0)))
                .andExpect(jsonPath("positions", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueNotAdmin() throws Exception {
        MockHttpSession session = signInAs("u1testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddQueueConflict() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAddRemoveQueue() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
        mockMvc.perform(delete("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queues").session(session))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testAddQueueAddOwner() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueTakeover() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/owner/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(1)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueForbiddenTakeover() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));


        mockMvc.perform(delete("/api/queue/abc123/owner/u1testUser").session(session2))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueAdminTakeover() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/owner/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(1)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueModerator() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/moderator/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(1)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddRemoveQueueModerator() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/moderator/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(1)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/moderator/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(0)));
        mockMvc.perform(get("/api/user/u1testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queues").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueuePopulateQueue() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(1)));
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(2)));
    }

    /* Testing the clear functionality by adding a queue, populating queue and trying
    to clear the queue from an admin account. */
    @Test
    public void testClearQueue() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(2)));
        mockMvc.perform(post("/api/queue/abc123/clear").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(0)));
    }

    @Test
    public void testOwnerAddAndRevokeOwner() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        // Need two admins to be able to remove one admin later
        MockHttpSession session3 = signInAs("u1testUser3", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser3").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser3")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(put("/api/user/u1testUser/role/admin").session(session).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("u1testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners[0]", is("u1testUser")));
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser2").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(delete("/api/queue/abc123/owner/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("owners[0]", is("u1testUser2")));
    }

    /* Test close and open queue as Admin (not Owner) and as Owner (not Admin)*/
    @Test
    public void testCloseAndOpenQueue() throws Exception {
        // For Admin (who is not an Owner)
        MockHttpSession session = signInAs("u1testUser", "admin");
        // Need two admins to be able to remove one admin later
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/queue/abc123/owner/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(0)))
                .andExpect(jsonPath("hidden", is(false)));
        mockMvc.perform(put("/api/queue/abc123/hidden").contentType(MediaType.APPLICATION_JSON).session(session).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hidden", is(true)));
        mockMvc.perform(put("/api/queue/abc123/hidden").contentType(MediaType.APPLICATION_JSON).session(session).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hidden", is(false)));

        // For Owner (who is not an Admin)
        mockMvc.perform(get("/api/user/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("hidden", is(false)));
        mockMvc.perform(put("/api/user/u1testUser/role/admin").session(session).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hidden", is(false)));
        mockMvc.perform(put("/api/queue/abc123/hidden").contentType(MediaType.APPLICATION_JSON).session(session).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hidden", is(true)));
        mockMvc.perform(put("/api/queue/abc123/hidden").contentType(MediaType.APPLICATION_JSON).session(session).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hidden", is(false)));
    }

    /**
     * Test to add and remove a comment for a user.
     *
     * @throws Exception
     */
    @Test
    public void testAddRemoveComment() throws Exception {
        MockHttpSession session = signInAs("u1testAdmin", "admin");
        MockHttpSession session2 = signInAs("u1testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        // Test to add comment for a user
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser/comment").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"comment\":\"This is a comment\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser/comment").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("comment", is("This is a comment")));
        // Test to remove comment
        mockMvc.perform(delete("/api/queue/abc123/position/u1testUser/comment").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser/comment").session(session2))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to add and remove a comment for another user.
     *
     * @throws Exception
     */
    @Test
    public void testAddRemoveCommentForbidden() throws Exception {
        MockHttpSession session = signInAs("u1testAdmin", "admin");
        MockHttpSession session2 = signInAs("u1testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin/comment").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"comment\":\"This is a comment\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        // Try to modify another user's comment
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin/comment").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"comment\":\"This is another a comment\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/queue/abc123/position/u1testAdmin/comment").session(session2))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLockUnlockQueueAsAdmin() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser1", "admin");
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session2).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(true)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session2).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
    }

    @Test
    public void testLockUnlockQueueAsOwner() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser1", "admin");
        // Need two admins to be able to remove one admin later
        MockHttpSession session2 = signInAs("u1testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser1").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(put("/api/queue/abc123/owner/u1testUser1").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(put("/api/user/u1testUser1/role/admin").contentType(MediaType.APPLICATION_JSON).session(session1).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser1").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session1).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(true)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session1).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
    }

    @Test
    public void testLockUnlockQueueAsModerator() throws Exception {
        MockHttpSession session1 = signInAs("u1testUser1", "admin");
        MockHttpSession session2 = signInAs("u1testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(put("/api/queue/abc123/moderator/u1testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/u1testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0]", is("abc123")));
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session2).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(true)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session2).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
    }

    /**
     * Test to add and remove the location for a user.
     *
     * @throws Exception
     */
    @Test
    public void testAddRemoveLocation() throws Exception {
        MockHttpSession session = signInAs("u1testAdmin", "admin");
        MockHttpSession session2 = signInAs("u1testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        // Test to add location for a user
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser/location").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"location\":\"This is a location\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser/location").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("location", is("This is a location")));
        // Test to remove location
        mockMvc.perform(delete("/api/queue/abc123/position/u1testUser/location").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/u1testUser/location").session(session2))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to add and remove a location for another user.
     *
     * @throws Exception
     */
    @Test
    public void testAddRemoveLocationForbidden() throws Exception {
        MockHttpSession session = signInAs("u1testAdmin", "admin");
        MockHttpSession session2 = signInAs("u1testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin/location").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"location\":\"This is a location\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session2))
                .andExpect(status().isOk());
        // Try to modify another user's location
        mockMvc.perform(put("/api/queue/abc123/position/u1testAdmin/location").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"location\":\"This is another a location\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/queue/abc123/position/u1testAdmin/location").session(session2))
                .andExpect(status().isForbidden());
    }

    /* Test if a user can join, then leave a queue */
    @Test
    public void testJoinLeaveQueue() throws Exception {
        MockHttpSession session = signInAs("u1testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(1)))
                .andExpect(jsonPath("positions[0].userName", is("u1testUser")));
        mockMvc.perform(delete("/api/queue/abc123/position/u1testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(0)));
    }
}

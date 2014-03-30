package se.kth.csc.controller;

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
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(true)))
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
    }

    @Test
    public void testGetUserNormal() throws Exception {
        MockHttpSession session = signInAs("testUser");
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)))
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
    }

    @Test
    public void testPutUserRoleAdminAuthorize() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");

        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(true)));
    }

    @Test
    public void testPutUserRoleAdminRevoke() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(true)));

        mockMvc.perform(put("/api/user/testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(true)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    public void testPutUserRoleAdminForbidden() throws Exception {
        MockHttpSession session1 = signInAs("testUser");
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/testUser/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    public void testPutUserRoleAdminForbiddenOther() throws Exception {
        MockHttpSession session1 = signInAs("testUser");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(false)));

        mockMvc.perform(put("/api/user/testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(false)));
    }

    @Test
    public void testPutUserRoleAdminForbiddenRevokeOther() throws Exception {
        MockHttpSession session1 = signInAs("testUser");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(true)));

        mockMvc.perform(put("/api/user/testUser2/role/admin").session(session1).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser2")))
                .andExpect(jsonPath("admin", is(true)));
    }

    @Test
    public void testAddQueue() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("active", is(true)))
                .andExpect(jsonPath("locked", is(false)))
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("owners[0].name", is("testUser")))
                .andExpect(jsonPath("owners[0].admin", is(true)))
                .andExpect(jsonPath("moderators", hasSize(0)))
                .andExpect(jsonPath("positions", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueNotAdmin() throws Exception {
        MockHttpSession session = signInAs("testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddQueueConflict() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAddRemoveQueue() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueAddOwner() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueTakeover() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/owner/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(1)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueForbiddenTakeover() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));


        mockMvc.perform(delete("/api/queue/abc123/owner/testUser").session(session2))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueAdminTakeover() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/owner/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/owner/testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(1)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddQueueModerator() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/moderator/testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(1)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }

    @Test
    public void testAddRemoveQueueModerator() throws Exception {
        MockHttpSession session1 = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(put("/api/queue/abc123/moderator/testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(1)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));

        mockMvc.perform(delete("/api/queue/abc123/moderator/testUser2").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("moderators", hasSize(0)));
        mockMvc.perform(get("/api/user/testUser").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)))
                .andExpect(jsonPath("ownedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("queuePositions", hasSize(0)))
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(get("/api/queue/list").session(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("abc123")));
    }
}

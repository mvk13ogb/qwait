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
        mockMvc.perform(delete("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/list").session(session))
                .andExpect(jsonPath("$", hasSize(0)));
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

    @Test
    public void testAddQueuePopulateQueue() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(1)));
        mockMvc.perform(put("/api/queue/abc123/position/testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("positions", hasSize(2)));
    }

    /* Testing the clear functionality by adding a queue, populating queue and trying
    to clear the queue from an admin account. */
    @Test
    public void testClearQueue() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser2").session(session2))
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
        MockHttpSession session = signInAs("testUser", "admin");
        MockHttpSession session2 = signInAs("testUser2");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/user/testUser/role/admin").session(session).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testUser")))
                .andExpect(jsonPath("admin", is(false)));
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners[0].name", is("testUser")));
        mockMvc.perform(get("/api/user/testUser2").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/testUser2").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(2)));
        mockMvc.perform(delete("/api/queue/abc123/owner/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("owners[0].name", is("testUser2")));
    }

    /* Test close and open queue as Admin (not Owner) and as Owner (not Admin)*/
    @Test
    public void testCloseAndOpenQueue() throws Exception {
        // For Admin (who is not an Owner)
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/queue/abc123/owner/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(0)))
                .andExpect(jsonPath("active", is(true)));
        mockMvc.perform(put("/api/queue/abc123/active").contentType(MediaType.APPLICATION_JSON).session(session).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active", is(false)));
        mockMvc.perform(put("/api/queue/abc123/active").contentType(MediaType.APPLICATION_JSON).session(session).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active", is(true)));

        // For Owner (who is not an Admin)
        mockMvc.perform(get("/api/user/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/owner/testUser").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("active", is(true)));
        mockMvc.perform(put("/api/user/testUser/role/admin").session(session).contentType(MediaType.APPLICATION_JSON).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active", is(true)));
        mockMvc.perform(put("/api/queue/abc123/active").contentType(MediaType.APPLICATION_JSON).session(session).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active", is(false)));
        mockMvc.perform(put("/api/queue/abc123/active").contentType(MediaType.APPLICATION_JSON).session(session).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("active", is(true)));
    }

    /**
     * Test to add and remove a comment for a user.
     * @throws Exception
     */
    @Test
    public void testAddRemoveComment() throws Exception {
        MockHttpSession session = signInAs("testAdmin", "admin");
        MockHttpSession session2 = signInAs("testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        // Test to add comment for a user
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser/comment").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"comment\":\"This is a comment\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser/comment").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("comment", is("This is a comment")));
        // Test to remove comment
        mockMvc.perform(delete("/api/queue/abc123/position/testUser/comment").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser/comment").session(session2))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to add and remove a comment for another user.
     * @throws Exception
     */
    @Test
    public void testAddRemoveCommentForbidden() throws Exception {
        MockHttpSession session = signInAs("testAdmin", "admin");
        MockHttpSession session2 = signInAs("testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin/comment").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"comment\":\"This is a comment\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        // Try to modify another user's comment
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin/comment").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"comment\":\"This is another a comment\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/queue/abc123/position/testAdmin/comment").session(session2))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLockUnlockQueue() throws Exception {
        MockHttpSession session1 = signInAs("testUser1", "admin");
        MockHttpSession session2 = signInAs("testUser2", "admin");
        MockHttpSession session3 = signInAs("testUser3");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session1).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());

        // Only queue owner
        mockMvc.perform(put("/api/user/testUser1/role/admin").contentType(MediaType.APPLICATION_JSON).session(session1).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser1").session(session1))
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

        // Only admin
        mockMvc.perform(get("/api/user/testUser2").session(session2))
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

        // Moderator only
        mockMvc.perform(get("/api/user/testUser3").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(0)));
        mockMvc.perform(put("/api/queue/abc123/moderator/testUser3").session(session1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user/testUser3").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("ownedQueues", hasSize(0)))
                .andExpect(jsonPath("moderatedQueues", hasSize(1)))
                .andExpect(jsonPath("moderatedQueues[0].name", is("abc123")));
        mockMvc.perform(get("/api/queue/abc123").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session3).content("true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(true)));
        mockMvc.perform(put("/api/queue/abc123/locked").contentType(MediaType.APPLICATION_JSON).session(session3).content("false"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").session(session3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("locked", is(false)));
    }

    /**
     * Test to add and remove the location for a user.
     * @throws Exception
     */
    @Test
    public void testAddRemoveLocation() throws Exception {
        MockHttpSession session = signInAs("testAdmin", "admin");
        MockHttpSession session2 = signInAs("testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        // Test to add location for a user
        mockMvc.perform(get("/api/queue/abc123").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser/location").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"location\":\"This is a location\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser/location").session(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("location", is("This is a location")));
        // Test to remove location
        mockMvc.perform(delete("/api/queue/abc123/position/testUser/location").session(session2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123/position/testUser/location").session(session2))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to add and remove a location for another user.
     * @throws Exception
     */
    @Test
    public void testAddRemoveLocationForbidden() throws Exception {
        MockHttpSession session = signInAs("testAdmin", "admin");
        MockHttpSession session2 = signInAs("testUser");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"title\":\"Test Queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin/location").contentType(MediaType.APPLICATION_JSON).session(session)
                .content("{\"location\":\"This is a location\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/queue/abc123/position/testUser").session(session2))
                .andExpect(status().isOk());
        // Try to modify another user's location
        mockMvc.perform(put("/api/queue/abc123/position/testAdmin/location").contentType(MediaType.APPLICATION_JSON).session(session2)
                .content("{\"location\":\"This is another a location\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/queue/abc123/position/testAdmin/location").session(session2))
                .andExpect(status().isForbidden());
    }
}

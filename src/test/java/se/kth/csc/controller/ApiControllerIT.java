package se.kth.csc.controller;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import se.kth.csc.config.WebSecurityConfigurationAware;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiControllerIT extends WebSecurityConfigurationAware {
    @Test
    public void testAddQueue() throws Exception {
        MockHttpSession session = signInAs("testUser", "admin");
        mockMvc.perform(put("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session).content("{\"title\":\"Test queue\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/queue/abc123").contentType(MediaType.APPLICATION_JSON).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Test queue")))
                .andExpect(jsonPath("name", is("abc123")))
                .andExpect(jsonPath("owners", hasSize(1)))
                .andExpect(jsonPath("owners[0].name", is("testUser")))
                .andExpect(jsonPath("moderators", hasSize(0)))
                .andExpect(jsonPath("positions", hasSize(0)));
    }
}

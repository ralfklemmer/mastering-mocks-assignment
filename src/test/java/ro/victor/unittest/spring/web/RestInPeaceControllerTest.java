package ro.victor.unittest.spring.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
// want to open port:
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestInPeaceControllerTest {

	@Autowired
    private MockMvc mockMvc;

//    @LocalServerPort
//    private int port;
//    @Before
//    public void initialize() {
//        System.out.println("Opened port on " + port);
//    }

    @Test
    public void peaceTest() throws Exception {
    	mockMvc.perform(get("/peace/{ssn}","abc"))
    		.andExpect(status().isOk())
    		.andExpect(header().string("Head-Shot", "true"))
    		.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.ssn").value(CoreMatchers.startsWith("ABC")));
        // TODO check status 200
    	// TODO check "Head-Shot" header
        // TODO check contentType
        // TODO check jsonPath $.ssn
        // TODO [extra] check and control TIME :) Hint: @Primary @Bean
    }

}



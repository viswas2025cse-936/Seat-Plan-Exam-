package com.example.Seat.Plan;

import com.example.Seat.Plan.dto.HallRequestDTO;
import com.example.Seat.Plan.dto.StudentRequestDTO;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class SeatPlanControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("REST API: Register Hall and Retrieve Halls List")
    void testHallEndpoints() throws Exception {
        HallRequestDTO hallRequest = new HallRequestDTO("REST Hall Omega", 5, 5, 25);

        mockMvc.perform(post("/api/halls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hallRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("REST Hall Omega"))
                .andExpect(jsonPath("$.capacity").value(25))
                .andExpect(jsonPath("$.totalRows").value(5))
                .andExpect(jsonPath("$.totalColumns").value(5));

        mockMvc.perform(get("/api/halls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("REST API: Input validation rejects invalid hall dimensions")
    void testHallValidationFailure() throws Exception {
        HallRequestDTO invalidHall = new HallRequestDTO("", -1, 0, 0);

        mockMvc.perform(post("/api/halls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidHall)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isMap());
    }

    @Test
    @DisplayName("REST API: Register Student and Search")
    void testStudentEndpoints() throws Exception {
        StudentRequestDTO studentRequest = new StudentRequestDTO("REST-STUD-99", "Sarah Connor", "Cybernetics", "CYBER", "A");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNumber").value("REST-STUD-99"))
                .andExpect(jsonPath("$.name").value("Sarah Connor"));

        mockMvc.perform(get("/api/students/roll/REST-STUD-99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sarah Connor"));
    }

    @Test
    @DisplayName("REST API: Student Lookup for non-existent roll returns 404")
    void testStudentNotFound() throws Exception {
        mockMvc.perform(get("/api/allocations/student/NON_EXISTENT_ROLL"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }
}

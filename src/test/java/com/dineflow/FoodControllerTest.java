package com.dineflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllFoodItems() throws Exception {
        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(18)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Paneer Tikka Croquettes"))
                .andExpect(jsonPath("$[0].price").value(280.0))
                .andExpect(jsonPath("$[0].imageUrl").isNotEmpty());
    }

    @Test
    void shouldReturnFoodById() throws Exception {
        mockMvc.perform(get("/api/foods/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Saffron Truffle Risotto"))
                .andExpect(jsonPath("$.category").value("Main Course"))
                .andExpect(jsonPath("$.price").value(520.0));
    }

    @Test
    void shouldReturn404ForNonExistentFoodId() throws Exception {
        mockMvc.perform(get("/api/foods/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldReturnFoodsByCategory() throws Exception {
        mockMvc.perform(get("/api/foods/category/Pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].category", everyItem(equalToIgnoringCase("Pizza"))));
    }

    @Test
    void shouldReturnFoodsByBurgersCategory() throws Exception {
        mockMvc.perform(get("/api/foods/category/Burgers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].category", everyItem(equalToIgnoringCase("Burgers"))));
    }
}

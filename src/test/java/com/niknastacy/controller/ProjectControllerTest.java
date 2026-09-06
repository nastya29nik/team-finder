package com.niknastacy.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Главная страница проектов доступна и отдает 200 OK")
    void testProjectsListPage() throws Exception {
        mockMvc.perform(get("/projects/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/project_list"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("all_skills"));
    }

    @Test
    @DisplayName("Редирект с корня на список проектов")
    void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/list"));
    }
}

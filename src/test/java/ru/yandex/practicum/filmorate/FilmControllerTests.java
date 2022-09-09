package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.CustomValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmControllerTests {
    private static final LocalDate RELEASE_START_DATE = LocalDate.of(1895, 12, 28);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private FilmController filmController;

    @Test
    void shouldReturnCollectionWithTwoCreatedFilmsGetRequestTest() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));

        Film film2 = new Film(
                "1nd film's name",
                "1nd film's description",
                RELEASE_START_DATE.plusDays(2),
                120
        );
        req = objectMapper.writeValueAsString(film2);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));

        var response = mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String resp = response.getContentAsString();
        Assertions.assertEquals(resp, objectMapper.writeValueAsString(filmController.findAll()));
    }

    @Test
    void shouldReturnExceptionBecauseOfReleaseDate() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.minusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        post("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(CustomValidationException.class)
                );
    }
}


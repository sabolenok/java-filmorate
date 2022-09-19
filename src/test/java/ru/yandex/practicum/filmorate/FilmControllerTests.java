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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

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
    @SpyBean
    private FilmService filmService;
    @SpyBean
    private InMemoryFilmStorage filmStorage;
    @SpyBean
    private UserService userService;
    @SpyBean
    private InMemoryUserStorage userStorage;

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
    void shouldReturnNewCreatedFilm() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        String response = mockMvc.perform(
                        post("/films").content(req).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(
                response,
                "{\"id\":"
                        + filmStorage.getId()
                        + ",\"likes\":[],\"name\":\"1st film's name\",\"description\":\"1st film's description\",\"releaseDate\":\"1895-12-29\",\"duration\":65}"
        );
    }

    @Test
    void shouldReturnNewUpdatedFilm() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        film1.setId(filmStorage.getId());
        film1.setDuration(120);
        req = objectMapper.writeValueAsString(film1);
        String response = mockMvc.perform(
                        put("/films").content(req).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(
                response,
                "{\"id\":"
                        + filmStorage.getId()
                        + ",\"likes\":[],\"name\":\"1st film's name\",\"description\":\"1st film's description\",\"releaseDate\":\"1895-12-29\",\"duration\":120}"
        );
    }

    @Test
    void shouldThrowNotFoundException() throws Exception {
        Film film42 = new Film(
                "3d film's name",
                "3d film's description",
                RELEASE_START_DATE.plusDays(1),
                100
        );
        String req = objectMapper.writeValueAsString(film42);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        // поставим вручную фильму id = 42, и попробуем обновить его продолжительность
        film42.setId(42);
        film42.setDuration(95);
        req = objectMapper.writeValueAsString(film42);
        // должны получить статус 404
        mockMvc.perform(put("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrowExceptionTryingToCreateFilmBecauseOfReleaseDate() throws Exception {
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
                        mvcResult.getResolvedException().getClass().equals("Дата релиза должна быть не раньше 28.12.1895")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateFilmBecauseOfReleaseDate() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        film1.setId(filmStorage.getId());
        film1.setReleaseDate(RELEASE_START_DATE.minusDays(1));
        req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        put("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Дата релиза должна быть не раньше 28.12.1895")
                );
    }

    @Test
    void shouldThrowExceptionTryingToCreateFilmBecauseOfEmptyName() throws Exception {
        Film film1 = new Film(
                "",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        post("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Название не может быть пустым")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateFilmBecauseOfEmptyName() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        film1.setId(filmStorage.getId());
        film1.setName("");
        req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        put("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Название не может быть пустым")
                );
    }

    @Test
    void shouldThrowExceptionTryingToCreateFilmBecauseOfTooLongDescription() throws Exception {
        Film film1 = new Film(
                "Star Wars: Episode IV - A New Hope",
                "A long time ago in a galaxy far, far away... " +
                        "It is a period of civil war. Rebel spaceships, striking from a hidden base, " +
                        "have won their first victory against the evil Galactic Empire. During the battle, " +
                        "Rebel spies managed to steal secret plans to the Empire's ultimate weapon, " +
                        "the DEATH STAR, an armored space station with enough power to destroy an entire planet.",
                LocalDate.of(1977, 5, 25),
                121
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        post("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Длина описания не может быть больше 200 символов")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateFilmBecauseOfTooLongDescription() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        film1.setId(filmStorage.getId());
        film1.setDescription("A long time ago in a galaxy far, far away... " +
                "It is a period of civil war. Rebel spaceships, striking from a hidden base, " +
                "have won their first victory against the evil Galactic Empire. During the battle, " +
                "Rebel spies managed to steal secret plans to the Empire's ultimate weapon, " +
                "the DEATH STAR, an armored space station with enough power to destroy an entire planet.");
        req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        put("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Длина описания не может быть больше 200 символов")
                );
    }

    @Test
    void shouldThrowExceptionTryingToCreateFilmBecauseOfNegativeDuration() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                -1
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        post("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Продолжительность фильма должна быть положительной")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateFilmBecauseOfNegativeDuration() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        film1.setId(filmStorage.getId());
        film1.setDuration(-1);
        req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(
                        put("/films").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Продолжительность фильма должна быть положительной")
                );
    }

    @Test
    void shouldReturnStatus400TryingToPostEmptyRequest() throws Exception {
        mockMvc.perform(post("/films").content("").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void shouldReturnStatus400TryingToPutEmptyRequest() throws Exception {
        Film film1 = new Film(
                "1st film's name",
                "1st film's description",
                RELEASE_START_DATE.plusDays(1),
                65
        );
        String req = objectMapper.writeValueAsString(film1);
        mockMvc.perform(post("/films").content(req).contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/films").content("").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }
}


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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private UserController userController;

    @Test
    void shouldReturnCollectionWithTwoCreatedUsersGetRequestTest() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));

        User user2 = new User(
                "mail1@mail.ru",
                "login2",
                "One More Name",
                LocalDate.of(1900, 2, 2)
        );
        req = objectMapper.writeValueAsString(user2);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));

        var response = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String resp = response.getContentAsString();
        Assertions.assertEquals(resp, objectMapper.writeValueAsString(userController.findAll()));
    }

    @Test
    void shouldReturnNewCreatedUser() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        String response = mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(
                response,
                "{\"id\":1,\"email\":\"mail@mail.ru\",\"login\":\"login1\",\"name\":\"New Name\",\"birthday\":\"1900-01-01\"}"
        );
    }

    @Test
    void shouldReturnNewUpdatedUser() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        user1.setId(1);
        user1.setName("One More Name");
        req = objectMapper.writeValueAsString(user1);
        String response = mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(
                response,
                "{\"id\":1,\"email\":\"mail@mail.ru\",\"login\":\"login1\",\"name\":\"One More Name\",\"birthday\":\"1900-01-01\"}"
        );
    }

    @Test
    void shouldThrowNotFoundException() throws Exception {
        User user3 = new User(
                "mail@mail.ru",
                "login3",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user3);
        // контроллер присвоит пользователю самый первый id, т.е. = 1
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        // поставим вручную пользователю id = 3, и попробуем обновить его имя
        user3.setId(3);
        user3.setName("OneMore Name");
        req = objectMapper.writeValueAsString(user3);
        // должны получить статус 404
        mockMvc.perform(put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldThrowExceptionTryingToCreateUserBecauseOfWrongBirthdayDate() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.now().plusDays(1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Дата рождения не может быть в будущем")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateUserBecauseOfWrongBirthdayDate() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        user1.setId(1);
        user1.setBirthday(LocalDate.now().plusDays(1));
        req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Дата рождения не может быть в будущем")
                );
    }

    @Test
    void shouldThrowExceptionTryingToCreateUserBecauseOfFailLogin() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Логин не может быть пустым")
                );
        User user2 = new User(
                "mail@mail.ru",
                "login 1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        req = objectMapper.writeValueAsString(user2);
        mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Логин не может содержать пробелы")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateUserBecauseOfFailLogin() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        user1.setId(1);
        user1.setLogin("");
        req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Логин не может быть пустым")
                );
        user1.setLogin("login 1");
        req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getMessage().equals("Логин не может содержать пробелы")
                );
    }

    @Test
    void shouldThrowExceptionTryingToCreateUserBecauseOfFailEmail() throws Exception {
        User user1 = new User(
                "",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Электронная почта не может быть пустой")
                );
        User user2 = new User(
                "mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        req = objectMapper.writeValueAsString(user2);
        mockMvc.perform(
                        post("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Электронная почта не соответствует формату")
                );
    }

    @Test
    void shouldThrowExceptionTryingToUpdateUserBecauseOfFailEmail() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        user1.setId(1);
        user1.setEmail("");
        req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Электронная почта не может быть пустой")
                );
        user1.setEmail("mail.ru");
        req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(
                        put("/users").content(req).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException()
                                .getMessage()
                                .equals("Электронная почта не соответствует формату")
                );
    }

    @Test
    void shouldReturnStatus400TryingToPostEmptyRequest() throws Exception {
        mockMvc.perform(post("/users").content("").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void shouldReturnStatus400TryingToPutEmptyRequest() throws Exception {
        User user1 = new User(
                "mail@mail.ru",
                "login1",
                "New Name",
                LocalDate.of(1900, 1, 1)
        );
        String req = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/users").content(req).contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/users").content("").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

}

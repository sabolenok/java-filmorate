package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private User user1;
    private User user2;

    @BeforeEach
    public void createTestUsers() {
        user1 = new User("test@test.ru", "test1", "test1_user", LocalDate.of(1990, 1, 1));
        userStorage.create(user1);
        user2 = new User("test2@test.ru", "test2", "test2_user", LocalDate.of(1990, 2, 2));
        userStorage.create(user2);
    }

    @Test
    public void shouldReturnTwoUsers() {
        Assertions.assertEquals(userStorage.findAll().size(), 2);
    }

    @Test
    public void shouldReturnUserById() {
        Optional<User> userOptional = userStorage.findUserById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void shouldThrowNotFoundExceptionCreateUser() {
        Assertions.assertThrows(NotFoundException.class, () -> userStorage.findUserById(-1));
    }

    @Test
    public void shouldUpdateUser() {
        user2.setName("new name");
        userStorage.put(user2);
        assertThat(userStorage.findUserById(2))
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "new name")
                );
    }

    @Test
    public void shouldThrowNotFoundExceptionUpdateUser() {
        User unknown = new User("test@test.ru", "unknown", "the_user_has_no_name", LocalDate.of(1990, 9, 9));
        Assertions.assertThrows(NotFoundException.class, () -> userStorage.put(unknown));
    }

    @Test
    void shouldAddJustOneFriend() {
        userStorage.addFriend(user1.getId(), user2.getId());
        Assertions.assertTrue(userStorage.getUsersFriends(user1.getId()).contains(user2));
        Assertions.assertFalse(userStorage.getUsersFriends(user2.getId()).contains(user1));
    }

    @Test
    void shouldAddBothToFriends() {
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());
        Assertions.assertTrue(userStorage.getUsersFriends(user1.getId()).contains(user2));
        Assertions.assertTrue(userStorage.getUsersFriends(user2.getId()).contains(user1));
    }

    @Test
    void shouldAddAndRemoveFriends() {
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());
        Assertions.assertTrue(userStorage.getUsersFriends(user1.getId()).contains(user2));
        Assertions.assertTrue(userStorage.getUsersFriends(user2.getId()).contains(user1));
        userStorage.removeFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user2.getId(), user1.getId());
        Assertions.assertFalse(userStorage.getUsersFriends(user1.getId()).contains(user2));
        Assertions.assertFalse(userStorage.getUsersFriends(user2.getId()).contains(user1));
    }

}
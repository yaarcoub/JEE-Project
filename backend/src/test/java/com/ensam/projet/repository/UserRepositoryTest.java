package com.ensam.projet.repository;

import com.ensam.projet.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:userdb_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAndCheckExistenceByUsernameAndEmail() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@ensam.ma");
        user.setPassword("hashed_password");
        user.setEnabled(true);
        userRepository.save(user);

        // Act & Assert : Find
        Optional<User> foundByUsername = userRepository.findByUsername("testuser");
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getEmail()).isEqualTo("testuser@ensam.ma");

        Optional<User> foundByEmail = userRepository.findByEmail("testuser@ensam.ma");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getUsername()).isEqualTo("testuser");

        // Act & Assert : Exists
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByEmail("testuser@ensam.ma")).isTrue();
        
        // Assert : Not Exists
        assertThat(userRepository.existsByUsername("unknown")).isFalse();
        assertThat(userRepository.existsByEmail("unknown@ensam.ma")).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userRepository.findByUsername("ghost_user");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveUserAndGenerateId() {
        User user = new User();
        user.setUsername("new_user");
        user.setEmail("new@ensam.ma");
        user.setPassword("pass");
        
        User savedUser = userRepository.save(user);
        
        assertThat(savedUser.getId()).isNotNull();
    }
}
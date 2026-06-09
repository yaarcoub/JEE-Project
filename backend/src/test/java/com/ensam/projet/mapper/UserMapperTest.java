package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.entity.Role;
import com.ensam.projet.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void shouldReturnNullWhenUserIsNull() {
        assertThat(userMapper.toResponse(null)).isNull();
    }

    @Test
    void shouldMapUserWithNullRoles() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(null); // Cas spécifique à tester

        UserResponse response = userMapper.toResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRoles()).isNull();
    }

    @Test
    void shouldMapUserWithRoles() {
        User user = new User();
        user.setId(2L);
        user.setUsername("admin");
        
        // On mock le Role et on lui dit de renvoyer la première valeur disponible dans ton enum ERole
        Role mockRole = mock(Role.class);
        when(mockRole.getName()).thenReturn(com.ensam.projet.entity.ERole.values()[0]);

        Set<Role> roles = new HashSet<>();
        roles.add(mockRole);
        user.setRoles(roles);

        UserResponse response = userMapper.toResponse(user);

        assertThat(response).isNotNull();
        // On vérifie que la liste contient bien le nom (String) de l'enum qui a été mocké
        assertThat(response.getRoles()).containsExactly(com.ensam.projet.entity.ERole.values()[0].name());
    }
}
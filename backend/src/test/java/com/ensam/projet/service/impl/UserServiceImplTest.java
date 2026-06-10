package com.ensam.projet.service.impl;

import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.entity.User;
import com.ensam.projet.exception.ResourceNotFoundException;
import com.ensam.projet.mapper.UserMapper;
import com.ensam.projet.repository.RoleRepository;
import com.ensam.projet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import com.ensam.projet.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("youssef");
        user.setEmail("youssef@ensam.ma");
        user.setEnabled(true);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("youssef");
        userResponse.setEmail("youssef@ensam.ma");
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("youssef");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldGetAllUsersWithPagination() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        PagedResponse<UserResponse> result = userService.getAllUsers(0, 10);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void shouldToggleUserEnabledStatus() {
        // Arrange : L'utilisateur est activé (true)
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // Act
        userService.toggleEnabled(1L);

        // Assert : Vérifie qu'il a été passé à false avant la sauvegarde
        assertThat(user.isEnabled()).isFalse(); 
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordWithWrongOldPassword() {
        // 1. Simuler proprement le SecurityContext et le Principal (UserDetails)
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("youssef"); // C'est ici que votre getCurrentUsername() va lire le nom
        SecurityContextHolder.setContext(securityContext);

        // 2. Simuler la recherche dans la BDD (maintenant qu'on a bien récupéré "youssef")
        user.setPassword("hashed_old_pass");
        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));

        // 3. Simuler l'échec de la vérification du mot de passe
        when(passwordEncoder.matches("wrong_old_pass", "hashed_old_pass")).thenReturn(false);

        // 4. Act & Assert
        assertThrows(BadRequestException.class, () -> 
            userService.changePassword("wrong_old_pass", "new_pass")
        );

        // Nettoyage
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // 1. Simuler proprement le SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        // 2. Simuler la BDD et le succès du mot de passe
        user.setPassword("hashed_old_pass");
        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct_old_pass", "hashed_old_pass")).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("new_hashed_pass");

        // 3. Act
        userService.changePassword("correct_old_pass", "new_pass");

        // 4. Assert
        assertThat(user.getPassword()).isEqualTo("new_hashed_pass");
        verify(userRepository).save(user);

        // Nettoyage
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateRolesSuccessfully() {
        Set<String> requestedRoles = new HashSet<>();
        requestedRoles.add("ADMIN");
        requestedRoles.add("ROLE_USER");

        com.ensam.projet.entity.Role adminRole = new com.ensam.projet.entity.Role();
        adminRole.setName(com.ensam.projet.entity.ERole.ROLE_ADMIN);
        
        com.ensam.projet.entity.Role userRole = new com.ensam.projet.entity.Role();
        userRole.setName(com.ensam.projet.entity.ERole.ROLE_USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(com.ensam.projet.entity.ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(com.ensam.projet.entity.ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateRoles(1L, requestedRoles);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenUpdateRolesUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Set<String> roles = Collections.singleton("ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> userService.updateRoles(1L, roles));
    }

    @Test
    void shouldThrowWhenUpdateRolesRoleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(com.ensam.projet.entity.ERole.ROLE_ADMIN)).thenReturn(Optional.empty());
        
        Set<String> roles = Collections.singleton("ADMIN");

        assertThrows(BadRequestException.class, () -> userService.updateRoles(1L, roles));
    }

    @Test
    void shouldThrowWhenToggleEnabledUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.toggleEnabled(1L));
    }

    @Test
    void shouldThrowWhenDeleteUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("youssef");

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetCurrentUserAsStringPrincipal() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getCurrentUser();

        assertThat(result).isNotNull();

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newUsername")).thenReturn(false);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateProfile("newUsername", "new@email.com");

        assertThat(result).isNotNull();
        verify(userRepository).save(user);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowWhenUpdateProfileUsernameTaken() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("takenUsername")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateProfile("takenUsername", "new@email.com"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowWhenUpdateProfileEmailTaken() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("youssef");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("youssef")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateProfile("youssef", "taken@email.com"));

        SecurityContextHolder.clearContext();
    }

}
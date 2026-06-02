package com.ensam.projet.service.impl;

import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.entity.ERole;
import com.ensam.projet.entity.Role;
import com.ensam.projet.entity.User;
import com.ensam.projet.exception.BadRequestException;
import com.ensam.projet.exception.ResourceNotFoundException;
import com.ensam.projet.mapper.UserMapper;
import com.ensam.projet.repository.RoleRepository;
import com.ensam.projet.repository.UserRepository;
import com.ensam.projet.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));
        return new PagedResponse<>(users.stream().map(userMapper::toResponse).toList(),
                users.getNumber(), users.getTotalPages(), users.getTotalElements(), users.getSize(), users.isLast());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateRoles(Long id, Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roles) {
            String normalizedRole = roleName.toUpperCase();
            if (!normalizedRole.startsWith("ROLE_")) {
                normalizedRole = "ROLE_" + normalizedRole;
            }
            ERole eRole = ERole.valueOf(normalizedRole);
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new BadRequestException("Role introuvable: " + roleName));
            newRoles.add(role);
        }
        user.setRoles(newRoles);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setEnabled(!user.isEnabled());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur introuvable");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse getCurrentUser() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(String username, String email) {
        String current = getCurrentUsername();
        User user = userRepository.findByUsername(current)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new BadRequestException("Nom d'utilisateur déjà utilisé");
        }
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email déjà utilisé");
        }
        user.setUsername(username);
        user.setEmail(email);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String currentUsername = getCurrentUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}

package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.entity.Role;
import com.ensam.projet.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());

        // mapping roles → Set<String>
        response.setRoles(mapRoles(user.getRoles()));

        return response;
    }

    private Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;

        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
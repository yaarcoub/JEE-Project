package com.ensam.projet.service.interfaces;

import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;

import java.util.Set;

public interface UserService {
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse getUserById(Long id);
    UserResponse updateRoles(Long id, Set<String> roles);
    UserResponse toggleEnabled(Long id);
    void deleteUser(Long id);
    UserResponse getCurrentUser();
    UserResponse updateProfile(String username, String email);
    void changePassword(String oldPassword, String newPassword);
}

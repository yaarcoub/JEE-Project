package com.ensam.projet.service.interfaces;

import com.ensam.projet.dto.request.LoginRequest;
import com.ensam.projet.dto.request.RegisterRequest;
import com.ensam.projet.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout();
}

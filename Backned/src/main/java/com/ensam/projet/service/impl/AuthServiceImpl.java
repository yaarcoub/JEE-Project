package com.ensam.projet.service.impl;

import com.ensam.projet.dto.request.LoginRequest;
import com.ensam.projet.dto.request.RegisterRequest;
import com.ensam.projet.dto.response.AuthResponse;
import com.ensam.projet.entity.ERole;
import com.ensam.projet.entity.Role;
import com.ensam.projet.entity.User;
import com.ensam.projet.exception.BadRequestException;
import com.ensam.projet.repository.RoleRepository;
import com.ensam.projet.repository.UserRepository;
import com.ensam.projet.security.JwtUtil;
import com.ensam.projet.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Nom d'utilisateur déjà utilisé");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email déjà utilisé");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Role utilisateur non trouvé"));
        user.getRoles().add(role);
        userRepository.save(user);
        System.out.println(user.getPassword());
        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

       // System.out.println("Admin :"+passwordEncoder.encode("admin123"));
       // System.out.println("Manager :"+passwordEncoder.encode("Manager123"));


        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        if (!jwtUtil.isTokenValid(refreshToken, org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                .toList()
                ).build())) {
            throw new BadRequestException("Refresh token invalide");
        }
        String accessToken = jwtUtil.generateAccessToken(org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                .toList()
                ).build());
        String refresh = jwtUtil.generateRefreshToken(org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                .toList()
                )
                .build());
        return new AuthResponse(accessToken, refresh, "Bearer", user.getUsername(), user.getEmail());
    }

    @Override
    public void logout() {
    }

    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toList()
        )
                .build();
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        return new AuthResponse(accessToken, refreshToken, "Bearer", user.getUsername(), user.getEmail());
    }
}

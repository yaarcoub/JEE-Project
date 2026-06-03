package com.ensam.projet.repository;

import com.ensam.projet.entity.ERole;
import com.ensam.projet.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}

package com.oexil.studentreg.repository;

import com.oexil.studentreg.enums.ERole;
import com.oexil.studentreg.model.masters.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}

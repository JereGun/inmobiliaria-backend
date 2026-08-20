package dev.jgunsett.inmobiliaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
	Optional<UserGroup> findByNameIgnoreCase(String name);
	boolean existsByNameIgnoreCase(String name);
}

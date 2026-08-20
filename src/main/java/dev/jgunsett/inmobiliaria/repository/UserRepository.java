package dev.jgunsett.inmobiliaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import dev.jgunsett.inmobiliaria.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@EntityGraph(attributePaths = {"groups"})
	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);

	List<User> findByActiveTrue();

}

package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.UserMapper;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	
	public UserResponse create(UserCreateRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException("El Email " + request.getEmail() + " ya se encuentra registrado");
		}
		
		User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
		User saved = userRepository.save(user);

		return userMapper.toResponse(saved);
	}

	public UserResponse update(Long id, UserUpdateRequest request) {
		User user = findEntityById(id);

		if (request.getEmail() != null
				&& !request.getEmail().equals(user.getEmail())
				&& userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException("El Email " + request.getEmail() + " ya se encuentra registrado");
		}

		if (request.getEmail() != null) {
			user.setEmail(request.getEmail());
		}
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (request.getRole() != null) {
			user.setRole(request.getRole());
		}
		if (request.getActive() != null) {
			user.setActive(request.getActive());
		}
		if (request.getVerified() != null) {
			user.setVerified(request.getVerified());
		}

		return userMapper.toResponse(user);
	}

	public UserResponse updateStatus(Long id, boolean active) {
		User user = findEntityById(id);
		user.setActive(active);

		return userMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		return userMapper.toResponse(findEntityById(id));
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userRepository.findAll(pageable)
				.map(userMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public UserResponse getByEmail(String email) {
		return userMapper.toResponse(findEntityByEmail(email));
	}

	@Transactional(readOnly = true)
	public User findEntityByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontro Usuario con email: " + email));
	}

	@Transactional(readOnly = true)
	public User findEntityById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontro Usuario con el ID: " + id));
	}

	public void registerLogin(String email) {
		User user = findEntityByEmail(email);
		user.setLastLogin(java.time.LocalDateTime.now());
	}
}

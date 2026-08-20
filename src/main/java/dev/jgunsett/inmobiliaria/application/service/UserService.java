package dev.jgunsett.inmobiliaria.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.UserMapper;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.entity.UserGroup;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import dev.jgunsett.inmobiliaria.repository.UserGroupRepository;

@Service
@Transactional
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final UserGroupRepository userGroupRepository;

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, UserGroupRepository userGroupRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userMapper = userMapper;
		this.userGroupRepository = userGroupRepository;
	}

	/**
	 * Compatibilidad con los tests y consumidores que crean el servicio sin gestión de grupos.
	 */
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
		this(userRepository, passwordEncoder, userMapper, null);
	}
	
	public UserResponse create(UserCreateRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException("El Email " + request.getEmail() + " ya se encuentra registrado");
		}
		
		User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
		user.setGroups(resolveGroups(request.getGroupIds()));
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
		if (request.getDisplayName() != null) {
			user.setDisplayName(request.getDisplayName().trim());
		}
		if (request.getWhatsapp() != null) {
			user.setWhatsapp(request.getWhatsapp().trim());
		}
		if (request.getAvatarPositionX() != null) user.setAvatarPositionX(request.getAvatarPositionX());
		if (request.getAvatarPositionY() != null) user.setAvatarPositionY(request.getAvatarPositionY());
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
		if (request.getGroupIds() != null) {
			user.setGroups(resolveGroups(request.getGroupIds()));
		}

		return userMapper.toResponse(user);
	}

	public UserResponse updateStatus(Long id, boolean active) {
		User user = findEntityById(id);
		user.setActive(active);

		return userMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public java.util.List<UserResponse> getActiveAgents() {
		return userRepository.findByActiveTrue().stream().map(userMapper::toResponse).toList();
	}

	public UserResponse uploadAvatar(Long id, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("La imagen de perfil está vacía");
		}
		if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
			throw new BusinessException("La imagen de perfil debe ser un archivo de imagen");
		}

		User user = findEntityById(id);
		String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
		String extension = originalName.contains(".")
				? originalName.substring(originalName.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.]", "")
				: ".jpg";
		String fileName = UUID.randomUUID() + extension.toLowerCase();
		Path uploadPath = Paths.get("uploads", "user-avatars");
		try {
			Files.createDirectories(uploadPath);
			Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
			deleteAvatarFile(user.getAvatarUrl());
		} catch (IOException ex) {
			throw new BusinessException("No se pudo guardar la imagen de perfil");
		}
		user.setAvatarUrl("/uploads/user-avatars/" + fileName);
		return userMapper.toResponse(user);
	}

	private void deleteAvatarFile(String avatarUrl) throws IOException {
		if (avatarUrl == null || !avatarUrl.startsWith("/uploads/user-avatars/")) return;
		String fileName = avatarUrl.substring("/uploads/user-avatars/".length());
		if (!fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")) {
			Files.deleteIfExists(Paths.get("uploads", "user-avatars", fileName));
		}
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

	private java.util.Set<UserGroup> resolveGroups(java.util.Set<Long> groupIds) {
		if (groupIds == null || groupIds.isEmpty()) {
			return new java.util.HashSet<>();
		}
		java.util.List<UserGroup> groups = userGroupRepository.findAllById(groupIds);
		if (groups.size() != groupIds.size()) {
			throw new ResourceNotFoundException("Uno o más grupos de usuarios no existen");
		}
		return new java.util.HashSet<>(groups);
	}
}

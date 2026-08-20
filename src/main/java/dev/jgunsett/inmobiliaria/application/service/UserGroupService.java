package dev.jgunsett.inmobiliaria.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.entity.UserGroup;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.UserGroupRepository;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserGroupService {

	private final UserGroupRepository groupRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<UserGroupResponse> getAll() {
		return groupRepository.findAll().stream().map(this::toResponse).toList();
	}

	public UserGroupResponse create(UserGroupCreateRequest request) {
		if (groupRepository.existsByNameIgnoreCase(request.getName().trim())) {
			throw new BusinessException("El grupo " + request.getName() + " ya existe");
		}
		UserGroup group = UserGroup.builder()
				.name(request.getName().trim())
				.description(request.getDescription())
				.permissions(request.getPermissions() == null ? new HashSet<>() : new HashSet<>(request.getPermissions()))
				.build();
		UserGroup saved = groupRepository.save(group);
		updateMembers(saved, request.getMemberIds());
		return toResponse(saved);
	}

	public UserGroupResponse update(Long id, UserGroupUpdateRequest request) {
		UserGroup group = findById(id);
		if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(group.getName())
				&& groupRepository.existsByNameIgnoreCase(request.getName().trim())) {
			throw new BusinessException("El grupo " + request.getName() + " ya existe");
		}
		if (request.getName() != null && !request.getName().isBlank()) group.setName(request.getName().trim());
		if (request.getDescription() != null) group.setDescription(request.getDescription());
		group.setPermissions(request.getPermissions() == null ? new HashSet<>() : new HashSet<>(request.getPermissions()));
		updateMembers(group, request.getMemberIds());
		return toResponse(group);
	}

	private void updateMembers(UserGroup group, Set<Long> memberIds) {
		Set<Long> desiredIds = memberIds == null ? Set.of() : memberIds;
		List<User> desiredUsers = userRepository.findAllById(desiredIds);
		if (desiredUsers.size() != desiredIds.size()) {
			throw new ResourceNotFoundException("Uno o más usuarios no existen");
		}

		for (User current : new HashSet<>(group.getUsers())) {
			current.getGroups().remove(group);
		}
		group.getUsers().clear();
		for (User user : desiredUsers) {
			user.getGroups().add(group);
			group.getUsers().add(user);
		}
		userRepository.saveAll(desiredUsers);
	}

	@Transactional(readOnly = true)
	public UserGroup findById(Long id) {
		return groupRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró el grupo con ID: " + id));
	}

	private UserGroupResponse toResponse(UserGroup group) {
		Set<Long> memberIds = group.getUsers().stream().map(User::getId).collect(java.util.stream.Collectors.toSet());
		return UserGroupResponse.builder()
				.id(group.getId())
				.name(group.getName())
				.description(group.getDescription())
				.permissions(new HashSet<>(group.getPermissions()))
				.memberIds(memberIds)
				.memberCount(memberIds.size())
				.build();
	}
}

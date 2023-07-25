package ru.practicum.user.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto save(User user) {
        User checkDuplicate = userRepository.findByName(user.getName());
        if (checkDuplicate != null) {
            throw new ConflictException("The name is not unique");
        }
        return UserMapper.toDto(userRepository.save(user));
    }

    public List<UserDto> get(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        List<User> users;
        if (ids == null) {
            users = userRepository.findAllBy(pageable);
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    public void delete(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("User with id=" + id + " was not found"));
    }
}
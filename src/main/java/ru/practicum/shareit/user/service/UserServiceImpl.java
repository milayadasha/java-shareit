package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Возвращает всех пользователей
     */
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    /**
     * Возвращает пользователя по id
     */
    @Override
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + id + " не найден")));
    }

    /**
     * Добавляет нового пользователя
     */
    @Override
    @Transactional
    public UserDto addNewUser(NewUserDto newUserDto) {
        User newUser = UserMapper.toUser(newUserDto);
        checkIsValidUser(newUser);
        log.info("Пользователь с email {} прошёл валидацию входных данных и готов к добавлению", newUser.getEmail());
        return UserMapper.toUserDto(userRepository.save(newUser));
    }

    /**
     * Обновляет пользователя
     */
    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        User updateUser = UserMapper.toUser(updateUserDto);
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + id + " не найден"));
        checkIsValidUser(updateUser);
        log.info("Пользователь с id {} прошёл валидацию входных данных и готов к обновлению", id);
        updateUserData(currentUser, updateUser, id);
        return UserMapper.toUserDto(userRepository.save(updateUser));
    }

    /**
     * Удаляет пользователя
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.trace("Пользователь с id {} удалён", id);
    }

    /**
     * Проверяет переданного пользователя на соответствие условиям.
     * Если не удовлетворяет какой-то проверке, то выбрасывается ошибка
     */
    private void checkIsValidUser(User checkUser) throws ValidationException {
        if (checkUser == null) {
            String nullError = "Пользователь для валидации входных параметров не найден";
            log.error(nullError);
            throw new NotFoundException(nullError);
        }

        if (checkUser.getEmail() != null && !checkUser.getEmail().contains("@")) {
            String emailError = "У пользователя должен быть email в корректном формате";
            log.error(emailError);
            throw new ValidationException(emailError);
        }

        List<User> users = userRepository.findAll();
        Optional<User> sameUserByEmail = users.stream()
                .filter(user -> user.getEmail().equals(checkUser.getEmail()))
                .findAny();
        if (sameUserByEmail.isPresent()) {
            String sameEmailError = "Пользователь с email " + checkUser.getEmail() + " уже существует.";
            log.error(sameEmailError);
            throw new DuplicateException(sameEmailError);
        }
    }

    /**
     * Обновляет пользователя данными из DTO-объекта
     */
    private void updateUserData(User currentUser, User updatedUser, Long id) {
        updatedUser.setId(id);
        if (updatedUser.getEmail() == null || updatedUser.getEmail().isEmpty()) {
            updatedUser.setEmail(currentUser.getEmail());
        }
        if (updatedUser.getName() == null || updatedUser.getName().isEmpty()) {
            updatedUser.setName(currentUser.getName());
        }
    }
}

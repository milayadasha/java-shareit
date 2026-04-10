package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Начался процесс получения всех пользователей");
        return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Начался процесс получения пользователя по id {}", id);
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + id + " не найден")));
    }

    @Override
    @Transactional
    public UserDto addNewUser(NewUserDto newUserDto) {
        log.info("Начался процесс добавления пользователя с email {}", newUserDto.getEmail());
        User newUser = UserMapper.toUser(newUserDto);
        log.info("Запрос на добавления пользователя с email {} преобразован в объект пользователя",
                newUserDto.getEmail());
        checkIsValidUser(newUser);
        log.info("Пользователь с email {} прошёл валидацию входных данных и готов к добавлению", newUser.getEmail());
        return UserMapper.toUserDto(userRepository.save(newUser));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        log.info("Начался процесс обновления пользователя {}", id);
        User updateUser = UserMapper.toUser(updateUserDto);
        log.info("Запрос на обновление пользователя {} преобразован в объект пользователя", id);

        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + id + " не найден"));
        log.info("Пользователя {} найден в БД ", id);

        checkIsValidUser(updateUser);
        log.info("Пользователь с id {} прошёл валидацию входных данных и готов к обновлению", id);
        updateUserData(currentUser, updateUser, id);
        return UserMapper.toUserDto(userRepository.save(currentUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Начался процесс удаления пользователя {}", id);
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
        log.info("Началась внутренняя проверка пользователя {}", checkUser.getId());

        if (checkUser.getEmail() != null && !checkUser.getEmail().contains("@")) {
            String emailError = "У пользователя должен быть email в корректном формате";
            log.error(emailError);
            throw new ValidationException(emailError);
        }
        log.info("Пройдена проверка email {}", checkUser.getEmail());

        List<User> users = userRepository.findAll();
        Optional<User> sameUserByEmail = users.stream()
                .filter(user -> user.getEmail().equals(checkUser.getEmail()))
                .findAny();
        if (sameUserByEmail.isPresent()) {
            String sameEmailError = "Пользователь с email " + checkUser.getEmail() + " уже существует.";
            log.error(sameEmailError);
            throw new DuplicateException(sameEmailError);
        }
        log.info("Пройдена проверка на уникальность email {}", checkUser.getEmail());
    }

    /**
     * Обновляет пользователя данными из DTO-объекта
     */
    private void updateUserData(User currentUser, User updatedUser, Long id) {
        log.info("Началась процесс обновления данных пользователя {}", id);
        updatedUser.setId(id);
        log.info("Пользователю установлен id {}", id);
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            currentUser.setEmail(updatedUser.getEmail());
            log.info("Пользователю установлен email {}", updatedUser.getEmail());
        }

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            currentUser.setName(updatedUser.getName());
            log.info("Пользователю установлено имя {}", updatedUser.getName());
        }
    }
}

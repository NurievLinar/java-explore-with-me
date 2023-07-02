package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.user.mapper.UserMapper.toUser;
import static ru.practicum.ewm.user.mapper.UserMapper.toUserDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Transactional
    public NewUserDto createUser(NewUserDto newUserDto) {
        User user = toUser(newUserDto);
        try {
            return toUserDto(repository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Почта, имя пользователя уже используется");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Запрос составлен не корректно ");
        }
    }

    public List<NewUserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        if (ids == null) {
            return repository.findAll(page).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findByIdIn(ids, page).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }


    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}

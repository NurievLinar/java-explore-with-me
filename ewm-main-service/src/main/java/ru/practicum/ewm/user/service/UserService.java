package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserDto;

import java.util.List;

public interface UserService {
    NewUserDto createUser(NewUserDto newUserDto);

    List<NewUserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long id);
}

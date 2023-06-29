package ru.practicum.ewm.user.controller;

import ru.practicum.ewm.user.dto.NewUserDto;
import ru.practicum.ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewUserDto createUser(@RequestBody @Valid NewUserDto newUserDto) {
        log.info("Сохранение пользователя.");
        return userService.createUser(newUserDto);
    }

    @GetMapping
    public List<NewUserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получение списка пользователей.");
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Удаление пользователя.");
        userService.deleteUser(id);
    }
}

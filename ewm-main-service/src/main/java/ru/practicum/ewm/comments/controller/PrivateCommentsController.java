package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentUpdateDto;
import ru.practicum.ewm.comments.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentsController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @RequestBody CommentDto commentDto) {
        log.info("Добавление комментария к событию");
        return commentService.saveComment(commentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long commentId,
                                    @PathVariable Long userId,
                                    @RequestBody CommentUpdateDto commentDto) {
        log.info("Обновление комментария пользователем");
        return commentService.updateComment(commentId, userId, commentDto);
    }

    @GetMapping
    public List<CommentDto> getAllCommentsByUser(@PathVariable Long userId,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение списка комментариев");
        return commentService.getAllCommentsByUser(userId, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void userDeleteComment(@PathVariable Long userId,
                                  @PathVariable Long commentId) {
        log.info("Удаление комментария пользователем");
        commentService.userDeleteComment(commentId, userId);
    }
}

package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentUpdateDto;

import java.util.List;

public interface CommentService {
    CommentDto saveComment(CommentDto commentDto, Long userId, Long eventId);

    CommentDto updateComment(Long commentId, Long userId, CommentUpdateDto commentUpdateDto);

    List<CommentDto> getAllCommentsByUser(Long userId, Integer from, Integer size);

    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size);

    void userDeleteComment(Long commentId, Long userId);

    void adminDeleteComment(Long commentId);
}

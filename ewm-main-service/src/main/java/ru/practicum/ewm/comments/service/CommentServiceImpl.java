package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.CommentUpdateDto;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventsRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

import static ru.practicum.ewm.comments.mapper.CommentMapper.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventsRepository eventsRepository;

    @Override
    public CommentDto saveComment(CommentDto commentDto, Long userId, Long eventId) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (!State.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Комментарий можно оставить только под опубликованным событием.");
        }
        Comment comment = toComment(commentDto, user, event);
        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long commentId, Long userId, CommentUpdateDto commentUpdateDto) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new ConflictException("Только автор может изменить комментарий."));
        comment.setText(commentUpdateDto.getText());
        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByUser(Long userId, Integer from, Integer size) {
        User user = getUser(userId);
        return toCommentsDto(commentRepository.findAllByUser(user, PageRequest.of(from / size, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        Event event = getEvent(eventId);
        return toCommentsDto(commentRepository.findAllByEvent(event, PageRequest.of(from / size, size)));
    }

    @Override
    public void userDeleteComment(Long commentId, Long userId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        if (!Objects.equals(comment.getUser().getId(), userId)) {
            throw new ConflictException("Только автор может удалить комментарий.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void adminDeleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий не найден.");
        }
        commentRepository.deleteById(commentId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Неверный ID пользователя."));
    }

    private Event getEvent(Long eventId) {
        return eventsRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Неверный ID события."));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Неверный ID комментария."));
    }
}

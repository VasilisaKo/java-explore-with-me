package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdatedCommentDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventPublicService;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventPublicService eventPublicService;

    public CommentDto postComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        Event event = eventPublicService.findById(eventId);
        User user = userService.findById(userId); //любой пользователь может оставить комментарий к любому событию
        newCommentDto.setCreated(LocalDateTime.now());
        Comment comment = CommentMapper.toEntity(newCommentDto, user, event);
        comment.setUpdated(null);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    public UpdatedCommentDto patchComment(NewCommentDto dto, Long userId, Long commentId) {
        userService.findById(userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException("The comment hasn't been found"));
        if (comment.getUser().getId() != userId) {
            throw new BadRequestException("Wrong user or event id"); //Изменить комментарий может только автор
        }
        if (!dto.getContent().equals(comment.getContent())) {
            comment.setContent(dto.getContent());
        } else {
            throw new ConflictException("There is nothing to update");
        }
        comment.setUpdated(LocalDateTime.now());
        commentRepository.save(comment);
        return CommentMapper.toUpdatedCommentDto(comment);
    }

    public void deleteComment(Long userId, Long commentId) {
        userService.findById(userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException("The comment hasn't been found"));
        if (comment.getUser().getId() != userId) {
            throw new ConflictException("User have no rights to delete the comment");
        } //удалить комментарий может только автор
        commentRepository.delete(comment);
    }

    public List<CommentDto> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getAllCommentsByEvent(Long eventId) { //подборка комментариев к определенному событию
        eventPublicService.findById(eventId);
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ObjectNotFoundException("The comment hasn't been found"));
        return CommentMapper.toCommentDto(comment);
    }

    public List<CommentDto> getAllCommentsByUser(Long userId) { //подборка комментариев определенного пользователя
        userService.findById(userId);
        List<Comment> comments = commentRepository.findAllByUserId(userId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
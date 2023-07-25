package ru.practicum.comments.mapper;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdatedCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

public class CommentMapper {
    public static Comment toEntity(NewCommentDto dto, User user, Event event) {
        return Comment.builder()
                .content(dto.getContent())
                .created(dto.getCreated())
                .user(user)
                .event(event)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .created(comment.getCreated())
                .content(comment.getContent())
                .user(UserMapper.toDtoShort(comment.getUser()))
                .event(EventMapper.toShortDto(comment.getEvent()))
                .build();
    }

    public static UpdatedCommentDto toUpdatedCommentDto(Comment comment) {
        return UpdatedCommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .updated(comment.getUpdated())
                .user(UserMapper.toDtoShort(comment.getUser()))
                .event(EventMapper.toShortDto(comment.getEvent()))
                .build();
    }
}

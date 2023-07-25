package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdatedCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
@Slf4j
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postComment(@RequestBody @Valid NewCommentDto dto,
                                  @RequestParam Long userId,
                                  @RequestParam Long eventId) {
        return commentService.postComment(dto, userId, eventId);
    }

    @PatchMapping(value = "/{commentId}")
    public UpdatedCommentDto patchComment(@RequestBody @Valid NewCommentDto dto,
                                          @RequestParam Long userId,
                                          @PathVariable Long commentId) {
        return commentService.patchComment(dto, userId, commentId);
    }

    @DeleteMapping(value = "/{commentId}")
    public void deleteComment(@RequestParam Long userId,
                              @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping(value = "/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    @GetMapping
    public List<CommentDto> getAllComments() {
        return commentService.getAllComments();
    }

    @GetMapping(value = "/events/{eventId}")
    public List<CommentDto> getAllCommentsByEvent(@PathVariable Long eventId) {
        return commentService.getAllCommentsByEvent(eventId);
    }

    @GetMapping(value = "/users/{userId}")
    public List<CommentDto> getAllCommentsByUser(@PathVariable Long userId) {
        return commentService.getAllCommentsByUser(userId);
    }
}
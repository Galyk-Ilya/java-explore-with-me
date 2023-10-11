package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{eventId}")
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @RequestBody @Valid NewCommentDto commentDto) {
        CommentDto comment = commentService.addComment(userId, eventId, commentDto);
        log.info("User Id = {} leaves a comment on the event id = {}.", userId, eventId);
        return comment;
    }

    @PatchMapping("/{commentId}")
    public CommentDto commentDto(@PathVariable Long userId,
                                 @PathVariable Long commentId,
                                 @RequestBody @Valid NewCommentDto commentDto) {
        CommentDto comment = commentService.updateComment(userId, commentId, commentDto);
        log.info("User id = {}, edited comment id = {}.", userId, commentId);
        return comment;
    }

    @GetMapping
    public List<CommentDto> findAllCommentsUser(@PathVariable Long userId) {
        List<CommentDto> comment = commentService.findCommentsUser(userId);
        log.info("Getting a list of user comments id = {}.", userId);
        return comment;
    }

    @GetMapping("/{commentId}")
    public CommentDto findComment(@PathVariable Long userId,
                                  @PathVariable Long commentId) {
        CommentDto comment = commentService.findComment(userId, commentId);
        log.info("Receive a comment id = {}.", commentId);
        return comment;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        log.info("Comment from id = {} deleted by user id = {}.", commentId, userId);
    }
}
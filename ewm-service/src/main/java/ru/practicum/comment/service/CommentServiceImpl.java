package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.error.exceptions.WrongEventDateException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto commentDto) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("You do not have access to post comments."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        return CommentMapper.toCommentDto(commentRepository
                .save(CommentMapper.toComment(creator, event, commentDto)));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto) {
        Comment oldComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
        oldComment.setText(commentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(oldComment));
    }

    @Override
    public List<CommentDto> findCommentsUser(Long userId) {
        List<CommentDto> comment = new ArrayList<>();
        List<Comment> comments = commentRepository.findAllByCreatorId(userId);
        comment.addAll(comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        return comment;
    }

    @Override
    public CommentDto findComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("You do not have permission to view"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user data."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
        if (!comment.getCreator().getId().equals(userId)) {
            throw new WrongEventDateException("You can't delete someone else's comment.");
        }
        commentRepository.delete(comment);
    }
}
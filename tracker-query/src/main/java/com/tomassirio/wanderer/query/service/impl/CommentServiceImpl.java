package com.tomassirio.wanderer.query.service.impl;

import com.tomassirio.wanderer.commons.dto.CommentDTO;
import com.tomassirio.wanderer.commons.mapper.CommentMapper;
import com.tomassirio.wanderer.query.repository.CommentRepository;
import com.tomassirio.wanderer.query.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper = CommentMapper.INSTANCE;

    @Override
    public CommentDTO getComment(UUID commentId) {
        return commentRepository
                .findByIdWithUser(commentId)
                .map(commentMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    @Override
    public List<CommentDTO> getCommentsForTrip(UUID tripId) {
        return commentRepository.findTopLevelCommentsByTripId(tripId).stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }
}

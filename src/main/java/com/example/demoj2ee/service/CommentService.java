package com.example.demoj2ee.service;

import com.example.demoj2ee.model.Comment;
import com.example.demoj2ee.model.Movie;
import com.example.demoj2ee.model.User;
import com.example.demoj2ee.repository.CommentRepository;
import com.example.demoj2ee.repository.MovieRepository;
import com.example.demoj2ee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Comment> getCommentsByMovie(Long movieId) {
        return commentRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
    }

    public Comment saveComment(Long movieId, String username, String content, int rating) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        User user = userRepository.findByUsername(username);

        if (movie == null || user == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setMovie(movie);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public boolean deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getUser().getUsername().equals(username)) {
            return false;
        }
        commentRepository.delete(comment);
        return true;
    }
}

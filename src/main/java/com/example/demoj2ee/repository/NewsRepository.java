package com.example.demoj2ee.repository;

import com.example.demoj2ee.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByActiveTrueOrderByPublishedAtDesc();

    List<News> findByCategoryOrderByPublishedAtDesc(String category);
}

package com.example.interviewprep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.interviewprep.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // You can add custom query methods if needed.
}

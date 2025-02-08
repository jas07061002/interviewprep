package com.example.interviewprep.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;   // e.g., "coding", "system design"
    private String difficulty; // e.g., "easy", "medium", "hard"

    @Column(length = 2000)
    private String prompt;

    // Optionally, store the user's submitted answer and feedback.
    @Column(length = 4000)
    private String answer;
}

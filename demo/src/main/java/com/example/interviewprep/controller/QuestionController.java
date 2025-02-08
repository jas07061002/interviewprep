package com.example.interviewprep.controller;

import com.example.interviewprep.model.Question;
import com.example.interviewprep.service.OpenAIService;
import com.example.interviewprep.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;
    private final OpenAIService openAIService;

    public QuestionController(QuestionService questionService, OpenAIService openAIService) {
        this.questionService = questionService;
        this.openAIService = openAIService;
    }

    @GetMapping("/questions")
    public Mono<List<Question>> getAllQuestionsReactive() {
        return Mono.fromCallable(questionService::getAllQuestions);
    }

    // Reactive endpoint to submit an answer and receive AI feedback.
    @PostMapping("/questions/{id}/submit")
    public Mono<String> submitAnswer(@PathVariable Long id, @RequestBody String userAnswer) {
        // Retrieve the question synchronously.
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return Mono.error(new RuntimeException("Question not found"));
        }

        // Evaluate the answer reactively.
        return openAIService.evaluateAnswerReactive(userAnswer, question.getPrompt())
                .doOnNext(feedback -> {
                    // Optionally store the submitted answer and feedback.
                    question.setAnswer(userAnswer + "\nFeedback: " + feedback);
                    questionService.saveQuestion(question);
                });
    }

    // Reactive endpoint to generate a question based on a prompt.
    @GetMapping("/generateReactive")
    public Mono<String> generateReactiveQuestion(@RequestParam String prompt) {
        return openAIService.generateQuestionReactive(prompt);
    }
}

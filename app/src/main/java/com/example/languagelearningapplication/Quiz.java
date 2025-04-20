package com.example.languagelearningapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Quiz extends AppCompatActivity {

    private TextView questionText, questionNumber;
    private RadioButton option1, option2, option3, option4;
    private RadioGroup radioGroup;
    private Button submitButton;
    private ProgressBar progressBar;

    private List<Question> mcqList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private DatabaseReference databaseReference;

    private String userId = "anonymous"; // Default fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        FirebaseApp.initializeApp(this);

        // Attempt to get Firebase Auth user ID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Initialize UI
        questionText = findViewById(R.id.question_text);
        questionNumber = findViewById(R.id.question_number);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        radioGroup = findViewById(R.id.radiogroup);
        submitButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progress_bar);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://salanika-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference("Questions");

        loadQuestions();

        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void loadQuestions() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mcqList.clear();

                if (!snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Quiz.this, "No questions found in database", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Question question = dataSnapshot.getValue(Question.class);
                    if (question != null) {
                        mcqList.add(question);
                    }
                }

                progressBar.setVisibility(View.GONE);
                if (!mcqList.isEmpty()) {
                    showQuestion();
                } else {
                    Toast.makeText(Quiz.this, "No valid questions found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Quiz.this, "Error loading questions: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showQuestion() {
        if (currentQuestionIndex < mcqList.size()) {
            Question currentQ = mcqList.get(currentQuestionIndex);

            questionText.setText(currentQ.getQuestion());
            questionNumber.setText("Question " + (currentQuestionIndex + 1) + "/" + mcqList.size());
            option1.setText(currentQ.getOption1());
            option2.setText(currentQ.getOption2());
            option3.setText(currentQ.getOption3());
            option4.setText(currentQ.getOption4());
            radioGroup.clearCheck();
        } else {
            Toast.makeText(this, "Quiz Completed! Score: " + score, Toast.LENGTH_LONG).show();
            saveScore();
        }
    }

    private void checkAnswer() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String selectedAnswer = selectedRadio.getText().toString();
        String correctAnswer = mcqList.get(currentQuestionIndex).getAnswer();

        if (selectedAnswer.equals(correctAnswer)) {
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! Correct answer: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;
        showQuestion();
    }

    private void saveScore() {
        int questionsToAdd = mcqList.size();
        int scoreToAdd = score;

        DatabaseReference scoreRef = FirebaseDatabase
                .getInstance("https://salanika-default-rtdb.firebaseio.com/")
                .getReference("Scores")
                .child(userId);

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long previousScore = 0;
                long previousQuestions = 0;
//  score function
                if (snapshot.exists()) {
                    Long scoreVal = snapshot.child("totalScore").getValue(Long.class);
                    Long questionVal = snapshot.child("totalQuestions").getValue(Long.class);
                    if (scoreVal != null) previousScore = scoreVal;
                    if (questionVal != null) previousQuestions = questionVal;
                }

                long updatedScore = previousScore + scoreToAdd;
                long updatedQuestions = previousQuestions + questionsToAdd;

                scoreRef.child("totalScore").setValue(updatedScore);
                scoreRef.child("totalQuestions").setValue(updatedQuestions);

                Log.d("ScoreUpdate", "Score updated to: " + updatedScore + ", Questions: " + updatedQuestions);
                startActivity(new Intent(Quiz.this, progressActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ScoreUpdate", "Failed to update score: " + error.getMessage());
            }
        });
    }
}

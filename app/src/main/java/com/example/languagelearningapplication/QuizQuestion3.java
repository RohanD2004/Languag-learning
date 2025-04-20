package com.example.languagelearningapplication;

// QuizActivity.java

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class QuizQuestion3 extends AppCompatActivity {

    private CardView cardOption1, cardOption2, cardOption3, cardOption4;
    private Button btnCheck;
    private int selectedOption = 0; // 0 means no selection, 1-4 for options
    private final int CORRECT_ANSWER = 2; // The India option is correct (4th option)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_question_3);

        // Initialize views
        cardOption1 = findViewById(R.id.cardOption1);
        cardOption2 = findViewById(R.id.cardOption2);
        cardOption3 = findViewById(R.id.cardOption3);
        cardOption4 = findViewById(R.id.cardOption4);
        btnCheck = findViewById(R.id.btnCheck);

        // Set click listeners for each option
        cardOption1.setOnClickListener(v -> selectOption(1));
        cardOption2.setOnClickListener(v -> selectOption(2));
        cardOption3.setOnClickListener(v -> selectOption(3));
        cardOption4.setOnClickListener(v -> selectOption(4));

        // Initially disable the check button until an option is selected
        btnCheck.setEnabled(false);
        btnCheck.setAlpha(0.5f);

        // Set click listener for check button
        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private void selectOption(int option) {
        // Reset all card backgrounds
        resetCardBackgrounds();

        // Set the selected option and highlight it
        selectedOption = option;

        // Highlight the selected option
        switch (option) {
            case 1:
                cardOption1.setCardBackgroundColor(Color.parseColor("#E5F8FF"));
                break;
            case 2:
                cardOption2.setCardBackgroundColor(Color.parseColor("#E5F8FF"));
                break;
            case 3:
                cardOption3.setCardBackgroundColor(Color.parseColor("#E5F8FF"));
                break;
            case 4:
                cardOption4.setCardBackgroundColor(Color.parseColor("#E5F8FF"));
                break;
        }

        // Enable the check button
        btnCheck.setEnabled(true);
        btnCheck.setAlpha(1.0f);
    }

    private void resetCardBackgrounds() {
        cardOption1.setCardBackgroundColor(Color.WHITE);
        cardOption2.setCardBackgroundColor(Color.WHITE);
        cardOption3.setCardBackgroundColor(Color.WHITE);
        cardOption4.setCardBackgroundColor(Color.WHITE);
    }

    private void checkAnswer() {
        if (selectedOption == 0) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable all options to prevent multiple selections
        cardOption1.setEnabled(false);
        cardOption2.setEnabled(false);
        cardOption3.setEnabled(false);
        cardOption4.setEnabled(false);
        btnCheck.setEnabled(false);

        // Check if the selected option is correct
        if (selectedOption == CORRECT_ANSWER) {
            // Show correct feedback
            cardOption2.setCardBackgroundColor(Color.parseColor("#8BC34A")); // Light green for correct
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(QuizQuestion3.this,Quiz.class));
        } else {
            // Show incorrect feedback
            switch (selectedOption) {
                case 1:
                    cardOption1.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Light red for incorrect
                    break;
                case 2:
                    cardOption2.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                    break;
                case 3:
                    cardOption3.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                    break;
            }

            // Show the correct answer
            cardOption2.setCardBackgroundColor(Color.parseColor("#8BC34A"));
            Toast.makeText(this, "Incorrect. The correct answer is India.", Toast.LENGTH_SHORT).show();
        }

        // Delay before moving to the next question or activity
        new Handler().postDelayed(() -> {
            // Here you can move to the next question or finish the activity
            // For example: startActivity(new Intent(QuizActivity.this, NextQuestionActivity.class));
            // Or for this example, let's just reset the UI
            resetQuiz();
        }, 2000); // 2 seconds delay
    }

    private void resetQuiz() {
        // Reset all card backgrounds
        resetCardBackgrounds();

        // Reset selection
        selectedOption = 0;

        // Enable all options again
        cardOption1.setEnabled(true);
        cardOption2.setEnabled(true);
        cardOption3.setEnabled(true);
        cardOption4.setEnabled(true);

        // Disable check button until an option is selected
        btnCheck.setEnabled(false);
        btnCheck.setAlpha(0.5f);
    }
}
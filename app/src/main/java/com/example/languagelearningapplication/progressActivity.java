package com.example.languagelearningapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class progressActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView percentage,sentence_count,txtCorrectAnswersValue,txtWrongAnswersValue;
    private ProgressBar progressCorrectAnswers,progressWrongAnswers;

    // Firebase Database reference
    private DatabaseReference mDatabase;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_progress);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        percentage=findViewById(R.id.txtScorePercentage);
        sentence_count=findViewById(R.id.txtCorrectCountsentence);
        txtCorrectAnswersValue=findViewById(R.id.txtCorrectAnswersValue);
        progressCorrectAnswers=findViewById(R.id.progressCorrectAnswers);
        progressWrongAnswers=findViewById(R.id.progressWrongAnswers);
        txtWrongAnswersValue=findViewById(R.id.txtWrongAnswersValue);
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();  // Get the current user's ID

        // Fetch data from the Firebase Realtime Database
        mDatabase.child("Scores").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve user data from the database, expecting Long instead of String
                Long totalQuestionsLong = dataSnapshot.child("totalQuestions").getValue(Long.class);
                Long scoreLong = dataSnapshot.child("totalScore").getValue(Long.class);

                if (totalQuestionsLong != null && scoreLong != null) {
                    int totalQuestions = totalQuestionsLong.intValue();
                    int score = scoreLong.intValue();

                    // Set percentage and sentence count
                    percentage.setText((100 * score) / totalQuestions + "%");
                    sentence_count.setText(score + " correct answers out of " + totalQuestions + " questions");
                    txtCorrectAnswersValue.setText(score+"/"+totalQuestions);
                    progressCorrectAnswers.setProgress((100 * score) / totalQuestions );
                    progressWrongAnswers.setProgress(100 -((100 * score) / totalQuestions));
                    txtWrongAnswersValue.setText((totalQuestions-score)+"/"+totalQuestions );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(progressActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
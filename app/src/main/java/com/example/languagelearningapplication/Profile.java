package com.example.languagelearningapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView profileInitial;
    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;

    // Sample user data - in a real app, this would come from a database or shared preferences
    private String userName = "";
    private String userEmail = " ";

    // Firebase Database reference
    private DatabaseReference mDatabase;

    // FirebaseAuth instance for getting the current user's UID
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Assuming your layout file is named activity_profile.xml

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Initialize views
        initViews();
        fetchUserData();
        // Set user data
        setUserData();

        // Set up click listeners
        setupListeners();
    }

    private void initViews() {
        profileInitial = findViewById(R.id.profileInitial);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setUserData() {
        // Set the name and email from our data
        txtName.setText(userName);
        txtEmail.setText(userEmail);

        // Get the first initial of the name for the profile circle
        if (userName != null && !userName.isEmpty()) {
            String initial = String.valueOf(userName.charAt(0));
            profileInitial.setText(initial);
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout action
                performLogout();
            }
        });
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();  // Get the current user's ID

        // Fetch data from the Firebase Realtime Database
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve user data from the database
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);

                // Set user data to the views
                txtName.setText(name);
                txtEmail.setText(email);
//                profileInitial.setText(name.charAt(0));

                // Get the first initial of the name for the profile circle
                if (name != null && !name.isEmpty()) {
                    String initial = String.valueOf(name.charAt(0));
                    profileInitial.setText(initial);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Profile.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        // In a real app, you would:
        // 1. Clear user session (SharedPreferences, database, etc.)
        // 2. Navigate back to login screen

        // For this example, just show a toast message
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        // Sample code to finish the activity and return to login screen
        // Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
    }
}
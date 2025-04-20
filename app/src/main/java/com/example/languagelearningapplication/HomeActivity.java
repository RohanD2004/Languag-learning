package com.example.languagelearningapplication;

// Import existing libraries
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

// Import additional libraries for translation and HTTP requests
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import for speech recognition
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class HomeActivity extends AppCompatActivity {

    // UI elements
    private Spinner sourceLanguageSpinner;
    private Spinner targetLanguageSpinner;
    private EditText inputText;
    private TextView outputText;
    private Button translateButton;
    private MaterialButton voiceInputBtn;
    private MaterialButton clearInputBtn;
    private MaterialButton copyBtn;
    private MaterialButton speakBtn;
    private ImageButton swapLanguageBtn;
    private android.speech.tts.TextToSpeech tts;

    // Speech recognition
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 100;
    private BottomNavigationView bottomNav;
    private boolean isListening = false;

    // Language lists
    private final List<String> displayLanguages = Arrays.asList(
            "English", "Hindi", "Spanish", "French", "German",
            "Chinese", "Japanese", "Korean", "Russian", "Arabic",
            "Portuguese", "Italian", "Dutch", "Swedish", "Greek",
            "Turkish", "Polish", "Vietnamese", "Thai", "Bengali",
            "Marathi"
    );

    // Language codes for Google Translate API
    private final Map<String, String> languageCodes = new HashMap<>();

    // Request queue for API calls
    private RequestQueue requestQueue;

    // Replace with your actual API key
    private static final String TRANSLATE_API_KEY = "AIzaSyDwISzB15N524x5fyDww4InAG9JF8vAMbc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(this);

        // Initialize the language code mapping
        initLanguageCodes();
        initTextToSpeech();

        // Initialize views
        bottomNav = findViewById(R.id.bottomNav);
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner);
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        translateButton = findViewById(R.id.translateButton);
        voiceInputBtn = findViewById(R.id.voiceInputBtn);
        clearInputBtn = findViewById(R.id.clearInputBtn);
        copyBtn = findViewById(R.id.copyBtn);
        speakBtn = findViewById(R.id.speakBtn);
        swapLanguageBtn = findViewById(R.id.swapLanguageBtn);

        // Setup BottomNavigationView listener
        setupBottomNavigation();

        // Setup language spinners
        setupLanguageSpinners();

        // Set click listeners
        setupClickListeners();

        // Check permission for speech recognition
        checkAudioPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the bottom navigation selection to home whenever this activity is in focus
        // This ensures when returning from other activities, the home is selected
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupBottomNavigation() {
        // Set initial selection to Home
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Do nothing since we're already in HomeActivity
                return true;
            } else if (itemId == R.id.quiz) {
                startActivity(new Intent(HomeActivity.this, QuizQuestion1.class));
                return true;
            } else if (itemId == R.id.progress_bar) {
                startActivity(new Intent(HomeActivity.this, progressActivity.class));
                return true;
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(HomeActivity.this, Profile.class));
                return true;
            }

            return false;
        });
    }

    private void initLanguageCodes() {
        // Map display names to ISO language codes used by Google Translate
        languageCodes.put("English", "en");
        languageCodes.put("Hindi", "hi");
        languageCodes.put("Spanish", "es");
        languageCodes.put("French", "fr");
        languageCodes.put("German", "de");
        languageCodes.put("Chinese", "zh");
        languageCodes.put("Japanese", "ja");
        languageCodes.put("Korean", "ko");
        languageCodes.put("Russian", "ru");
        languageCodes.put("Arabic", "ar");
        languageCodes.put("Portuguese", "pt");
        languageCodes.put("Italian", "it");
        languageCodes.put("Dutch", "nl");
        languageCodes.put("Swedish", "sv");
        languageCodes.put("Greek", "el");
        languageCodes.put("Turkish", "tr");
        languageCodes.put("Polish", "pl");
        languageCodes.put("Vietnamese", "vi");
        languageCodes.put("Thai", "th");
        languageCodes.put("Bengali", "bn");
        languageCodes.put("Marathi", "mr");
    }

    private void setupLanguageSpinners() {
        // Create adapter for spinners
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                displayLanguages
        );

        // Apply adapters to spinners
        sourceLanguageSpinner.setAdapter(languageAdapter);
        targetLanguageSpinner.setAdapter(languageAdapter);

        // Set default selections
        sourceLanguageSpinner.setSelection(displayLanguages.indexOf("English"));
        targetLanguageSpinner.setSelection(displayLanguages.indexOf("Hindi"));
    }

    private void setupClickListeners() {
        // Translate button
        translateButton.setOnClickListener(v -> {
            String input = inputText.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected languages
            String sourceLanguageName = sourceLanguageSpinner.getSelectedItem().toString();
            String targetLanguageName = targetLanguageSpinner.getSelectedItem().toString();

            // Get language codes
            String sourceLanguageCode = languageCodes.get(sourceLanguageName);
            String targetLanguageCode = languageCodes.get(targetLanguageName);

            // Call the translation API
            translateText(input, sourceLanguageCode, targetLanguageCode);
        });

        // Clear input button
        clearInputBtn.setOnClickListener(v -> {
            inputText.setText("");
            outputText.setText("Translation will appear here");
        });

        // Swap languages button
        swapLanguageBtn.setOnClickListener(v -> {
            int sourcePos = sourceLanguageSpinner.getSelectedItemPosition();
            int targetPos = targetLanguageSpinner.getSelectedItemPosition();

            sourceLanguageSpinner.setSelection(targetPos);
            targetLanguageSpinner.setSelection(sourcePos);

            // If there's already a translation, swap input and output
            String currentInput = inputText.getText().toString().trim();
            String currentOutput = outputText.getText().toString().trim();

            if (!currentInput.isEmpty() && !currentOutput.equals("Translation will appear here")) {
                inputText.setText(currentOutput);
                outputText.setText(currentInput);
            }
        });

        // Voice input button
        voiceInputBtn.setOnClickListener(v -> {
            if (isListening) {
                stopSpeechRecognition();
                voiceInputBtn.setText("Voice Input");
                isListening = false;
            } else {
                startSpeechRecognition();
                voiceInputBtn.setText("Stop Listening");
                isListening = true;
            }
        });

        // Copy button
        copyBtn.setOnClickListener(v -> {
            String translatedText = outputText.getText().toString();
            if (!translatedText.equals("Translation will appear here")) {
                // Copy to clipboard
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Translated Text", translatedText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        // Speak button
        speakBtn.setOnClickListener(v -> {
            String translatedText = outputText.getText().toString();
            if (!translatedText.equals("Translation will appear here")) {
                // Text-to-speech implementation
                speakText(translatedText);
            }
        });
    }

    private void translateText(String text, String sourceLanguage, String targetLanguage) {
        // Show loading state
        outputText.setText("Translating...");

        try {
            // For Google Cloud Translation API v2
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + TRANSLATE_API_KEY;

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("q", text);
            jsonBody.put("source", sourceLanguage);
            jsonBody.put("target", targetLanguage);
            jsonBody.put("format", "text");

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Parse the response
                                JSONObject data = response.getJSONObject("data");
                                JSONArray translations = data.getJSONArray("translations");
                                JSONObject translation = translations.getJSONObject(0);
                                String translatedText = translation.getString("translatedText");

                                // Update the UI with translated text
                                outputText.setText(translatedText);
                            } catch (JSONException e) {
                                outputText.setText("Translation error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            outputText.setText("Network error: " + error.getMessage());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Add the request to the queue
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            outputText.setText("Error preparing translation request: " + e.getMessage());
        }
    }

    // Method to speak text
    private void speakText(String text) {
        if (tts == null) {
            Toast.makeText(this, "Text-to-speech is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get selected target language
            String targetLanguageName = targetLanguageSpinner.getSelectedItem().toString();
            String targetLanguageCode = languageCodes.get(targetLanguageName);

            // Set language for TTS
            java.util.Locale locale = new java.util.Locale(targetLanguageCode);
            int result = tts.setLanguage(locale);

            if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                    result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported for text-to-speech", Toast.LENGTH_SHORT).show();
            } else {
                tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error with text-to-speech: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initTextToSpeech() {
        tts = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != android.speech.tts.TextToSpeech.SUCCESS) {
                    Toast.makeText(HomeActivity.this, "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Check for audio recording permission
    private void checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                initSpeechRecognizer();
            }
        } else {
            initSpeechRecognizer();
        }
    }

    // Initialize speech recognizer
    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            // Set the language based on source language selection
            String sourceLanguageName = sourceLanguageSpinner.getSelectedItem().toString();
            String sourceLanguageCode = languageCodes.get(sourceLanguageName);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode);

            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Toast.makeText(HomeActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBeginningOfSpeech() {
                    inputText.setHint("Listening...");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Update UI to show voice level if desired
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    inputText.setHint("Input text");
                    voiceInputBtn.setText("Voice Input");
                    isListening = false;
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    voiceInputBtn.setText("Voice Input");
                    isListening = false;
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        inputText.setText(text);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        inputText.setText(text);
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });

            // Listen for source language changes to update speech recognizer language
            sourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String sourceLanguageName = sourceLanguageSpinner.getSelectedItem().toString();
                    String sourceLanguageCode = languageCodes.get(sourceLanguageName);
                    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    // Start speech recognition
    private void startSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    // Stop speech recognition
    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    // Helper method to get error text
    private String getErrorText(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                errorMessage = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorMessage = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorMessage = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorMessage = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorMessage = "No match found";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorMessage = "No speech input";
                break;
            default:
                errorMessage = "Unknown error";
                break;
        }
        return errorMessage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSpeechRecognizer();
            } else {
                Toast.makeText(this, "Permission denied for audio recording", Toast.LENGTH_SHORT).show();
                voiceInputBtn.setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
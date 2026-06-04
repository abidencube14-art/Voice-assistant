package com.human.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;

    private TextView textView;
    private Button listenButton;

    private boolean ttsReady = false;

    // Memory
    private String userName = "Abide";
    private String assistantName = "Baymax";
    private String wakeName = "Max";
    
    // Personality mode
    private String personalityMode = "casual";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("BaymaxMemory", MODE_PRIVATE);

userName = prefs.getString("userName", "friend");
assistantName = prefs.getString("assistantName", "Baymax");
personalityMode = prefs.getString("personalityMode", "casual");u

        textView = findViewById(R.id.textView);
        listenButton = findViewById(R.id.listenButton);

        // Microphone permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1
            );
        }

        // Text To Speech
        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Toast.makeText(
                            this,
                            "Language not supported",
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    ttsReady = true;

                    Toast.makeText(
                            this,
                            assistantName + " is ready",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } else {

                Toast.makeText(
                        this,
                        "TTS Initialization Failed",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {

                textView.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {

                textView.setText("Processing...");
            }

            @Override
            public void onError(int error) {

                textView.setText("I didn't catch that.");
            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> data =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (data != null && data.size() > 0) {

                    String text = data.get(0);

                    String lowerText = text.toLowerCase();

                    String reply;

                    // Wake phrase
                    if (lowerText.contains("hey " + wakeName.toLowerCase())
        || lowerText.contains("hey " + assistantName.toLowerCase())) {

                        if (personalityMode.equals("casual")) {

                            reply = "Yeah " + userName + "?";

                        } else {

                            reply = "Hello " + userName + ". How may I assist you?";
                        }
                    }

                    // User name memory
                    else if (lowerText.contains("call me")) {

                        String[] parts = text.split("call me");

                        if (parts.length > 1) {

                            userName = parts[1].trim();

prefs.edit()
        .putString("userName", userName)
        .apply();

reply = "Alright. I'll call you " + userName;
                        } else {

                            reply = "What should I call you?";
                        }
                    }

                    // Assistant naming
                    else if (lowerText.contains("your name is")) {

                        String[] parts = text.split("your name is");

                        if (parts.length > 1) {

                            assistantName = parts[1].trim();

prefs.edit()
        .putString("assistantName", assistantName)
        .apply();
                            
                            reply = "Okay. My new name is " + assistantName;
                        } else {

                            reply = "What should my name be?";
                        }
                    }

                    // Recall user name
                    else if (lowerText.contains("what's my name")
                            || lowerText.contains("what is my name")) {

                        reply = "Your name is " + userName;
                    }

                    // Personality modes
                    else if (lowerText.contains("switch to casual mode")) {

                        personalityMode = "casual";

prefs.edit()
        .putString("personalityMode", personalityMode)
        .apply();
                        
                        reply = "Casual mode activated.";

                    }

                    else if (lowerText.contains("switch to formal mode")) {

                        personalityMode = "formal";

prefs.edit()
        .putString("personalityMode", personalityMode)
        .apply();
                        
                        reply = "Formal mode activated.";
                    }

                    // Time
                    else if (lowerText.contains("time")) {

                        SimpleDateFormat sdf =
                                new SimpleDateFormat(
                                        "hh:mm a",
                                        Locale.getDefault()
                                );

                        String currentTime = sdf.format(new Date());

                        if (personalityMode.equals("casual")) {

                            reply = "It's " + currentTime;

                        } else {

                            reply = "The current time is " + currentTime;
                        }
                    }
                        
                    // Assistant name
                    else if (lowerText.contains("what's your name")
        || lowerText.contains("what is your name")) {

                        if (personalityMode.equals("casual")) {

                            reply = "I'm " + assistantName;

                        } else {

                            reply = "My name is " + assistantName;
                        }
                    }
                        
                    // Greetings
                    else if (lowerText.contains("hello")
                            || lowerText.contains("hi")) {

                        if (personalityMode.equals("casual")) {

                            reply = "Hey " + userName + ", what's up?";

                        } else {

                            reply = "Hello " + userName +
                                    ". I hope you are doing well.";
                        }
                    }

                    // How are you
                    else if (lowerText.contains("how are you")) {

                        if (personalityMode.equals("casual")) {

                            reply = "I'm doing great.";

                        } else {

                            reply = "I am functioning perfectly.";
                        }
                    }

                    // Joke
                    else if (lowerText.contains("joke")) {

                        reply = "Why did the programmer go broke? Because he used up all his cache.";
                    }

                    // Creator
                    else if (lowerText.contains("who made you")) {

                        reply = "I was created by an ambitious developer on a Samsung phone.";
                    }

                    // Default
                    else {

                        if (personalityMode.equals("casual")) {

                            reply = "I heard you say " + text;

                        } else {

                            reply = "You said " + text;
                        }
                    }

                    // Show reply
                    textView.setText(reply);

                    // Speak reply
                    if (ttsReady) {

                        tts.speak(
                                reply,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                null
                        );
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        // Button
        listenButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Speak now..."
            );

            speechRecognizer.startListening(intent);
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
        }
    }
}

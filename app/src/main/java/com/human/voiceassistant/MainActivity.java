package com.human.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        listenButton = findViewById(R.id.listenButton);

        // Request microphone permission
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

        // Initialize Text To Speech
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
                            "Voice Assistant Ready",
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

        // Initialize Speech Recognizer
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

                    textView.setText(text);

                    String lowerText = text.toLowerCase();

                    String reply;

                    // Greetings
                    if (lowerText.contains("hello")
                            || lowerText.contains("hi")) {

                        reply = "Hey, what's up?";

                    }

                    // Name
                    else if (lowerText.contains("your name")) {

                        reply = "I'm your offline voice assistant.";

                    }

                    // How are you
                    else if (lowerText.contains("how are you")) {

                        reply = "I'm functioning perfectly.";

                    }

                    // Time
                    else if (lowerText.contains("time")) {

                        SimpleDateFormat sdf =
                                new SimpleDateFormat(
                                        "hh:mm a",
                                        Locale.getDefault()
                                );

                        String currentTime = sdf.format(new Date());

                        reply = "The time is " + currentTime;

                    }

                    // Joke
                    else if (lowerText.contains("joke")) {

                        reply = "Why did the programmer go broke? Because he used up all his cache.";

                    }

                    // Creator
                    else if (lowerText.contains("who made you")) {

                        reply = "I was created by an ambitious developer on a Samsung phone.";

                    }

                    // Default reply
                    else {

                        reply = "I heard you say " + text;
                    }

                    // Show reply on screen
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

        // Button click
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

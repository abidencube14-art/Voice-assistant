package com.human.voiceassistant;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button listenButton = findViewById(R.id.listenButton);

        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Toast.makeText(this,
                            "Language not supported",
                            Toast.LENGTH_LONG).show();

                } else {

                    ttsReady = true;

                    Toast.makeText(this,
                            "Voice Assistant Ready",
                            Toast.LENGTH_SHORT).show();
                }

            } else {

                Toast.makeText(this,
                        "TTS Initialization Failed",
                        Toast.LENGTH_LONG).show();
            }
        });

        listenButton.setOnClickListener(v -> {

            if (ttsReady) {

                tts.speak(
                        "Hello. I am online and ready.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                );

            } else {

                Toast.makeText(this,
                        "TTS not ready yet",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}

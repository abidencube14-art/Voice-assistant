package com.human.voiceassistant;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button listenButton = findViewById(R.id.listenButton);

        tts = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        listenButton.setOnClickListener(v -> {
            tts.speak("Hello. I am online and ready.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null);
        });
    }
}

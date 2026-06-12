package com.human.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.BatteryManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Iterator;

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
    private HashMap<String, String> memory = new HashMap<>();
    private HashMap<String, String> contacts = new HashMap<>();
    
    // Personality mode
    private String personalityMode = "casual";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("BaymaxMemory", MODE_PRIVATE);
        String memoryJson = prefs.getString("memory", "{}");

        String savedContacts =
        prefs.getString("contacts", "{}");

try {

    JSONObject contactsObj =
            new JSONObject(savedContacts);

    Iterator<String> keys =
            contactsObj.keys();

    while (keys.hasNext()) {

        String key = keys.next();

        contacts.put(
                key,
                contactsObj.getString(key)
        );
    }

} catch (Exception e) {

    e.printStackTrace();
}

try {
    JSONObject obj = new JSONObject(memoryJson);

    Iterator<String> keys = obj.keys();

    while (keys.hasNext()) {
        String key = keys.next();
        memory.put(key, obj.getString(key));
    }

} catch (JSONException e) {
    e.printStackTrace();
}

userName = prefs.getString("userName", "friend");
assistantName = prefs.getString("assistantName", "Baymax");
personalityMode = prefs.getString("personalityMode", "casual");

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

                    String reply = "";

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

                    // Battery
                        else if (lowerText.contains("battery")
        || lowerText.contains("battery percentage")
        || lowerText.contains("how much battery")) {

    IntentFilter ifilter =
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    Intent batteryStatus =
            registerReceiver(null, ifilter);

    int level =
            batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

    int scale =
            batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

    int batteryPct =
            (int) ((level / (float) scale) * 100);

    reply = "Your battery is at "
            + batteryPct
            + " percent.";
                        }

                    // App opening
                            else if (lowerText.contains("open calculator")) {

    String[] packages = {
            "com.sec.android.app.popupcalculator",
            "com.google.android.calculator",
            "com.android.calculator2"
    };

    boolean opened = false;

    for (String pkg : packages) {

        Intent launchIntent =
                getPackageManager()
                        .getLaunchIntentForPackage(pkg);

        if (launchIntent != null) {

            startActivity(launchIntent);

            reply = "Opening calculator.";
            opened = true;
            break;
        }
    }

    if (!opened) {

        reply = "I couldn't find the calculator app.";
    }
                            }
                                else if (lowerText.contains("open camera")) {

    Intent intent =
            new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

    startActivity(intent);

    reply = "Opening camera.";
                                }
                                    else if (lowerText.contains("open settings")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_SETTINGS);

    startActivity(intent);

    reply = "Opening settings.";
                                    }
                                        else if (lowerText.contains("open browser")
        || lowerText.contains("open internet")) {

    Intent intent =
            new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com"));

    startActivity(intent);

    reply = "Opening browser.";
                                        }
                                            else if (lowerText.contains("open contacts")) {

    Intent intent =
            new Intent(Intent.ACTION_VIEW);

    intent.setType("vnd.android.cursor.dir/contact");

    startActivity(intent);

    reply = "Opening contacts.";
                                            }
                                                else if (lowerText.contains("open phone")
        || lowerText.contains("open dialer")) {

    Intent intent =
            new Intent(Intent.ACTION_DIAL);

    startActivity(intent);

    reply = "Opening phone.";
                                                }
                                                    else if (lowerText.contains("open messages")) {

    Intent intent =
            new Intent(Intent.ACTION_MAIN);

    intent.addCategory(Intent.CATEGORY_APP_MESSAGING);

    startActivity(intent);

    reply = "Opening messages.";
                                                    }
                                                        else if (lowerText.contains("open whatsapp")) {

    Intent launchIntent =
            getPackageManager().getLaunchIntentForPackage("com.whatsapp");

    if (launchIntent != null) {

        startActivity(launchIntent);
        reply = "Opening WhatsApp.";

    } else {

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me"));

        startActivity(intent);

        reply = "Opening WhatsApp.";
    }
                             }
                                                            else if (lowerText.contains("open youtube")) {

    Intent intent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com"));

    startActivity(intent);

    reply = "Opening YouTube.";
                }
                                                                else if (lowerText.contains("open gallery")) {

    Intent intent = new Intent(Intent.ACTION_VIEW);

    intent.setType("image/*");

    startActivity(intent);

    reply = "Opening gallery.";
                                                                }
                                                                    
                                                                    else if (lowerText.contains("open files")
        || lowerText.contains("open file manager")) {

    Intent intent =
            new Intent(Intent.ACTION_GET_CONTENT);

    intent.setType("*/*");

    startActivity(intent);

    reply = "Opening files.";
                                                                    }
                                                                        else if (lowerText.contains("open chrome")) {

    Intent launchIntent =
            getPackageManager().getLaunchIntentForPackage(
                    "com.android.chrome");

    if (launchIntent != null) {

        startActivity(launchIntent);

        reply = "Opening Chrome.";

    } else {

        reply = "Chrome is not installed.";
    }
                                                                        }
                                                                            else if (lowerText.contains("open play store")) {

    Intent launchIntent =
            getPackageManager().getLaunchIntentForPackage(
                    "com.android.vending");

    if (launchIntent != null) {

        startActivity(launchIntent);

        reply = "Opening Play Store.";

    } else {

        reply = "Play Store is not available.";
    }
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

                        else if (lowerText.contains("what is today's date")
      || lowerText.contains("what is the date")
      || lowerText.contains("today's date")) {

    SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

    reply = "Today is " + dateFormat.format(new Date());
                        }

                            else if (lowerText.contains("what day is it")) {

    SimpleDateFormat dayFormat =
            new SimpleDateFormat("EEEE", Locale.getDefault());

    reply = "Today is " + dayFormat.format(new Date());
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
        || lowerText.contains("hi")
        || lowerText.contains("hey")) {

    Calendar calendar = Calendar.getInstance();

    int hour = calendar.get(Calendar.HOUR_OF_DAY);

    if (hour < 12) {

        reply = "Good morning! How can I help you?";

    } else if (hour < 18) {

        reply = "Good afternoon! How can I help you?";

    } else {

        reply = "Good evening! How can I help you?";
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

                     // Assistant information
                        else if (lowerText.contains("who are you")) {

    reply =
            "I am Baymax, your personal voice assistant.";
                        }

                    // Memory count
                            else if (lowerText.contains("how much do you remember")) {

    reply =
            "I currently remember "
                    + memory.size()
                    + " facts and "
                    + contacts.size()
                    + " contacts.";
                            }

                     // Random Joke
                                else if (lowerText.contains("tell me a joke")) {

    String[] jokes = {

            "Why did the programmer quit his job? Because he didn't get arrays.",

            "Why do Java developers wear glasses? Because they don't C sharp.",

            "I would tell you a UDP joke, but you might not get it."

    };

    reply =
            jokes[new Random().nextInt(jokes.length)];
                                }
                            
                    // List contact
                        else if (lowerText.contains("list contacts")
        || lowerText.contains("my contacts")) {

    if (contacts.isEmpty()) {

        reply = "You don't have any saved contacts.";

    } else {

        StringBuilder sb = new StringBuilder();

        sb.append("Your contacts are ");

        for (String name : contacts.keySet()) {

            sb.append(name).append(", ");
        }

        reply = sb.toString();
    }
                        }

                    // Delete contact
                            else if (lowerText.startsWith("delete contact ")) {

    String name =
            text.substring(15).trim().toLowerCase();

    if (contacts.containsKey(name)) {

        contacts.remove(name);

        try {

            JSONObject contactsObj =
                    new JSONObject(contacts);

            prefs.edit()
                    .putString(
                            "contacts",
                            contactsObj.toString())
                    .apply();

        } catch (Exception e) {

            e.printStackTrace();
        }

        reply = "Deleted contact " + name;

    } else {

        reply = "I couldn't find that contact.";
    }
                            }

                    // Calls and searches
                        else if (lowerText.startsWith("call ")) {

    String number = text.substring(5).trim();

    Intent intent = new Intent(Intent.ACTION_DIAL);
    intent.setData(Uri.parse("tel:" + number));

    startActivity(intent);

    reply = "Calling " + number;
                        }
                            else if (lowerText.startsWith("search google for ")) {

    String query = text.substring(18).trim();

    Intent intent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));

    startActivity(intent);

    reply = "Searching Google for " + query;
                            }
                                else if (lowerText.startsWith("search youtube for ")) {

    String query = text.substring(19).trim();

    Intent intent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/results?search_query="
                    + Uri.encode(query)));

    startActivity(intent);

    reply = "Searching YouTube for " + query;
                                }
                                    else if (lowerText.startsWith("open website ")) {

    String site = text.substring(13).trim();

    if (!site.startsWith("http")) {
        site = "https://" + site;
    }

    Intent intent =
            new Intent(Intent.ACTION_VIEW, Uri.parse(site));

    startActivity(intent);

    reply = "Opening " + site;
                                    }
                                        else if (lowerText.matches("what is \\d+ plus \\d+")) {

    String expression = lowerText.replace("what is ", "");
    String[] parts = expression.split(" plus ");

    int a = Integer.parseInt(parts[0].trim());
    int b = Integer.parseInt(parts[1].trim());

    reply = "The answer is " + (a + b);
}

else if (lowerText.startsWith("save contact ")) {

    String contactData = text.substring(13).trim();
    String[] parts = contactData.split(" as ");

    if (parts.length == 2) {

        String number = parts[0].trim();
        String name = parts[1].trim().toLowerCase();

        contacts.put(name, number);

        try {
            JSONObject contactsObj = new JSONObject(contacts);

            prefs.edit()
                    .putString("contacts", contactsObj.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        reply = "Saved " + name + " with number " + number;

} else {
    reply = "Say: save contact number as name";
}

}   // <-- ADD THIS MISSING BRACE

else if (lowerText.startsWith("call ")) {

    String person = text.substring(5).trim().toLowerCase();

    if (contacts.containsKey(person)) {

        String number = contacts.get(person);

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));

        startActivity(intent);

        reply = "Calling " + person;

    } else {

        reply = "I don't know that contact.";
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
                    else if (lowerText.contains("what do you know about me")) {

    if (memory.isEmpty()) {

        reply = "I don't know much about you yet.";

    } else {

        StringBuilder info = new StringBuilder();

        info.append("Here's what I know about you. ");

        for (String key : memory.keySet()) {

            info.append(key)
                .append(" is ")
                .append(memory.get(key))
                .append(". ");
        }

        reply = info.toString();
    }
                        }
                        
                        else if (lowerText.startsWith("what is")) {

    String key = text.substring(7).trim().toLowerCase();

    if (memory.containsKey(key)) {

        reply = key + " is " + memory.get(key);

    } else if (memory.containsKey("my " + key)) {

        reply = "Your " + key + " is " + memory.get("my " + key);

    } else {

        reply = "I don't remember anything about " + key;
    }
                        }
                        
                    else if (lowerText.startsWith("remember that")) {

    String fact = text.substring(13).trim();

    if (fact.contains(" is ")) {

        String[] parts = fact.split(" is ", 2);

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        memory.put(key, value);

        try {
    JSONObject obj = new JSONObject(memory);

    prefs.edit()
            .putString("memory", obj.toString())
            .apply();

} catch (Exception e) {
    e.printStackTrace();
        }

        reply = "Okay. I'll remember that " + key + " is " + value;

    } else {

        reply = "Please say it like remember that my favorite color is blue";
    }
}
                        
else if (lowerText.startsWith("my ") && lowerText.contains(" is ")) {

    String[] parts = text.split(" is ", 2);

    if (parts.length > 1) {

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        memory.put(key, value);

        try {
            JSONObject obj = new JSONObject(memory);

            prefs.edit()
                    .putString("memory", obj.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        reply = "Got it. I'll remember that " + key + " is " + value;

    } else {

        reply = "I couldn't understand that memory.";
    }
}
    
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

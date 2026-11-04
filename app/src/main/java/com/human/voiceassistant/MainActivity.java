package com.human.voiceassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.media.AudioManager;
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
import java.util.List;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private CameraManager cameraManager;
    private String cameraId;
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
        cameraManager =
        (CameraManager) getSystemService(CAMERA_SERVICE);

try {
    String[] cameraIds = cameraManager.getCameraIdList();

    for (String id : cameraIds) {
        android.hardware.camera2.CameraCharacteristics characteristics =
                cameraManager.getCameraCharacteristics(id);

        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        
        if (Boolean.TRUE.equals(hasFlash)) {
            cameraId = id;
            break;
        }
    }

} catch (CameraAccessException e) {
    e.printStackTrace();
}

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

userName = prefs.getString("userName", "Abide");
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

    openAppByName("Calculator");

    reply = "Opening Calculator";
                            }
                                else if (lowerText.contains("open camera")) {

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivity(intent);

    reply = "Opening Camera";
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

    if (openAppByName("WhatsApp")) {

        reply = "Opening WhatsApp";

    } else {

        reply = "Couldn't find WhatsApp";
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

                    // Settings and gestures
                                                                                else if (lowerText.contains("open wifi")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);

    startActivity(intent);

    reply = "Opening WiFi settings";
                                                                                }
                                                                                    else if (lowerText.contains("open bluetooth")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);

    startActivity(intent);

    reply = "Opening Bluetooth settings";
        }
                                                                                        else if (lowerText.contains("open airplane mode")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);

    startActivity(intent);

    reply = "Opening Airplane Mode settings";
                                                                                        }
                                                                                            else if (lowerText.contains("open location")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

    startActivity(intent);

    reply = "Opening Location settings";
                                                                                            }
                                                                                                else if (lowerText.contains("open display settings")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS);

    startActivity(intent);

    reply = "Opening Display settings";
                                                                                                }
                                                                                                else if (lowerText.contains("open sound settings")) {

    Intent intent =
            new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);

    startActivity(intent);

    reply = "Opening Sound settings";
                                                                                                }
                                                                                                    
                                                                                                    // =====================================
// BRIGHTNESS CONTROL
// =====================================

else if (
    lowerText.contains("brightness up") ||
    lowerText.contains("increase brightness") ||
    lowerText.contains("increase screen brightness") ||
    lowerText.contains("make screen brighter") ||
    lowerText.contains("brighten the screen")
) {

    reply = handleBrightnessCommand(lowerText);

}

else if (
    lowerText.contains("brightness down") ||
    lowerText.contains("decrease brightness") ||
    lowerText.contains("decrease screen brightness") ||
    lowerText.contains("make screen dimmer") ||
    lowerText.contains("dim the screen")
) {

    reply = handleBrightnessCommand(lowerText);

}

else if (
    lowerText.contains("set brightness") ||
    lowerText.contains("set screen brightness") ||
    lowerText.contains("change brightness") ||
    lowerText.contains("adjust brightness")
) {

    reply = handleBrightnessCommand(lowerText);

}

else if (
    lowerText.contains("current brightness") ||
    lowerText.contains("brightness level") ||
    lowerText.contains("what's my current brightness") ||
    lowerText.contains("what is my current brightness") ||
    lowerText.contains("what is my brightness") ||
    lowerText.contains("what's my brightness") ||
    lowerText.contains("how bright is the screen") ||
    lowerText.contains("how bright")
) {

    reply = handleBrightnessCommand(lowerText);

    }
                                                                                                    
                                                                                                    else if (lowerText.contains("volume up")) {

    AudioManager audio =
            (AudioManager)getSystemService(AUDIO_SERVICE);

    audio.adjustVolume(
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
    );

    reply = "Volume increased";
        }
                                                                                                        else if (lowerText.contains("volume down")) {

    AudioManager audio =
            (AudioManager)getSystemService(AUDIO_SERVICE);

    audio.adjustVolume(
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
    );

    reply = "Volume decreased";
                                                                                                        }
                                                                                                    else if (lowerText.contains("silent mode")) {

    AudioManager audio =
            (AudioManager)getSystemService(AUDIO_SERVICE);

    audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);

    reply = "Silent mode enabled";
                                                                                                    }
                                                                                                else if (lowerText.contains("vibrate mode")) {

    AudioManager audio =
            (AudioManager)getSystemService(AUDIO_SERVICE);

    audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

    reply = "Vibrate mode enabled";
                                                                                                }
                                                                                                    else if (lowerText.contains("normal volume")) {

    AudioManager audio =
            (AudioManager)getSystemService(AUDIO_SERVICE);

    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    reply = "Normal mode enabled";
                                                                                                    }
                                                                                                        
                                                                                                        else if (
        lowerText.contains("turn on flashlight")
        || lowerText.contains("flashlight on")
        || lowerText.contains("turn on torch")
        || lowerText.equals("torch on")
) {
    try {
        if (cameraId == null) {
            reply = "I couldn't find a flashlight on this phone.";
        } else {
            cameraManager.setTorchMode(cameraId, true);
            reply = "Flashlight turned on.";
        }
    } catch (Exception e) {
        reply = "I couldn't turn on the flashlight.";
    }
                }
                                                                                                            
                                                                                                            else if (
        lowerText.contains("turn off flashlight")
        || lowerText.contains("flashlight off")
        || lowerText.contains("turn off torch")
        || lowerText.equals("torch off")
) {
    try {
        if (cameraId == null) {
            reply = "I couldn't find a flashlight on this phone.";
        } else {
            cameraManager.setTorchMode(cameraId, false);
            reply = "Flashlight turned off.";
        }
    } catch (Exception e) {
        reply = "I couldn't turn off the flashlight.";
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
                        else if (lowerText.contains("+") || lowerText.contains("plus")) {

    try {

        String expression = lowerText
                .replace("what is", "")
                .trim();

        String[] parts;

        if (expression.contains("+")) {
            parts = expression.split("\\+");
        } else {
            parts = expression.split("plus");
        }

        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());

        reply = "The answer is " + (a + b);

    } catch (Exception e) {

        reply = "Sorry, I couldn't calculate that.";
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

    private boolean openAppByName(String appName) {

    PackageManager pm = getPackageManager();

    List<ApplicationInfo> apps =
            pm.getInstalledApplications(PackageManager.GET_META_DATA);

    for (ApplicationInfo app : apps) {

        String label =
                pm.getApplicationLabel(app).toString();

        if (label.equalsIgnoreCase(appName)) {

            Intent intent =
                    pm.getLaunchIntentForPackage(app.packageName);

            if (intent != null) {

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            }
        }
    }

    return false;
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
    
    // =====================================
// BAYMAX BRIGHTNESS CONTROL
// =====================================

private String setScreenBrightness(int percentage) {

    if (!Settings.System.canWrite(this)) {

        Intent intent = new Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:" + getPackageName())
        );

        startActivity(intent);

        return "Please allow Baymax to modify system settings, then repeat your command.";
    }

    percentage = Math.max(1, Math.min(100, percentage));

    int brightness = (int) Math.round(
        percentage * 255.0 / 100.0
    );

    try {

        Settings.System.putInt(
            getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS,
            brightness
        );

        return "Screen brightness set to " + percentage + " percent.";

    } catch (Exception e) {

        return "Sorry, I couldn't change the screen brightness.";
    }
}


// Get current brightness percentage
private int getCurrentBrightness() {

    try {

        int brightness = Settings.System.getInt(
            getContentResolver(),
            Settings.System.SCREEN_BRIGHTNESS
        );

        return Math.max(
            1,
            Math.min(100, (int) Math.round(brightness * 100.0 / 255.0))
        );

    } catch (Exception e) {

        return 50;
    }
}


// Extract a percentage from the voice command
private int extractBrightnessPercentage(String command) {

    String cleaned = command.replaceAll("[^a-z0-9 ]", " ");
    String[] words = cleaned.trim().split("\\s+");

    String[] numbers = {
        "zero", "one", "two", "three", "four",
        "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen",
        "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    };

    String[] tens = {
        "twenty", "thirty", "forty", "fifty",
        "sixty", "seventy", "eighty", "ninety"
    };

    for (int i = 0; i < words.length; i++) {

        try {

            int value = Integer.parseInt(words[i]);

            if (value >= 1 && value <= 100) {
                return value;
            }

        } catch (NumberFormatException ignored) {
        }

        for (int j = 1; j < numbers.length; j++) {

            if (words[i].equals(numbers[j])) {
                return j;
            }
        }

        for (int j = 0; j < tens.length; j++) {

            if (words[i].equals(tens[j])) {

                int value = (j + 2) * 10;

                if (i + 1 < words.length) {

                    for (int k = 1; k < 10; k++) {

                        if (words[i + 1].equals(numbers[k])) {
                            value += k;
                            break;
                        }
                    }
                }

                return value;
            }
        }

        if (words[i].equals("hundred")) {
            return 100;
        }
    }

    return -1;
}


// Process brightness voice commands
private String handleBrightnessCommand(String command) {

    int current = getCurrentBrightness();

    int amount = extractBrightnessPercentage(command);

    boolean increase =
        command.contains("increase") ||
        command.contains("brightness up") ||
        command.contains("brighter") ||
        command.contains("brighten") ||
        command.contains("raise") ||
        command.contains("turn up");

    boolean decrease =
        command.contains("decrease") ||
        command.contains("brightness down") ||
        command.contains("dimmer") ||
        command.contains("dim the screen") ||
        command.contains("lower") ||
        command.contains("turn down") ||
        command.contains("reduce");

    boolean hasTo = command.contains(" to ");
    boolean hasBy = command.contains(" by ");

    // Current brightness inquiry
    if (
        command.contains("current brightness") ||
        command.contains("brightness level") ||
        command.contains("how bright") ||
        command.contains("what is my brightness")
    ) {

        return "Your current screen brightness is approximately "
            + current + " percent.";
    }

    // Increase or decrease brightness
    if (increase || decrease) {

        if (hasTo) {

            if (amount == -1) {
                return "Please tell me the percentage you want.";
            }

            return setScreenBrightness(amount);
        }

        if (hasBy) {

            if (amount == -1) {
                return "Please tell me how much to adjust the brightness.";
            }

            int target = increase
                ? current + amount
                : current - amount;

            return setScreenBrightness(target);
        }

        int target = increase
            ? current + 10
            : current - 10;

        return setScreenBrightness(target);
    }

    // Set a specific brightness percentage
    if (
        command.contains("set brightness") ||
        command.contains("set screen brightness") ||
        command.contains("change brightness") ||
        command.contains("adjust brightness")
    ) {

        if (amount == -1) {
            return "What brightness percentage would you like?";
        }

        return setScreenBrightness(amount);
    }

    return "I couldn't understand the brightness instruction.";
    }
    }

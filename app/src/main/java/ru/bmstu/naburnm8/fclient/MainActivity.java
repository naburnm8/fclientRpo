package ru.bmstu.naburnm8.fclient;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import ru.bmstu.naburnm8.fclient.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'fclient' library on application startup.
    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
        Log.println(Log.INFO, "Library Loaded", "Library Loaded");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();
        byte[] v = randomBytes(10);

        Log.println(Log.INFO, "mbedtls", Arrays.toString(v));

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'fclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
}
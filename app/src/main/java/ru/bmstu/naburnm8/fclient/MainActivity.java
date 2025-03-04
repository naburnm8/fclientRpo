package ru.bmstu.naburnm8.fclient;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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

    private Button button;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            byte[] key = stringToHex("0123456789ABCDEF0123456789ABCDE0");
            byte[] enc = encrypt(key, stringToHex("000000000000000102"));
            byte[] dec = decrypt(key, enc);
            String s = new String(Hex.encodeHex(dec)).toUpperCase();
            //Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

            Intent it = new Intent(this, PinpadActivity.class);
            //startActivity(it);
            activityResultLauncher.launch(it);
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        String pin = data.getStringExtra("pin");
                        Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                    }
                }
        );


        int res = initRng();
        byte[] v = randomBytes(16);

        byte[] encrypted = encrypt(v, v);
        byte[] decrypted = decrypt(v, encrypted);

        Log.println(Log.INFO, "mbedtls", Arrays.toString(v));
        Log.println(Log.INFO, "mbedtls_encrypted", Arrays.toString(encrypted));
        Log.println(Log.INFO, "mbedtls_decrypted", Arrays.toString(decrypted));


    }

    /**
     * A native method that is implemented by the 'fclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);

    public static byte[] stringToHex(String s){
        byte[] hex;
        try{
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException e) {
            hex = null;
        }
        return hex;
    }
}
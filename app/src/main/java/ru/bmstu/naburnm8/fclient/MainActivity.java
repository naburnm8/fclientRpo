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
import org.apache.commons.io.IOUtils;

import ru.bmstu.naburnm8.fclient.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    // Used to load the 'fclient' library on application startup.
    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
        Log.println(Log.INFO, "Library Loaded", "Library Loaded");
    }

    private ActivityMainBinding binding;

    private Button button;
    private TextView textView;

    private String pin;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);

        textView = findViewById(R.id.textView);

        button.setOnClickListener(v -> {
            /*
            byte[] key = stringToHex("0123456789ABCDEF0123456789ABCDE0");
            byte[] enc = encrypt(key, stringToHex("000000000000000102"));
            byte[] dec = decrypt(key, enc);
            String s = new String(Hex.encodeHex(dec)).toUpperCase();
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

            Intent it = new Intent(this, PinpadActivity.class);
            activityResultLauncher.launch(it);
             */
            testHttpClient();
        });

        button.setOnLongClickListener(view -> {
            new Thread(() -> {
                try {
                    byte[] trd = stringToHex("9F0206000000000100");
                    transaction(trd);

                } catch (Exception ex) {
                    Log.e("MainActivity.transaction", ex.getMessage());
                }
            }).start();
            return true;
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        //String pin = data.getStringExtra("pin");
                        assert data != null;
                        pin = data.getStringExtra("pin");
                        synchronized (MainActivity.this) {
                            MainActivity.this.notifyAll();
                        }

                        //Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
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

    @Override
    public String enterPin(int ptc, String amount) {
        pin = "";
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                Log.println(Log.ERROR, "MainActivity.enterPin", ex.getMessage());
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(()-> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    public native boolean transaction(byte[] trd);

    protected String getPageTitle(String html)
    {
        Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        String p;
        if (matcher.find())
            p = matcher.group(1);
        else
            p = "Not found";
        return p;
    }
    protected void testHttpClient(){
        new Thread(() -> {
            try {
                HttpURLConnection uc = (HttpURLConnection)
                        (new URL("http://10.0.2.2:8080/api/v1/title").openConnection());
                InputStream inputStream = uc.getInputStream();
                String html = IOUtils.toString(inputStream);
                String title = getPageTitle(html);
                runOnUiThread(() ->
                {
                    textView.setText(title);
                });

            } catch (Exception ex) {
                Log.e("fapptag", "Http client fails", ex);
            }
        }).start();
    }
}
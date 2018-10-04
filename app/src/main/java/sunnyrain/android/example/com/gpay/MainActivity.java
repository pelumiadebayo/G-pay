package sunnyrain.android.example.com.gpay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import static android.R.attr.button;
import static android.R.id.message;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static sunnyrain.android.example.com.gpay.R.id.login;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = (Button) findViewById(R.id.login);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAccessToken();
           Intent intent= new Intent(MainActivity.this, Form.class);
                    intent.putExtra(EXTRA_MESSAGE, accessToken);
            }
        });
    }


    String accessToken;
    String url ="http://104.131.174.54:2673/api/v2.0/authentication/token";

    public void getAccessToken() {
        Ion.with(this)
        .load(url)
        .setBodyParameter("username", "user")
        .setBodyParameter("pass", "user")
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, "Error loading", Toast.LENGTH_LONG).show();
                    return;
                }
                accessToken = result.get("token").getAsString();
                return;
            }
        });
    }

}

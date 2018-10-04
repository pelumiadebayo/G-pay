package sunnyrain.android.example.com.gpay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.net.URL;

public class Form extends AppCompatActivity {
    ArrayAdapter<JsonObject> historyAdapter;
    String accessToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            accessToken = extras.getString("accessToken");
        }
        historyAdapter = new ArrayAdapter<JsonObject>(this, 0){

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.history, null);

                if (position >= getCount())
                    load();

                // grab the history
                JsonObject data = getItem(position);
                JsonObject bearerHistory = data.getAsJsonObject("data");
                if (bearerHistory != null)
                    data = bearerHistory;

                // grab the user info... name, date of transaction
                JsonObject bearerName = data.getAsJsonObject("name");
                String date = bearerName.get("date").getAsString();


                // and finally, set the name and date
                TextView handle = (TextView)convertView.findViewById(R.id.date_list);
                handle.setText(date);

                TextView text = (TextView)convertView.findViewById(R.id.name_list);
                text.setText(bearerName.get("text").getAsString());
                return convertView;
            }
        };

        // basic setup of the ListView and adapter
        setContentView(R.layout.history_list);
        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(historyAdapter);

        // authenticate and do the first load

    }

    Future<JsonArray> loading;

    private void load() {
        EditText acctNumber = (EditText) findViewById(R.id.AccountNumber);
        String id = acctNumber.getText().toString();

        String url = "http://104.131.174.54:2673/api/v2.0/electricity/payments?unique_id" + id;

        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;
        
        // This request loads a URL as JsonArray and invokes a callback on completion.
         loading = Ion.with(this)
            .load(url)

            .setHeader("Authorization", "Bearer " +accessToken)
            .asJsonArray()
            .setCallback(new FutureCallback<JsonArray>() {
                @Override
                public void onCompleted(Exception e, JsonArray result) {
                    // this is called back onto the ui thread,
                    if (e != null) {
                        Toast.makeText(Form.this, "Error loading history", Toast.LENGTH_LONG).show();
                        return;
                    }
                    for (int i = 0; i < result.size(); i++) {
                        historyAdapter.add(result.get(i).getAsJsonObject());
                    }
                }
            });
    }

}

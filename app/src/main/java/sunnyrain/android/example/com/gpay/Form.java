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
import com.jaredrummler.materialspinner.MaterialSpinner;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static android.R.attr.data;
import static android.R.attr.handle;
import static android.R.attr.id;
import static android.R.attr.name;
import static sunnyrain.android.example.com.gpay.R.id.spinner;
import static sunnyrain.android.example.com.gpay.R.string.electricity;

public class Form extends AppCompatActivity {
    ArrayAdapter<JsonObject> historyAdapter;
    Future<JsonArray> loading;
    String accessToken;
    String operationId;
    JSONObject operation;
    JSONObject transactionInfo;

    final String DATA = "data";
    final String ID = "id";
    final String NAME = "name";
    final String TRANS_AMOUNT = "amount";
    final String TRANS_DATE = "created";

    ArrayList<String> opId = new ArrayList<String>();
    ArrayList<String> idName = new ArrayList<String>();
    ArrayList<String> transactionName = new ArrayList<String>();
    ArrayList<String> transactionAmount = new ArrayList<String>();
    ArrayList<String> transactionDate = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            accessToken = extras.getString("accessToken");
        }
        getOperatorId();

        historyAdapter = new ArrayAdapter<JSONObject>(this, 0){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.history, null);

                if (position >= getCount())
                    load();

                JSONObject data = getItem(position);
                JSONArray transactionInfo = null;
                try {
                    transactionInfo = data.getJSONArray(DATA);
                } catch (JSONException e) {

                }
                for (int i = 0; i < transactionInfo.length(); i++) {
                    try {
                        JSONObject infoObject = transactionInfo.getJSONObject(i);
                        String name = infoObject.getString(NAME);
                        String amount = infoObject.getString(TRANS_AMOUNT);
                        String date = infoObject.getString(TRANS_DATE);

                        // and finally, set the name and date
                        TextView dateCreated = (TextView)convertView.findViewById(R.id.date_list);
                        dateCreated.setText(date);

                        TextView text = (TextView)convertView.findViewById(R.id.name_list);
                        text.setText(name);

                        TextView amt = (TextView)convertView.findViewById(R.id.name_list);
                        amt.setText(amount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // basic setup of the ListView and adapter
                setContentView(R.layout.history_list);
                ListView listView = (ListView)findViewById(R.id.list);
                listView.setAdapter(historyAdapter);
                // authenticate and do the first load
                load();

            }

    };
    }

    private void getOperatorId(){
        String url = "http://104.131.174.54:2673/api/v2.0/electricity/operators" ;
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        // This request loads a URL as JsonArray and invokes a callback on completion.
        loading = Ion.with(this)
                .load(url)
                .setHeader("Authorization", "Bearer " + accessToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        try {
                            operation = new JSONObject(String.valueOf(result));
                        } catch (Exception el) {
                            el.printStackTrace();
                        }
                    }
                });
        try {
            JSONArray data = operation.getJSONArray(DATA);
            for (int i = 0; i < data.length(); i++) {
                JSONObject operationObject =  data.getJSONObject(i);
                opId.add(operationObject.getString(ID));
                idName.add(operationObject.getString(NAME));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void load() {
        EditText acctNumber = (EditText) findViewById(R.id.AccountNumber);
        String uId = acctNumber.getText().toString();

        final MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner);
        spinner.setItems(idName);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                operationId = opId.get(position);
            }
        });
        spinner.setOnNothingSelectedListener(new MaterialSpinner.OnNothingSelectedListener() {
            @Override
            public void onNothingSelected(MaterialSpinner spinner) {
                return;
            }
        });

        String url = "http://104.131.174.54:2673/api/v2.0/electricity/payments?unique_id="
                + uId + "&" + "operator_id=" + operationId;

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
                    historyAdapter.add(result.getAsJsonObject());
                }
            });
    }

}

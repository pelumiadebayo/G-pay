package sunnyrain.android.example.com.gpay;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static android.R.attr.cacheColorHint;
import static android.R.attr.id;

public class WalletDetails extends AppCompatActivity {
    private Spinner mProductSpinner;
    JSONObject telecommunications;
    JSONObject electricity;

    final String DATA = "data";
    final String PAYMENT = "payments";
    final String TODAY_SUM = "sum_today";
    final String MONTHLY_SUM = "sum_this_month";

    String todaySum;
    String monthlySum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_details);
        mProductSpinner = (Spinner) findViewById(R.id.spinner);
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the product summary.
     */
    private void setupSpinner() {
        // Create adapter for spinner.
        // the spinner will use the default layout
        ArrayAdapter walletSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.wallet_details_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        walletSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mProductSpinner.setAdapter(walletSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mProductSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.electricity))) {
                        setTitle(getString(R.string.electricity));
                            electricityWallet();
                    } else if (selection.equals(getString(R.string.telecommunication))) {
                        setTitle(getString(R.string.telecommunication));
                            telecommunicationsWallet();
                    } else {
                        setTitle(getString(R.string.electricity));
                        electricityWallet();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setTitle(getString(R.string.electricity));
                electricityWallet();
            }
        });
    }

    Future<JsonArray> loading;

    private void electricityWallet() {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        Ion.with(this)
                .load("http://104.131.174.54:2673/api/v2.0/electricity/stats")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            electricity = new JSONObject(result);
                        } catch (Exception el) {
                            el.printStackTrace();
                        }
                    }
                });
        try {
            JSONObject electricityObject = electricity.getJSONObject(DATA);
            JSONObject payment = electricityObject.getJSONObject(PAYMENT);
            todaySum = payment.getString(TODAY_SUM);
            monthlySum = payment.getString(MONTHLY_SUM);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView electricityWalletToday =(TextView) findViewById(R.id.today);
        TextView electricityWalletMonthly =(TextView) findViewById(R.id.monthly);

        electricityWalletToday.setText(todaySum);
        electricityWalletMonthly.setText(monthlySum);
    }

    private void telecommunicationsWallet() {
        // don't attempt to load more if a load is already in progress
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        Ion.with(this)
                .load("http://104.131.174.54:2673/api/v2.0/telecommunications/stats")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            telecommunications = new JSONObject(result);
                        } catch (Exception el) {
                            el.printStackTrace();
                        }
                    }
                });
        try {
            JSONObject electricityObject = telecommunications.getJSONObject(DATA);
            JSONObject payment = electricityObject.getJSONObject(PAYMENT);
            todaySum = payment.getString(TODAY_SUM);
            monthlySum = payment.getString(MONTHLY_SUM);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView telecommunicationsWalletToday =(TextView) findViewById(R.id.today);
        TextView telecommunicationsWalletMonthly =(TextView) findViewById(R.id.monthly);

        telecommunicationsWalletToday.setText(todaySum);
        telecommunicationsWalletMonthly.setText(monthlySum);
    }

}



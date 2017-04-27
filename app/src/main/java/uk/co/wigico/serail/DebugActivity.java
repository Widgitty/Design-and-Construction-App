package uk.co.wigico.serail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static uk.co.wigico.serail.MainActivity.SetData;


public class DebugActivity extends ActionBarActivity {

    ImageButton dial;
    TextView screenText;
    TextView resultText;

    int mode = 0;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");
            resultText.post(new Runnable() {
                @Override
                public void run() {
                    resultText.setText(message);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // Import UI components
        dial = (ImageButton) findViewById(R.id.imageViewDial);
        screenText = (TextView) findViewById(R.id.textViewScreen);
        resultText = (TextView) findViewById(R.id.textViewResult);
        //screenText.setText(Double.toString(MainActivity.GetData()));
        screenText.setText(Double.toString(MainActivity.received));
        Toast.makeText(this, Double.toString(MainActivity.received), Toast.LENGTH_SHORT).show();
        dial.setRotation(-90);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("dataUpdate"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDialClick(View view) {
        mode++;
        if (mode > 8) {
            mode = 0;
        }

        String textMode = "Current";
        switch(mode) {
            case 0:
                screenText.setText("Current");
                textMode = "Current";
                break;
            case 1:
                screenText.setText("Voltage");
                textMode = "Voltage";
                break;
            case 2:
                screenText.setText("Voltage RMS");
                textMode = "Voltage RMS";
                break;
            case 3:
                screenText.setText("Resistance");
                textMode = "Resistance";
                break;
            case 4:
                screenText.setText("Capacitance");
                textMode = "Capacitance";
                break;
            case 5:
                screenText.setText("Inductance");
                textMode = "Inductance";
                break;
            case 6:
                screenText.setText("Frequency");
                textMode = "Frequency";
                break;
            case 7:
                screenText.setText("Continuity Test");
                textMode = "Continuity Test";
                break;
            case 8:
                screenText.setText("Diode Test");
                textMode = "Diode Test";
                break;
        }

        dial.setRotation(40*mode - 90);
        SetData(textMode,getApplicationContext());
    }
}

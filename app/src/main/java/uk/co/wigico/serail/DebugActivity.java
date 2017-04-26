package uk.co.wigico.serail;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class DebugActivity extends ActionBarActivity {

    ImageButton dial;
    TextView screenText;

    int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // Import UI components
        dial = (ImageButton) findViewById(R.id.imageViewDial);
        screenText = (TextView) findViewById(R.id.textViewScreen);
        //screenText.setText(Double.toString(MainActivity.GetData()));
        screenText.setText(Double.toString(MainActivity.received));
        Toast.makeText(this, Double.toString(MainActivity.received), Toast.LENGTH_SHORT).show();

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
        if (mode > 5) {
            mode = 0;
        }
        switch(mode){
            case 0:
                screenText.setText("Current");
                break;
            case 1:
                screenText.setText("Voltage");
                break;
            case 2:
                screenText.setText("Resistance");
                break;
            case 3:
                screenText.setText("Capacitance");
                break;
            case 4:
                screenText.setText("Inductance");
                break;
            case 5:
                screenText.setText("Frequency");
                break;
        }

        dial.setRotation((mode*20)-50);
    }
}

package uk.co.wigico.serail;

import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.DefaultLabelFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;

import static android.os.SystemClock.elapsedRealtime;


public class PlotActivity extends ActionBarActivity{

    LineGraphSeries<DataPoint> data;
    int count = 1;
    GraphView graph;
    boolean record = false;
    Button btnStartStop;
    RadioButton btnLow;
    RadioButton btnHigh;
    Long startTime = (long)0;
    int recordingMode = 0;
    String[] units = {"A", "V", "\u03A9","C", "H","Hz"};
    EditText setSampleRate;
    int sampleRate = 500;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        graph = (GraphView) findViewById(R.id.graphView_graph);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(5);
        graph.getViewport().setMinX(0);




        data = new LineGraphSeries<DataPoint>();
        //data.appendData(new DataPoint(count, 1.0), true, count);
        //count ++;
        //data.appendData(new DataPoint(count, 2.0), true, count);
        //count ++;
        graph.addSeries(data);

        btnStartStop = (Button) findViewById(R.id.btnStartStop);

        setSampleRate = (EditText)findViewById(R.id.setSampleRate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plot, menu);
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




    class UpdateThread implements Runnable {

        @Override
        public void run(){

            graph.removeAllSeries();
            data = new LineGraphSeries<DataPoint>();
            graph.addSeries(data);

            while(record) {
                //ThreadToast("Update");
                data.appendData(new DataPoint((elapsedRealtime () - startTime), MainActivity.received), true, count);
                count++;
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMaxX((elapsedRealtime () - startTime));
                graph.getViewport().setMinX(0);
                LabelFormatter labelFormatter = (new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            // scale time values into seconds.
                            return super.formatLabel(value*0.01, isValueX);
                        } else {
                            // display mode dependant units on Y axis
                            return super.formatLabel(value, isValueX) + units[MainActivity.mode];
                        }
                    }
                });

                //graph.getGridLabelRenderer().setPadding(1);
                //graph.getGridLabelRenderer().setLabelsSpace(5);
                graph.getGridLabelRenderer().setLabelVerticalWidth(100);

                graph.getGridLabelRenderer().setLabelFormatter(labelFormatter);



                try {
                    Thread.sleep(sampleRate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (recordingMode != MainActivity.mode) {
                    record = false;
                    btnStartStop.setText("Start");
                    ThreadToast("Mode changed!");
                }

            }

        }

    }

    public void onStartStopClick(View view) {
        if (record) {
            record = false;
            btnStartStop.setText("Start");

        }
        else {
            record = true;
            sampleRate = Integer.parseInt(setSampleRate.getText().toString());
            sampleRate = sampleRate * 1000;
            //data.setAnimated(true);
            recordingMode = MainActivity.mode;
            new Thread(new UpdateThread()).start();
            startTime = elapsedRealtime ();
            btnStartStop.setText("Stop");
        }
    }

/*
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.btn_Low:
                if (checked)
                    sampleRate = 500;
                    break;
            case R.id.btn_High:
                if (checked)
                    sampleRate = 50;
                    break;
            default:
                sampleRate = 500;
                break;
        }
    }
    */


    public void onSaveClick(View view) {

        //Toast.makeText(getApplicationContext(), "Save", Toast.LENGTH_SHORT).show();
        FileOutputStream outputStream;

        //String fileName = ((EditText) findViewById(R.id.edit_file)).getText().toString();
        String fileName = "OUTPUT.csv";

        if (!fileName.endsWith(".csv")) {
            fileName = String.format("%s.csv", fileName);
        }

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/Wigico/Serail");
        dir.mkdirs();
        File file = new File(dir, fileName);

        try {
            outputStream = new FileOutputStream(file);

            Iterator<DataPoint> iterator = data.getValues(data.getLowestValueX(), data.getHighestValueX());
            while (iterator.hasNext()) {
                DataPoint dataPoint = iterator.next();
                outputStream.write(Double.toString(dataPoint.getX()).getBytes());
                outputStream.write(", ".getBytes());
                outputStream.write(Double.toString(dataPoint.getY()).getBytes());
                outputStream.write("\n".getBytes());
            }

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setResult(RESULT_CANCELED);
        ThreadToast("Saved");
    }



    public void ThreadToast (String str) {
        final String strf = str;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), strf, Toast.LENGTH_SHORT).show();
            }
        });
    }


}



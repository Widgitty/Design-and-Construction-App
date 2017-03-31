package uk.co.wigico.serail;

import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class PlotActivity extends ActionBarActivity {

    LineGraphSeries<DataPoint> data;
    int count = 1;
    GraphView graph;

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
        new Thread(new UpdateThread()).start();
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
        public void run() {

            while(true) {
                //ThreadToast("Update");
                data.appendData(new DataPoint(count, MainActivity.received), true, count);
                count ++;
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMaxX(count);
                graph.getViewport().setMinX(0);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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

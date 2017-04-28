package uk.co.wigico.serail;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.ksksue.driver.serial.FTDriver;
import android.hardware.usb.UsbManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.TimeoutException;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {



    public enum Mode_Type {
        AT, FT, WiFi
    }

    public static volatile FTDriver mSerial;
    public static volatile boolean read = false;
    char LEDs = 0x00;
    static Mode_Type Mode = Mode_Type.AT;

    // TCP stuff
    private static Socket socket;
    public static boolean SerialFlag = false;
    private static final int PORT = 1138;
    private static final String IP = "192.168.1.1";

    // Interface components
    public static volatile ImageView LED1, LED2, LED3, LED4, LED5, LED6, LED7, LED8;
    public static ImageView[] LED = {LED1, LED2, LED3, LED4, LED5, LED6, LED7, LED8};
    Button btnBegin, /*btnRead, btnEndRead,*/ btnWrite, btnEnd;
    public static volatile TextView tvMonitor;
    EditText sendMessage;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;
    Spinner modeSelector;

    // [FTDriver] Permission String
    private static final String ACTION_USB_PERMISSION =
            "jp.ksksue.tutorial.USB_PERMISSION";

    // Static variables
    public static volatile double received = 0.1;
    public static volatile int mode = 0;
    public static volatile int oldMode = 0;
    public static volatile int range = 0;
    public static volatile boolean connected = false;

    boolean modeSet = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Import UI components
        btnBegin = (Button) findViewById(R.id.btnBegin);
        //btnRead = (Button) findViewById(R.id.btnRead);
        //btnEndRead = (Button) findViewById(R.id.btnEndRead);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        radioButton1 = (RadioButton) findViewById(R.id.radio_AT);
        radioButton2 = (RadioButton) findViewById(R.id.radio_FT);
        radioButton3 = (RadioButton) findViewById(R.id.radio_WiFi);
        modeSelector = (Spinner) findViewById(R.id.modeSelectionSpinner);




        /*LED[0] = (ImageView) findViewById(R.id.led_1);
        LED[1] = (ImageView) findViewById(R.id.led_2);
        LED[2] = (ImageView) findViewById(R.id.led_3);
        LED[3] = (ImageView) findViewById(R.id.led_4);
        LED[4] = (ImageView) findViewById(R.id.led_5);
        LED[5] = (ImageView) findViewById(R.id.led_6);
        LED[6] = (ImageView) findViewById(R.id.led_7);
        LED[7] = (ImageView) findViewById(R.id.led_8);*/

        sendMessage = (EditText) findViewById(R.id.sendMessage);

        tvMonitor = (TextView) findViewById(R.id.tvMonitor);

        // Set default states
        //btnRead.setEnabled(false);
        //btnEndRead.setEnabled(false);
        btnWrite.setEnabled(false);
        btnEnd.setEnabled(false);


        // [FTDriver] Create Instance
        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));

        // [FTDriver] setPermissionIntent() before begin()
        // This asks the OS for permission to use the USB device
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSelector.setAdapter(adapter);
        modeSelector.setOnItemSelectedListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mModeReceiver,
                new IntentFilter("modeUpdate"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;
        Bundle connectionType= new Bundle();
        if(Mode == Mode_Type.AT){
            connectionType.putInt("connectionType", 0);
        }
        else if(Mode == Mode_Type.FT){
            connectionType.putInt("connectionType", 1);

        }
        else{
            connectionType.putInt("connectionType", 2);

        }

        switch (item.getItemId()) {
            case R.id.action_favorite:
                // About option clicked.
                Toast.makeText(this, "Icon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_GUI:
                // Exit option clicked.
                //Toast.makeText(this, "Debug", Toast.LENGTH_SHORT).show();

                intent = new Intent(this, DebugActivity.class);
                intent.putExtras(connectionType);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);

                return true;
            case R.id.action_plot:
                // Exit option clicked.
                Toast.makeText(this, "Data Logger", Toast.LENGTH_SHORT).show();

                intent = new Intent(this, PlotActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);

                return true;

            case R.id.action_nav:
                // Exit option clicked.
                Toast.makeText(this, "Nav", Toast.LENGTH_SHORT).show();

                //intent = new Intent(this, NavigationDrawerActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                //startActivity(intent);

                return true;

            case R.id.action_settings:
                // Settings option clicked.
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // [FTDriver] Close USB Serial
        mSerial.end();
    }




    //====================================//
    //======= Handle button pushes =======//
    //====================================//

    public void onBeginClick(View view) {
        if (Mode == Mode_Type.AT) {
            new Thread(new ATSerialThread()).start();
        }
        else if (Mode == Mode_Type.FT) {
            Toast.makeText(this, "FTDI chip not supported", Toast.LENGTH_SHORT).show();
        }
        else if (Mode == Mode_Type.WiFi) {
            // TODO: Add socket support
            new Thread(new WiFiThread()).start();
        }


    }


    public void onWriteClick(View view) {
        // Serial print string from text box
        String str = sendMessage.getText().toString();
        Toast.makeText(this, "Sending: " + str, Toast.LENGTH_SHORT).show();

        if (Mode == Mode_Type.AT) {

            mSerial.write("s");
            mSerial.write(str.getBytes());
            mSerial.write("\n");

        }
        else if (Mode == Mode_Type.FT) {
            Toast.makeText(this, "FTDI chip not supported", Toast.LENGTH_SHORT).show();
        }
        else if (Mode == Mode_Type.WiFi) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.println("s" + str);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void onEndClick(View view) {
        read = false;
    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_AT:
                if (checked)
                    Toast.makeText(this, "AT chip selected", Toast.LENGTH_SHORT).show();
                    Mode = Mode_Type.AT;
                    break;
            case R.id.radio_FT:
                if (checked)
                    Toast.makeText(this, "FT chip not yet supported", Toast.LENGTH_SHORT).show();
                    ((RadioButton) findViewById(R.id.radio_FT)).setChecked(false);
                    ((RadioButton) findViewById(R.id.radio_AT)).setChecked(true);
                    break;
            case R.id.radio_WiFi:
                if (checked)
                    Toast.makeText(this, "WiFi selected", Toast.LENGTH_SHORT).show();
                    Mode = Mode_Type.WiFi;
                break;
        }

    }






    //====================================//
    //====== Communication  Threads ======//
    //====================================//


    class ATSerialThread implements Runnable {

        @Override
        public void run() {

            //mSerial.setFlowControl()
            if (mSerial.begin(FTDriver.BAUD115200)) {
                // TODO: Make a function to do this properly
                MaskButtonsConnected(true);
                ThreadToast("Connected");
                SerialFlag = true;
                read = true;
            } else {
                ThreadToast("Cannot connect");
                read = false;
            }


            int i,len;
            char LEDs = 0x00;
            byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte

            connected = true;
            while (read) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                len = mSerial.read(rbuf);

                for (i = 0; i < len; i++) {

                    HandleByte(rbuf[i]);

                }
            }

            // End
            mSerial.end();
            MaskButtonsConnected(false);
            ThreadToast("Disconnected");
            SerialFlag = false;
            connected = false;

        }
    }


    class WiFiThread implements Runnable {

        @Override
        public void run() {

            byte[] buffer = new byte[1];
            InputStream inputStream = null;

            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                socket = new Socket(serverAddr, PORT);
                socket.setSoTimeout(1000);
                inputStream = socket.getInputStream();
                read = true;
                MaskButtonsConnected(true);
                ThreadToast("Connected to socket");

                //TODO: could use this?
                /*
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
                */

            } catch (UnknownHostException e) {
                e.printStackTrace();
                ThreadToast("Board not found");
                read = false;
            } catch (IOException e) {
                e.printStackTrace();
                ThreadToast("Board not found");
                read = false;
            }

            connected = true;
            while (read) {
                try {
                    if (inputStream.read(buffer) > 0) {

                        HandleByte(buffer[0]);

                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // Expected exception
                    ThreadToast("Board not found");
                }
            }

            // TODO: close and exit
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connected = false;
            ThreadToast("Disconnected");
            MaskButtonsConnected(false);
        }
    }


    //====================================//
    //========== State Machine  ==========//
    //====================================//

    final ByteBuffer bb = ByteBuffer.allocate(1000);
    int state = 0;
    int loopCounter = 0;

    int checksum = 0;

    public void HandleByte(byte data) {

        switch (state) {

            case 0:
                if ((char) data == 'D') { // check for start byte
                    checksum = checksum ^ data;
                    state = 1;
                }
                break;

            case 1:
                if ((char) data == '1') { // only data type '1' currently supported
                    checksum = checksum ^ data;
                    state = 2;
                }
                else {
                    checksum = 0;
                    state = 0; // treat as misscommunication if not '1'
                }
                break;

            case 2:
                // receive data
                bb.put(data);
                checksum = checksum ^ data;
                loopCounter++;
                if (loopCounter >= 8) {
                    loopCounter = 0;
                    state = 3;
                }
                break;

            case 3:
                // receive range
                range = (int)data;
                checksum = checksum ^ data;
                state = 4;
                break;

            case 4:
                // receive mode
                mode = (int)data;
                checksum = checksum ^ data;
                state = 5;
                break;

            case 5:
                // checksum
                if (checksum == (int)data) {
                    checksum = (int)data;
                    String units;
                    String str;

                    bb.rewind();
                    received = bb.getDouble(0);
                    str = "";
                    units = "";
                    str = (str + "Double: ");
                    str = (str + String.format("%.5f\n", received));

                    str = (str + "Range: ");
                    str = (str + String.format("%d\n", range));
                    switch (range) {
                        case 0:
                            units = "n";
                            break;
                        case 1:
                            units = "\u03BC"; // mu
                            break;
                        case 2:
                            units = "m";
                            break;
                        case 3:
                            units = "";
                            break;
                        case 4:
                            units = "k";
                            break;
                        case 5:
                            units = "M";
                            break;
                        default:
                            units = "?";
                            break;
                    }

                    str = (str + "Mode: ");
                    str = (str + String.format("%d\n", mode));
                    switch (mode) {
                        case 0:
                            units = (units + "A");
                            break;
                        case 1:
                            units = (units + "V");
                            break;
                        case 2:
                            units = (units + "V");
                            break;
                        case 3:
                            units = (units + "\u03A9"); //ohms
                            break;
                        case 4:
                            units = (units + "F");
                            break;
                        case 5:
                            units = (units + "H");
                            break;
                        case 6:
                            units = (units + "Hz");
                            break;
                        default:
                            units = (units + "?");
                            break;
                    }

                    str = (str + "Checksum: ");
                    str = (str + String.format("%d\n", checksum));

                    str = (str + "\nOutput:\n");
                    str = (str + String.format("%.5f %s\n", received, units));

                    DisplayString(str);
                    sendDataBroadCast(received, units);
                    if(oldMode != mode)
                        sendModeBroadcast(mode);
                    oldMode = mode;
                }
                else {
                    DisplayString("Checksum failed!");
                }
                checksum = 0x00;
                bb.clear();
                state = 0;
                break;

        }

    }

    //====================================//
    //======= Supporting functions =======//
    //====================================//

    public void updateLEDs(char LEDs)
    {
        int i;
        for(i = 0; i < 8; i++) {
            if (((LEDs >> i) & 0x01) == 0x01) {
                final int j = i;
                LED[i].post(new Runnable() {
                    @Override
                    public void run() {
                        LED[j].setImageResource(R.drawable.led_on);
                    }
                });
            } else {
                final int j = i;
                LED[i].post(new Runnable() {
                    @Override
                    public void run() {
                        LED[j].setImageResource(R.drawable.led_off);
                    }
                });
            }
        }
    }


    public void DisplayString(String str) {
        final String strSend = str;
        tvMonitor.post(new Runnable() {
            @Override
            public void run() {
                tvMonitor.setText(strSend);
            }
        });
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


    public void MaskButtonsConnected (final Boolean mask) { // True when connected

        btnBegin.post(new Runnable() {
            @Override
            public void run() {
                btnBegin.setEnabled(!mask);
            }
        });

        btnWrite.post(new Runnable() {
            @Override
            public void run() {
                btnWrite.setEnabled(mask);
            }
        });

        btnEnd.post(new Runnable() {
            @Override
            public void run() {
                btnEnd.setEnabled(mask);
            }
        });

        radioButton1.post(new Runnable() {
            @Override
            public void run() {
                radioButton1.setEnabled(!mask);
            }
        });

        radioButton2.post(new Runnable() {
            @Override
            public void run() {
                radioButton2.setEnabled(!mask);
            }
        });

        radioButton3.post(new Runnable() {
            @Override
            public void run() {
                radioButton3.setEnabled(!mask);
            }
        });
    }

    /*Spinner Listener, to do something when an item is selected*/
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String item = adapterView.getItemAtPosition(i).toString();
        tvMonitor.setText(item);
        modeSet = true;
        SetData(i, getApplicationContext());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Toast.makeText(getApplicationContext(), "No mode selected", Toast.LENGTH_SHORT).show();
    }





    private BroadcastReceiver mModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int newMode = intent.getIntExtra("modeUpdate", 0);
            mode = newMode;

            if(!modeSet) {
                modeSelector.post(new Runnable() {
                    @Override
                    public void run() {
                        modeSelector.setSelection(mode);
                    }
                });
            }
            modeSet = false;
        }
    };






    //====================================//
    //======= Interface =======//
    //====================================//

    public static double GetData() {
        return received;
    }

    public static void SetData(int input, Context context){

        String item;
        item = "m" + Integer.toString(input);

        Log.d("LOG", item);

        if ((Mode == Mode_Type.AT) && SerialFlag) {
            mSerial.write(item);
            mSerial.write("\n");
        }
        else if (Mode == Mode_Type.FT) {
            Toast.makeText(context, "FTDI chip not supported", Toast.LENGTH_SHORT).show();
        }
        else if (Mode == Mode_Type.WiFi) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(item);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendDataBroadCast(double value, String input) {
        Intent intent = new Intent("dataUpdate");
        // You can also include some extra data.

        intent.putExtra("message", new DecimalFormat("#.###").format(value));
        intent.putExtra("range", input);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void sendModeBroadcast(int mode) {
        Intent intent = new Intent("modeUpdate");
        // You can also include some extra data.

        intent.putExtra("modeUpdate", mode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}



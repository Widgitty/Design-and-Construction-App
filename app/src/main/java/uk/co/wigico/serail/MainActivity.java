package uk.co.wigico.serail;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import jp.ksksue.driver.serial.FTDriver;
import android.hardware.usb.UsbManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;


public class MainActivity extends ActionBarActivity {

    public enum Mode_Type {
        AT, FT, WiFi
    }

    public static volatile FTDriver mSerial;
    public static volatile boolean read = false;
    char LEDs = 0x00;
    Mode_Type Mode = Mode_Type.AT;

    // TCP stuff
    private Socket socket;
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

    // [FTDriver] Permission String
    private static final String ACTION_USB_PERMISSION =
            "jp.ksksue.tutorial.USB_PERMISSION";




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

        LED[0] = (ImageView) findViewById(R.id.led_1);
        LED[1] = (ImageView) findViewById(R.id.led_2);
        LED[2] = (ImageView) findViewById(R.id.led_3);
        LED[3] = (ImageView) findViewById(R.id.led_4);
        LED[4] = (ImageView) findViewById(R.id.led_5);
        LED[5] = (ImageView) findViewById(R.id.led_6);
        LED[6] = (ImageView) findViewById(R.id.led_7);
        LED[7] = (ImageView) findViewById(R.id.led_8);

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

        switch (item.getItemId()) {
            case R.id.action_favorite:
                // About option clicked.
                Toast.makeText(this, "Icon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_debug:
                // Exit option clicked.
                Toast.makeText(this, "Debug", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, DebugActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);

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

        if (Mode == Mode_Type.AT) {

            // TODO: this is not compatible with the WiFI mode
            String wbuf = sendMessage.getText().toString();
            Toast.makeText(this, "Sending: " + wbuf, Toast.LENGTH_SHORT).show();

            // [FTDriver] Wirte to USB Serial
            mSerial.write(wbuf.getBytes());
            mSerial.write("\n");

        }
        else if (Mode == Mode_Type.FT) {
            Toast.makeText(this, "FTDI chip not supported", Toast.LENGTH_SHORT).show();
        }
        else if (Mode == Mode_Type.WiFi) {
            // TODO: Add socket support
            Toast.makeText(this, "Transmit not currently supported for WiFi mode", Toast.LENGTH_SHORT).show();
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
                read = true;
            } else {
                ThreadToast("Cannot connect");
                read = false;
            }


            int i,len;
            char LEDs = 0x00;
            final StringBuilder mText = new StringBuilder();
            byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
            while (read) {
                len = mSerial.read(rbuf);

                for (i = 0; i < len; i++) {
                    //if (isLetterOrDigit((char) rbuf[i]))
                    mText.append((char) rbuf[i]);
                    if ((char)rbuf[i] == '\n') {
                        DisplayString(mText.toString());
                        mText.delete(0, mText.length());
                    }
                }

                if (len > 0) {
                    LEDs = (char) rbuf[len - 1];

                    // This function communicates back to the UI components by passing a
                    // runnable function to the component, as described in the Android
                    // documentation.
                    updateLEDs(LEDs);
                }
            }

            // End
            mSerial.end();
            MaskButtonsConnected(false);
            ThreadToast("Disconnected");

        }
    }


    class WiFiThread implements Runnable {

        @Override
        public void run() {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1];
            int bytesRead;
            InputStream inputStream = null;
            String response = "";
            final StringBuilder mText = new StringBuilder();

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
                ThreadToast("Failed to connect to socket");
                read = false;
            } catch (IOException e) {
                e.printStackTrace();
                ThreadToast("IO Exception");
                read = false;
            }

            int Bytes;
            while (read) {
                try {
                    if ((Bytes = inputStream.read(buffer)) > 0) {
                        mText.append((char)buffer[0]);
                        if ((char)buffer[0] == '\n') {
                            DisplayString(mText.toString());
                            mText.delete(0, mText.length());
                        }
                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    //ThreadToast("Timeout");
                } catch (IOException e) {
                    // Expected exception
                    ThreadToast("IO exception");
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

            ThreadToast("Disconnected");
            MaskButtonsConnected(false);
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
}

















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

import static java.lang.Character.isLetter;
import static java.lang.Character.isLetterOrDigit;

public class MainActivity extends ActionBarActivity {

    public enum Mode_Type {
        AT, FT, WiFi
    }

    public static volatile FTDriver mSerial;
    public static volatile boolean read = false;
    char LEDs = 0x00;
    Mode_Type Mode = Mode_Type.AT;


    // Interface components
    public static volatile ImageView LED1, LED2, LED3, LED4, LED5, LED6, LED7, LED8;
    public static ImageView[] LED = {LED1, LED2, LED3, LED4, LED5, LED6, LED7, LED8};
    Button btnBegin,btnRead,btnEndRead, btnWrite, btnEnd;
    public static volatile TextView tvMonitor;
    EditText sendMessage;

    // [FTDriver] Permission String
    private static final String ACTION_USB_PERMISSION =
            "jp.ksksue.tutorial.USB_PERMISSION";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Import UI components
        btnBegin = (Button) findViewById(R.id.btnBegin);
        btnRead = (Button) findViewById(R.id.btnRead);
        btnEndRead = (Button) findViewById(R.id.btnEndRead);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnEnd = (Button) findViewById(R.id.btnEnd);

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
        btnRead.setEnabled(false);
        btnEndRead.setEnabled(false);
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
        int id = item.getItemId();

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



    public void onBeginClick(View view) {
        // Establish connection

        // [FTDriver] Open USB Serial
        if (Mode == Mode_Type.AT) {
            //mSerial.setFlowControl()
            if (mSerial.begin(FTDriver.BAUD115200)) {
                btnBegin.setEnabled(false);
                btnRead.setEnabled(true);
                btnEndRead.setEnabled(true);
                btnWrite.setEnabled(true);
                btnEnd.setEnabled(true);

                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "cannot connect", Toast.LENGTH_SHORT).show();
            }
        }
        else if (Mode == Mode_Type.WiFi) {
            Toast.makeText(this, "Could not connect to socket", Toast.LENGTH_SHORT).show();
            // TODO: Add socket support
        }
    }

    public void onReadClick(View view) {
        // Begin reading data

        read = true;
        btnEnd.setEnabled(false);

        int i,len;

        final StringBuilder mText = new StringBuilder();
        byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte


        // [FTDriver] Read from USB Serial
        len = mSerial.read(rbuf);

        for (i = 0; i < len; i++) {
            if (isLetterOrDigit((char) rbuf[i]))
                mText.append((char) rbuf[i]);
        }

        if (len > 0) {
            LEDs = (char) rbuf[len - 1];
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                int i,len;
                char LEDs = 0x00;
                final StringBuilder mText = new StringBuilder();
                byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte
                while (read) {

                    // [FTDriver] Read from USB Serial
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
                        // doccumentation.
                        updateLEDs(LEDs);
                    }
                }
            }
        }).start();
        // TODO: Check thread exit, ensure that the thread never gets left running

    }

    public void onEndReadClick(View view) {
        read = false;
        btnEnd.setEnabled(true);
    }


    public void onWriteClick(View view) {
        // Serial print string from text box

        String wbuf = sendMessage.getText().toString();
        Toast.makeText(this, "Sending: " + wbuf, Toast.LENGTH_SHORT).show();

        // [FTDriver] Wirte to USB Serial
        mSerial.write(wbuf.getBytes());
        mSerial.write("\n");
    }


    public void onEndClick(View view) {
        // [FTDriver] Close USB Serial
        mSerial.end();

        btnBegin.setEnabled(true);
        btnRead.setEnabled(false);
        btnEndRead.setEnabled(false);
        btnWrite.setEnabled(false);
        btnEnd.setEnabled(false);

        Toast.makeText(this, "disconnect", Toast.LENGTH_SHORT).show();
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
}

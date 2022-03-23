package com.example.bt_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zerokol.views.joystickView.JoystickView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import java.lang.*;

public class Doggo extends AppCompatActivity{
    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    //converted using angle, between -1 and 1
    private int sentCount=0;
    private double x;
    private double y;
    private double Power;
    // Importing also other views
    private JoystickView joystick;

    //bluetooth variables
    private final String DEVICE_ADDRESS = "00:14:03:05:09:8F";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String Id;
    Button forwardButton, backwardButton, jumpButton, sitButton, backButton, connectButton;
//    TextView textView;
//    EditText editText;
//    TextView textConn;
    boolean deviceConnected = false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    private String mode;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doggo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Bundle bundle = getIntent().getExtras();
        Id = bundle.getString("id");

        forwardButton = (Button) findViewById(R.id.buttonForward);
        backwardButton = (Button) findViewById(R.id.buttonBackward);
        jumpButton = (Button) findViewById(R.id.buttonJump);
        sitButton = (Button) findViewById(R.id.buttonSit);
        backButton = (Button) findViewById(R.id.doggoBack);
        connectButton = (Button) findViewById(R.id.connectBT);
//        textView = (TextView) findViewById(R.id.textView);
//        textConn = (TextView) findViewById(R.id.textConn);

        forwardButton.setEnabled(true);
        backwardButton.setEnabled(true);
        jumpButton.setEnabled(true);
        sitButton.setEnabled(true);
        backButton.setEnabled(true);
//        textView.setEnabled(true);

//        angleTextView = (TextView) findViewById(R.id.angleTextView);
//        powerTextView = (TextView) findViewById(R.id.powerTextView);
//        directionTextView = (TextView) findViewById(R.id.directionTextView);
        //Referencing also other views
        joystick = (JoystickView) findViewById(R.id.joystickView);

//        joystick.setFixedCenter(false);
        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener(){

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub
                double radians = Math.toRadians(Double.valueOf(angle));
                x = ((power/100.0)*Math.sin(radians))+1;
                y = ((power/100.0)*Math.cos(radians))+1;
                Power = power;
//                angleTextView.setText(" " + String.valueOf(angle) + "°");
//                powerTextView.setText(" " + String.valueOf(power) + "%");

                mode = "J";
                sendData();
//                switch (direction) {
//                    case JoystickView.FRONT:
//                        directionTextView.setText(R.string.front_lab);
//                        break;
//                    case JoystickView.FRONT_RIGHT:
//                        directionTextView.setText(R.string.front_right_lab);
//                        break;
//                    case JoystickView.RIGHT:
//                        directionTextView.setText(R.string.right_lab);
//                        break;
//                    case JoystickView.RIGHT_BOTTOM:
//                        directionTextView.setText(R.string.right_bottom_lab);
//                        break;
//                    case JoystickView.BOTTOM:
//                        directionTextView.setText(R.string.bottom_lab);
//                        break;
//                    case JoystickView.BOTTOM_LEFT:
//                        directionTextView.setText(R.string.bottom_left_lab);
//                        break;
//                    case JoystickView.LEFT:
//                        directionTextView.setText(R.string.left_lab);
//                        break;
//                    case JoystickView.LEFT_FRONT:
//                        directionTextView.setText(R.string.left_front_lab);
//                        break;
//                    default:
//                        directionTextView.setText(R.string.center_lab);
//                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        connectButton.setOnClickListener(view -> {
            onClickConnect();
        });

        forwardButton.setOnClickListener(view -> {
            mode = "F";
            sendData();
        });

        backwardButton.setOnClickListener(view -> {
            mode = "B";
            sendData();
        });

        jumpButton.setOnClickListener(view -> {
            mode = "U";
            sendData();
        });

        sitButton.setOnClickListener(view -> {
            mode = "S";
            sendData();
        });

        backButton.setOnClickListener(view -> {
            if(deviceConnected){
                stopThread = true;
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                deviceConnected=false;
                Toast.makeText(getApplicationContext(), "\nConnection Closed!\n", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent( Doggo.this, MainActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivity( intent );
        });

    }

    public void sendData(){
        checkBT();
        new CountDownTimer(5, 5) {
            public void onFinish() {
                String _x = String.format(Locale.getDefault(),"%.5f",x);
                String _y = String.format(Locale.getDefault(),"%.5f",y);
                double _Power = Power/10;
                String _power = String.format(Locale.getDefault(),"%.5f",_Power);
                String string = mode+" "+_x + " " + _y + " " + _power +"!\0";
                try {
                    outputStream.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();
    }

//    public void clearScreen(){
//        sentCount++;
//        if(sentCount>=2){
//            textView.setText("");
//            sentCount=0;
//        }
//    }


    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(getDeviceId())) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void checkBT() {
        if(!deviceConnected){
            if(BTinit())
            {
                if(BTconnect())
                {
                    deviceConnected=true;
//                    Toast.makeText(getApplicationContext(), "\nConnection Opened!\n", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void onClickConnect(){
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                    Toast.makeText(getApplicationContext(), "\nConnection Opened!\n", Toast.LENGTH_SHORT).show();
            }

        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
//                                    textView.append(string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public  String getDeviceId(){
        return Id;
    }

//    public void onClickSend(View view) {
//        String string = editText.getText().toString();
//        try {
//            outputStream.write(string.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        textView.append("\nSent Data:"+string+"\n");
//
//    }

//    public void onClickStop(View view) throws IOException {
//        stopThread = true;
//        outputStream.close();
//        inputStream.close();
//        socket.close();
////        setUiEnabled(false);
//        deviceConnected=false;
//        Toast.makeText(getApplicationContext(), "\nConnection Closed!\n", Toast.LENGTH_SHORT).show();
//    }

//    public void onClickClear(View view) {
//        textView.setText("");
//    }
}
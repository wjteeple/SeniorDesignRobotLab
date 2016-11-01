package arduinotoandroid.arduinotoandroidcode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity implements OnClickListener {

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket socket = null;
    private OutputStream outStream = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //robot bluetooth address
    private static String address = "98:76:B6:00:64:90";

    @Bind(R.id.verticalSeekbarSpeed)
    VerticalSeekBar mSpeedSeekbar;

    @Bind(R.id.turnSeekbar)
    SeekBar mTurnSeekbar;

    @Bind(R.id.seekbarLeftNumber)
    TextView mLeftSliderText;
    @Bind(R.id.seekbarRightNumber)
    TextView mRightSliderText;

    @Bind(R.id.connect)
    Button connect;
    @Bind(R.id.stop)
    Button stop;

    @Bind(R.id.connectedStatus)
    TextView connectedStatus;

    int wheelSpeed = 0;

    int leftTurn = 0;
    int rightTurn = 0;

    int leftSpeedTotal = 0;
    int rightSpeedTotal = 0;

    int maxSpeed = 225; //max forward/backward speed (no turning)
    int increment = 15; //15 forward/reverse speeds
    int numIncrements = maxSpeed/increment;

    int maxTurn = 30; //max turn increase
    int turnIncrement = 2; //15 left/right increments
    int numTurnIncrements = maxTurn/turnIncrement; //number of turn increments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setListeners();
        CheckBtIsOn();
    }

    @Override
    public void onClick(View control) {
        switch (control.getId()) {
            case R.id.connect:
                Connect();
                connectedStatus.setText(R.string.connection_connected);
                break;
            case R.id.stop: {
                writeData("0 0 x");
            }
        }
    }

    private void CheckBtIsOn() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth Disabled!",
                    Toast.LENGTH_SHORT).show();

            //start activity to request bluetooth to be enabled
            Intent enableBtIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void Connect() {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        mBluetoothAdapter.cancelDiscovery();

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                Log.d("ERROR", "Socket can't close.");
            }
            connectedStatus.setText(R.string.connection_disconnected);
            Log.d("ERROR", "Socket can;t open.");
        }
    }

    private void writeData(String data) {
        try {
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("ERROR", "Error before sending.");
        }

        String message = data;
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Log.d("ERROR", "Error during sending.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public void setListeners()
    {
        connect.setOnClickListener(this);
        stop.setOnClickListener(this);

        mSpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //convert current progress to speed
                progress -= numIncrements;
                progress *= increment;

                //set move speed of both wheels
                wheelSpeed = progress;

                //determine total speed of both wheels
                leftSpeedTotal = wheelSpeed + leftTurn;
                rightSpeedTotal = wheelSpeed + rightTurn;

                //send data via bluetooth
                writeData(Integer.toString(leftSpeedTotal) + " " + Integer.toString(rightSpeedTotal) + " x");

                //display values to user
                mRightSliderText.setText(String.valueOf(progress));
            }
        });

        mTurnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //convert current progress to speed
                progress -= numTurnIncrements;
                progress *= turnIncrement;

                //check if progress is to the left or right of center
                if (progress > 0) //to the left of center, turn left
                {
                    rightTurn = 0;
                    leftTurn = progress;
                }
                else //to the right of center, turn right
                {
                    rightTurn = (-1)*progress;
                    leftTurn = 0;
                }

                //check to see if wheel speed is forward or backward
                if (wheelSpeed >= 0)
                {
                    //set total speeds
                    leftSpeedTotal = wheelSpeed + leftTurn;
                    rightSpeedTotal = wheelSpeed + rightTurn;
                }
                else
                {
                    leftSpeedTotal = wheelSpeed - leftTurn;
                    rightSpeedTotal = wheelSpeed - rightTurn;
                }

                //send data via bluetooth
                writeData(Integer.toString(leftSpeedTotal) + " " + Integer.toString(rightSpeedTotal) + " x");

                //display values to user
                mLeftSliderText.setText(String.valueOf(progress));
            }

        });
    }
}
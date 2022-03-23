package com.example.bluetoothaplikacija;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<String> stringArrayList=new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket btsocket = null;
    SendRecieve sendRecieve;
    BluetoothDevice[] btArray;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btntOn = (Button)findViewById(R.id.btnOn);
        Button btntOff = (Button)findViewById(R.id.btnOFF);
        Button btnsend = (Button)findViewById(R.id.btnsendtxt);
        final BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        Button scanbutton = (Button)findViewById(R.id.btnDiscover);
        ListView scanlistview = (ListView)findViewById(R.id.lista);
        EditText poruka = (EditText)findViewById(R.id.poruka);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);


        btntOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bAdapter == null)
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth nije podržan",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(!bAdapter.isEnabled()){
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1);
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 1);
                        Toast.makeText(getApplicationContext(),"Bluetooth uključen",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btntOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bAdapter.disable();
                Toast.makeText(getApplicationContext(),"Bluetooth isključen", Toast.LENGTH_SHORT).show();
            }
        });

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number

        scanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt = bAdapter.getBondedDevices();
                btArray = new BluetoothDevice[bt.size()];
                String[] strings = new String[bt.size()];
                int index = 0;

                if(!bAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(), "Uključite Bluetooth",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(bt.size() > 0){
                        for(BluetoothDevice device:bt){
                            btArray[index] = device;
                            strings[index] = (device.getName() + "\n" + device.getAddress());
                            index++;
                        }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,strings);
                    scanlistview.setAdapter(arrayAdapter);
                    }
                }
            }
        });

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = String.valueOf(poruka.getText());
                sendRecieve.write(message.getBytes());
            }
        });

        scanlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();
            }
        });
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1){
            device = device1;
            try{
                socket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch(IOException e){
                e.printStackTrace();
            }

            try{

                socket.connect();
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();
                Toast.makeText(getApplicationContext(), "povezano, valjda",Toast.LENGTH_SHORT).show();

            }
            catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "nije, valjda",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SendRecieve extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final OutputStream outputStream;
        private final InputStream inputStream;

        public SendRecieve(BluetoothSocket socket){
            this.bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try{
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            outputStream = tempOut;
            inputStream = tempIn;
        }

        public void write(byte[] bytes){
            try{
                outputStream.write(bytes);
                Toast.makeText(getApplicationContext(), "šaljem",Toast.LENGTH_SHORT).show();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            }
        }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    }

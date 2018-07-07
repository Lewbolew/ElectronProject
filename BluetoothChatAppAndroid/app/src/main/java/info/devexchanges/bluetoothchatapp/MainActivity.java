package info.devexchanges.bluetoothchatapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MainActivity extends AppCompatActivity {
    private View button;
    private View textView;

    private TextView status;
    private TextView strokesTextArea;
    private TextView serverResponse;
    private Button btnConnect;

    private Dialog dialog;
    private TextInputLayout inputLayout;

    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private BluetoothAdapter bluetoothAdapter;

    public ArrayList<Long> ultrasonicMaxDistances = new ArrayList<>();
    public ArrayList<Long> laserMaxDistances = new ArrayList<>();
    public ArrayList<Long> redMaxDistances = new ArrayList<>();
    public Long maxLength = new Long(0);
    public Long strikesNumber = new Long(0);
    public int DISTANCE_DELTA = 3;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;

    EditText textmsg;
    static final int READ_BLOCK_SIZE = 100;

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds();

//        textmsg=(EditText)findViewById(R.id.editText1);
        // todo uncoment the button with writting to file
        button = findViewById(R.id.button_id);
        textView = findViewById(R.id.strokes_text);
        //check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });

        //set chat adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                JSONObject json = new JSONObject();
                try {
                    json.put("distance", laserMaxDistances.toString()); // // TODO: 2/6/18 Number of strokes
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, "http://electron.pythonanywhere.com/electron/add_distance/", json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        serverResponse.setText(response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(sr);
            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @TargetApi(Build.VERSION_CODES.ECLAIR)
        @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;

                case MESSAGE_READ:

                        String data = (String) msg.obj;
                        //---------------------DEMO------------------------
//                        Long strNum = Long.parseLong(data);
//                        algoMAX(strNum);
//                        strokesArr.add(strNum);
//                        serverResponse.setText(data.split(":")[1]);
                        String[] parsed_data = data.split(":");
                        if(parsed_data[0].equals("L")) {
                            laserMaxDistances.add(Long.parseLong(parsed_data[1]));
                        } else if(parsed_data[0].equals("U")) {
                            ultrasonicMaxDistances.add(Long.parseLong(parsed_data[1]));
                        } else {
                            redMaxDistances.add(Long.parseLong(parsed_data[1]));
                        }
                        algoMAX(Long.parseLong(parsed_data[1]));
                        strokesTextArea.setText(strikesNumber.toString());
                        serverResponse.setText(ultrasonicMaxDistances.toString());
                    //----------------------END------------------------

//                        String laserSensor = data.split("[\\(\\)]")[1];
//                        String infraRed = data.split("[\\[\\]]")[1];
//                        Log.e("dsfklsdfsdfasd", "sdfsdfsdfsdfsdf");
//                        String ultraSonic = data.split("[\\{\\}]")[1];
//                        String timeStamp = data.split("[\\<\\>]")[1];
//                        ArrayList<String> finalData = new ArrayList<>();
//                        finalData.add(laserSensor);
//                        finalData.add(infraRed);
//                        finalData.add(ultraSonic);
//                        finalData.add(timeStamp);
//                        sentDataToServer(finalData);
//                        strokesTextArea.setText(strikesNumber.toString());
//                        serverResponse.setText(finalData.toString());
                        chatMessages.add(connectingDevice.getName() + ":  " + data);
                        chatAdapter.notifyDataSetChanged();

                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void algoMAX(Long gotMaxLength) {
        if(this.maxLength < gotMaxLength) {
            this.strikesNumber = new Long(1);
            this.maxLength = gotMaxLength;
        } else if (Math.abs(maxLength - gotMaxLength) <= DISTANCE_DELTA){
            this.strikesNumber ++;
        }
    }

    private String parseSensorName(String rawData) {
        return rawData.split(".")[0];
    }
    private void setStatus(String s) {
        /*

        Set status wether connected to,
         */
        status.setText(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds() {
        status = (TextView) findViewById(R.id.status);
        strokesTextArea = (TextView) findViewById(R.id.strokes_text);
        btnConnect = (Button) findViewById(R.id.btn_connect);
        serverResponse = (TextView) findViewById(R.id.server_response);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*

        Turn of the application when the bluetooth OFF.

         */
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onStart() {
        /*

        System method.

        */
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        /*

        System method.

        */
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        /*

        System method.

        */
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };

    public void WriteBtn(View v) {
        // add-write text into file
        try {
            FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(textmsg.getText().toString());
            outputWriter.close();

            //display file saved message
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read text from file
    public void ReadBtn(View v) {
        //reading text from file
        try {
            FileInputStream fileIn=openFileInput("mytextfile.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sentDataToServer(ArrayList<String> dist_list) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JSONObject json = new JSONObject();
            try {
                json.put("distance", dist_list.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, "http://electron.pythonanywhere.com/electron/add_distance/", json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    serverResponse.setText(response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(sr);
    }
}
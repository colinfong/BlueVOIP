package com.example.chulheewoo.a117;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.File;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class MainActivity extends AppCompatActivity {

    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    Button play, stop, record, playRec;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.listView);
        /****************************/
        //Button assignments for audio playback
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        playRec = (Button) findViewById(R.id.playRec);


        stop.setEnabled(false);
        play.setEnabled(false);
        //Choose the destination for the audio recording in the SD card
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        //Assign record button click action
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*create MediaRecorder to declare audio source, output format, and audio encoding*/
                myAudioRecorder = new MediaRecorder();
                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                myAudioRecorder.setOutputFile(outputFile);
                try {
                    //Prepare recording device by getting its resources
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //record.setEnabled(false);
                stop.setEnabled(true);

                //Notify user of recording
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        //Declare stop button function
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                //releases the audio recording resources
                myAudioRecorder.release();
                myAudioRecorder = null;

                //Disallow stop from being clicked again
                stop.setEnabled(false);
                //Enable the playback of the recorded audio
                play.setEnabled(true);

                //Announce the reodring of the audio
                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
            }
        });

        //Assign action to play button
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {
                //Create a media player
                        MediaPlayer m = new MediaPlayer();

                //Try to play the recorded file
                try {
                    m.setDataSource(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });

        playRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {
                //Create a media player
                MediaPlayer m = new MediaPlayer();

                //Try to play the recorded file
                try {
                    boolean found = true;
                    String ourFile = null;
                    String recFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/recording";
                    int a = 1;
                    while(found) {
                            File file = new File(recFile.concat("-" + a + ".3gp"));
                            if(file.exists()) {
                                ourFile = recFile.concat("-" + a + ".3gp");
                                a++;
                            } else {
                                found = false;
                            }
                    }
                    if(ourFile == null) {
                        ourFile = outputFile;
                    }
                    m.setDataSource(ourFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /*******************************************************************************************************/

    // FILE SEND OVER BLUETOOTH

    public void sendViaBluetooth(View v) {

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
        } else {
            enableBluetooth();
        }
    }

    public void enableBluetooth() {

        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);

        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {

            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String myFile = "download.jpg";
            //String path= baseDir + "/Download/" + myFile;
            //String path = "file:///storage/sdcard0/Download/" + myFile;
            String path = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
            //if(path.startsWith("file")||path.startsWith("content")||path.startsWith("FILE")||path.startsWith("CONTENT")){

            //}else{
            //    path="file://"+path;
            //}
            /*
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            shareIntent.setType("video/mp4");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.sendTo)));
            */

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            //intent.setType("image/jpeg");
            intent.setType("audio/3gpp");
            //File f = new File(Environment.getExternalStorageDirectory(), "31286.txt");
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));

            PackageManager pm = getPackageManager();
            List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);

            if(appsList.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for(ResolveInfo info : appsList) {
                    packageName = info.activityInfo.packageName;
                    if(packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Bluetooth havn't been found",
                            Toast.LENGTH_LONG).show();
                } else {
                    intent.setClassName(packageName, className);
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, "Bluetooth is cancelled", Toast.LENGTH_LONG)
                    .show();
        }
    }

    /*************************************************************************************************/

    // BLUETOOTH CONNECTION AUX FXNS

    public void on(View v) {
        if (!BA.isEnabled()) { //Bluetooth adapter is not enabled
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show(); //Screen popup notification
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v) { //Disable bluetooth & notify user
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }

    public void visible(View v) { //Make device visible to other bluetooth devices
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void list(View v) { //List paired bluetooth devices (different from connected)
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
    }
    /*************************************************************************************************/

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.chulheewoo.a117/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.chulheewoo.a117/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


}
package com.navigation.vibration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

public class ConnectionActivity extends AppCompatActivity {

    private int noDevices;
    public String MessageToDisplay = "Connect both devices";

    private Button connLeft;
    private Button connRight;
    private Button next;

    private boolean isLeftConnected ;
    private boolean isRightConnected;

    public static final String NO_DEVICES = "NO_DEVICES";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);


      //  List<Node> connectedNodes =
      //         Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(NoDevicesSelectionActivity.NO_DEVICES);
        try{
            noDevices = Integer.parseInt(message);
            if (noDevices == 1)
               MessageToDisplay = "Select device to connect";
        }
        catch (Exception e)
        { }

        TextView editText = findViewById(R.id.textView);
        editText.setText(MessageToDisplay);

        initButtons();
    }


    public void connectLeft(View view) {
        // TO DO : handle the connection
        connLeft.setBackgroundColor(getResources().getColor(R.color.button_green));

        isLeftConnected = true;
        enableNext();
    }

    public void connectRight(View view) {
        // TO DO : handle the connection


        connRight.setBackgroundColor(getResources().getColor(R.color.button_green));
        isRightConnected = true;
        enableNext();
    }

    public void goToNextActivity(View view){
        Intent intent = new Intent(this, PatternSelectionActivity.class);
        intent.putExtra(NO_DEVICES, "" + noDevices);
        startActivity(intent);

    }

    private void enableNext(){
        if (noDevices == 1){
            if  (isLeftConnected || isRightConnected)
                 next.setEnabled(true);
            connLeft.setEnabled(false);
            connRight.setEnabled(false);
        }

        if (noDevices == 2 &&isRightConnected && isRightConnected)
            next.setEnabled(true);
    }
    //
    private void initButtons(){
        connLeft =  findViewById(R.id.conn_left);
        connRight = findViewById(R.id.conn_right);
        next = findViewById(R.id.next);

        connLeft.setBackgroundColor(getResources().getColor(R.color.button_red));
        connRight.setBackgroundColor(getResources().getColor(R.color.button_red));

        next.setEnabled(false);
        
        connLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //something something
            }
        });

        connRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TEST BLUETOOTH CONNECITON


                //something something
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //something something
            }
        });
    }
}

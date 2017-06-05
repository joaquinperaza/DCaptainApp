package uy.peraza.dronecaptain;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import uy.peraza.dronecaptain.R;

import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;

import static java.lang.Math.random;

public class ConnectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ConnectionActivity.class.getName();

    private TextView mTextConnectionStatus;
    private TextView mTextProduct;
    private Button mBtnOpen;
    boolean firebased = false;


    String nowtoken = "notok";
    String uuid = UUID.randomUUID().toString();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("token");
    Map n = ServerValue.TIMESTAMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_connection);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);



        String idtoken = preferences.getString("idtoken", "");
        String lasttoken = preferences.getString("atoken", uuid);


        ////retrievo now token
        DatabaseReference userdb = database.getReference("userdata/"+idtoken+"/token");
        userdb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    nowtoken = dataSnapshot.getValue().toString();
                } catch (Exception e){
                    nowtoken=uuid;
                }


                auth();
                Log.e("servervalue",n.toString());


            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });




        ////end nowtoken retrieve
        initUI();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initUI() {

        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        mTextProduct = (TextView) findViewById(R.id.text_product_info);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnOpen.setOnClickListener(this);
        mBtnOpen.setEnabled(false);

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = DJIDemoApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected() && firebased==true) {
            Log.v(TAG, "refreshSDK: True");

            mBtnOpen.setEnabled(true);

            String str = mProduct instanceof Aircraft ? "DJIAircraft" : "DJIHandHeld";
            mTextConnectionStatus.setText("Status: " + str + " connected");

            if (null != mProduct.getModel()) {

                mTextProduct.setText("" + mProduct.getModel().getDisplayName());
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("model", mProduct.getModel().getDisplayName());
                editor.apply();


            } else {
                mTextProduct.setText(R.string.product_information);
            }

        } else {
            Log.v(TAG, "refreshSDK: False");
            mBtnOpen.setEnabled(false);

            mTextProduct.setText(R.string.product_information);
            mTextConnectionStatus.setText(R.string.connection_loose);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_open: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    public void auth() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String idtoken = preferences.getString("idtoken", "");
        String lasttoken = preferences.getString("atoken", uuid);
        final TextView tkDisplay = (TextView) findViewById(R.id.tkDisplay);
        final TextView textsmall = (TextView) findViewById(R.id.textid);

        if (!idtoken.equalsIgnoreCase("") && nowtoken.equalsIgnoreCase(lasttoken) ) {

            tkDisplay.setText("Connected to DroneCaptain!");
            firebased=true;
            refreshSDKRelativeUI();
            textsmall.setText("");







        } else {
            Log.e("now",nowtoken);
            Log.e("old",lasttoken);
            Log.e("tk",idtoken);
            // Wait a message from the database

            auth2();
        }}



    public void auth2(){
        final TextView tkDisplay = (TextView) findViewById(R.id.tkDisplay);
        final TextView textsmall = (TextView) findViewById(R.id.textid);
        Double m = 10000 + random() * 90000;
        final Integer t = m.intValue();
        tkDisplay.setText(t.toString());
        textsmall.setText("Drone ID:");
        final Context contexto = this;

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                DataSnapshot useraw = dataSnapshot.child(t.toString());

                Object value = useraw.child("userID").getValue();

                long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
                Query oldItems = myRef.orderByChild("age").endAt(cutoff);
                oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                            itemSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


                if (value != null) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(contexto);
                    String idtoken = value.toString();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference userdb = database.getReference("userdata/" + idtoken + "/token");
                    userdb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.

                            try {
                                nowtoken = dataSnapshot.getValue().toString();
                            } catch (Exception e){
                                nowtoken=uuid;
                                userdb.setValue(uuid);
                            }



                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                    tkDisplay.setText("Connected to DroneCaptain!");
                    firebased=true;
                    refreshSDKRelativeUI();
                    textsmall.setText("");


                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("idtoken", value.toString());
                    editor.putString("atoken", nowtoken);
                    editor.apply();






                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}

package com.ganyo.pushtest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import static com.ganyo.pushtest.Util.*;
import static com.ganyo.pushtest.Util.TAG;

public class PushMainActivity extends Activity {

  private TextView messageTextView;
  private Button sendButton;
  private AlertDialogManager alert = new AlertDialogManager();

  public static String registrationId = "";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // this is a hack to force AsyncTask to be initialized on main thread. Without this things
    // won't work correctly on older versions of Android (2.2, apilevel=8)
    try {
    	Class.forName("android.os.AsyncTask");
    } catch (Exception ignored) {}
    
    GCMRegistrar.checkDevice(this);
    GCMRegistrar.checkManifest(this);

    initUI();

    AppServices.loginAndRegisterForPush(this);
  }

  private void initUI() {
    setContentView(R.layout.main);
    messageTextView = (TextView)findViewById(R.id.lblMessage);

   sendButton = (Button)findViewById(R.id.sendButton);
   sendButton.setVisibility(View.INVISIBLE);
    sendButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
           	  
    	 Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
   	     sharingIntent.setType("text/plain");
   	     sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "registrationId:");
   	     sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, registrationId);
   	     startActivity(Intent.createChooser(sharingIntent, "Share via"));
   	     	  
      }
    });

    registerReceiver(notificationReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
  }

  /**
   * Receives push Notifications
   * */
  private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {

      // Waking up mobile if it is sleeping
      WakeLocker.acquire(getApplicationContext());

      /**
       * Take some action upon receiving a push notification here!
       **/
      String message = intent.getExtras().getString(EXTRA_MESSAGE);
      if (message == null) { message = "Empty Message"; }
      if (message.contains("DeviceId:"))
      {
    	  Log.i(TAG, message);
          messageTextView.append("\n" + message);

          registrationId = message.substring("DeviceId:".length());
          
          sendButton.setVisibility(View.VISIBLE);
          
         // alert.showAlertDialog(context, getString(R.string.gcm_alert_title), message);
         // Toast.makeText(getApplicationContext(), getString(R.string.gcm_message, message), Toast.LENGTH_LONG).show();
      }   

      WakeLocker.release();
    }
  };

  // this will be called when the screen rotates instead of onCreate()
  // due to manifest setting, see: android:configChanges
  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
    initUI();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(notificationReceiver);
  }

}

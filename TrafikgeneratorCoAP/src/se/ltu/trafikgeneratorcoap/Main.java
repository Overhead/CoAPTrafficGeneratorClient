package se.ltu.trafikgeneratorcoap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.jnetpcap.util.Length;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends AbstractActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			Log.e("Main", "Could not grant SU.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void sendData(View view) {
		Intent intent = new Intent(this, InputData.class);
	    intent.putExtra("ResultType", ResultType.SEND_DATA.ordinal());
		startActivityForResult(intent, ResultType.SEND_DATA.ordinal());
	}
	
	public void recieveData(View view) {
		Intent intent = new Intent(this, InputData.class);
	    intent.putExtra("ResultType", ResultType.RECEIVE_DATA.ordinal());
		startActivityForResult(intent, ResultType.RECEIVE_DATA.ordinal());
	}
	
	public void installTCPDump(View view) {
		Intent intent = new Intent(this, AndroidExplorer.class);
		startActivityForResult(intent, ResultType.LOAD_FILE.ordinal());
	}
	
	public void uninstallTCPDump(View view) {
		if ((new File("/data/local/tcpdump-coap")).exists()) {
			try {
				Runtime.getRuntime().exec("su ; rm /data/local/tcpdump-coap");
				Log.d("Main", "tcpdump was uninstalled.");
			} catch (IOException e) {
				Log.d("Main", "An error occurred, and tcpdump was not uninstalled.");
			}
		}
		else
			Log.d("Main", "Uninstallation unnecessary; tcpdump was not installed.");
	}
	
	public void checkSU(View view) {
		//TODO: rename stuff. Not "Check SU", but rather "setup script"?
		File scriptDirectory = new File(new File(Environment.getExternalStorageDirectory(), "trafikgeneratorcoap"), "scripting");
		scriptDirectory.mkdirs();
		File scriptFile = new File(scriptDirectory, "SaveMyPID.sh");
		if (scriptFile.exists())
			scriptFile.delete();
		String script = "eval \"$2 &\"\necho $! > " + (new File(scriptDirectory, "$1")).toString() + ".pid\n";
		try {
			PrintWriter out = new PrintWriter(scriptFile);
			out.print(script);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void exit(View view){
        finish();
        System.exit(0);
	}
	
	private void install(String string) {
		if ((new File("/data/local/tcpdump")).exists() || (new File("/data/local/tcpdump-coap")).exists())
			Log.d("Main", "tcpdump already installed.");
		else {
			try {
				Runtime.getRuntime().exec("su ; cp " + string + " /data/local/tcpdump-coap ; chmod 555 /data/local/tcpdump-coap");
				Log.d("Main", "tcpdump installed.");
			} catch (IOException e) {
				Log.d("Main", "An error occurred, and tcpdump was not installed.");
			}
		}
	}
		
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if(requestCode == ResultType.LOAD_FILE.ordinal())
    	{
    		if(resultCode == RESULT_OK)
    		{
    			install(data.getStringExtra("path"));
    		}
    	}
    	if(requestCode == ResultType.SEND_DATA.ordinal())
    	{
    		if(resultCode == RESULT_OK)
    		{
    			Log.d("Main", "Sending done!");
    			//BuildNotification("Sending to Server","Sending done!" );
    			finish();
    			System.exit(0);
                /*final Dialog dialog = new Dialog((Context)this);
 
                dialog.setContentView(R.layout.dialog_ok);
                dialog.setTitle("Sending to Server");
 
                TextView txt = (TextView) dialog.findViewById(R.id.txt);
 
                //xxx change this to something useful
                txt.setText("Sending done!");
                	
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
 
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();*/
    		}
    		if(resultCode == RESULT_CANCELED)
    		{
    			Log.d("Main", "Task Canceled!");
    		}
    	}
    	
    	if(requestCode == ResultType.RECEIVE_DATA.ordinal())
    	{
    		if(resultCode == RESULT_OK)
    		{
    			Log.d("Main", "Receiving done!");
                final Dialog dialog = new Dialog((Context)this);
 
                dialog.setContentView(R.layout.dialog_ok);
                dialog.setTitle("Receiving from Server");
 
                TextView txt = (TextView) dialog.findViewById(R.id.txt);
 
                //xxx change this to something useful
                txt.setText("Receiving done!");
                	
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
 
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
    		}
    		if(resultCode == RESULT_CANCELED)
    		{
    			Log.d("Main", "Task Canceled!");
    		}
    	}
    }
    
    private void BuildNotification(String title, String msg){
    	Intent intent = new Intent(this, Main.class);
    	PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

    	// build notification
    	// the addAction re-use the same intent to keep the example short
    	Notification n  = new Notification.Builder(this)
    	        .setContentTitle(title)
    	        .setContentText(msg)
    	        .setContentIntent(pIntent).build();
    	    
    	  
    	NotificationManager notificationManager = 
    	  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    	notificationManager.notify(0, n); 
    }
}


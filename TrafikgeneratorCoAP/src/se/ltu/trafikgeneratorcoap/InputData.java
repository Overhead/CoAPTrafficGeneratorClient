package se.ltu.trafikgeneratorcoap;

import android.os.Bundle;
import android.app.ActionBar;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import java.net.Inet4Address;
import java.util.ArrayList;

import se.ltu.trafikgeneratorcoap.R;

public class InputData extends AbstractActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_input);
		Intent type = getIntent();
		thisResultType = type.getIntExtra("ResultType", -1);		
		Intent intent = new Intent(this, AndroidExplorer.class);
		startActivityForResult(intent, ResultType.LOAD_FILE.ordinal());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar act = getActionBar();
		act.setDisplayShowHomeEnabled(false);
		act.setDisplayShowTitleEnabled(false);
		getMenuInflater().inflate(R.menu.input, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("InputData", "Options!");
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.AddFile:
	            addFile();
	            return true;
	        case R.id.RemoveFile:
	        	removeFile();
	            return true;
	        case R.id.Continue:
	        	next();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private int totalConfigs = 0;
	
	private int thisResultType;
	
	private ArrayList<String> fileName = 	new ArrayList<String>();
	private ArrayList<String> filePath = 	new ArrayList<String>();
	private ArrayList<String> ip = 			new ArrayList<String>();
	private ArrayList<String> timeout = 	new ArrayList<String>();
	private ArrayList<String> retransmit = 	new ArrayList<String>();		
	private ArrayList<String> nStart = 		new ArrayList<String>();
	private ArrayList<String> payloadSize = new ArrayList<String>();
	private ArrayList<String> port = 		new ArrayList<String>();
	private ArrayList<String> seconds = 	new ArrayList<String>();
	private ArrayList<String> sleep = 		new ArrayList<String>();
	private ArrayList<String> random = 		new ArrayList<String>();
	
	private void next(){
		Log.d("InputData", "Next");
		if(addFieldsToLists()){
		    Intent intent = new Intent(this, HandleData.class);
		    intent.putStringArrayListExtra("timeout", timeout);
		    intent.putStringArrayListExtra("random", random);
		    intent.putStringArrayListExtra("retransmit", retransmit);
		    intent.putStringArrayListExtra("nStart", nStart);
		    intent.putStringArrayListExtra("payloadSize", payloadSize);
		    intent.putStringArrayListExtra("filePath", filePath);
		    intent.putStringArrayListExtra("port", port);
			intent.putStringArrayListExtra("seconds", seconds);
		    intent.putStringArrayListExtra("ip", ip);
		    intent.putStringArrayListExtra("sleep", sleep);
		    intent.putExtra("totalConfigs", totalConfigs);
		    intent.putExtra("ResultType", thisResultType);
		    startActivityForResult(intent, thisResultType);
		}
		else if(filePath.isEmpty()){
			TextView infoField = (TextView) findViewById(R.id.Error);
			infoField.setText("Add a file please!");
		}
	}
	
	private void removeFile(){
		Log.d("InputData", "RemoveFile");
		if(filePath.size() > 1){
			Log.d("InputData", fileName.get(fileName.size()-1) + " " + ip.size());
			TextView infoField = (TextView) findViewById(R.id.Error);
			infoField.setText("Removed #" + (totalConfigs) + ": "  + fileName.get(fileName.size()-1));
			fileName.remove((fileName.size()-1));
			filePath.remove((filePath.size()-1));
			ip.remove((ip.size()-1));
			timeout.remove((timeout.size()-1));
			retransmit.remove((retransmit.size()-1));		
			nStart.remove((nStart.size()-1));
			payloadSize.remove((payloadSize.size()-1));
			port.remove((port.size()-1));
			seconds.remove((seconds.size()-1));
			sleep.remove((sleep.size()-1));
			random.remove((random.size()-1));
			totalConfigs--;
		}
		else if(filePath.size() == 1){
			fileName.remove((fileName.size()-1));
			filePath.remove((filePath.size()-1));
			totalConfigs--;
			TextView infoField = (TextView) findViewById(R.id.Error);
			infoField.setText("Add a file please!");
		}
	}
	
	private void addFile() {
		Log.d("InputData", "AddFile");
		
		if (addFieldsToLists()) 
		{
			Intent intent = new Intent(this, AndroidExplorer.class);
			startActivityForResult(intent, ResultType.LOAD_FILE.ordinal());
		}
	}
	
	private boolean addFieldsToLists(){
		Log.d("InputData", "AddFields");
		EditText portField = (EditText) findViewById(R.id.Port);
		String portString = portField.getText().toString();
		port.add(portString);
	    
		EditText timeField = (EditText) findViewById(R.id.Time);
		String timeString = timeField.getText().toString();
		seconds.add(timeString);
		
		EditText timeoutField = (EditText) findViewById(R.id.timeout);
		String timeoutString = timeoutField.getText().toString();
		timeout.add(timeoutString);
		
		EditText randomField = (EditText) findViewById(R.id.random);
		String randomString = randomField.getText().toString();
		random.add(randomString);
		
		EditText retransmittField = (EditText) findViewById(R.id.retransmitt);
		String retransmittString = retransmittField.getText().toString();
		retransmit.add(retransmittString);
		
		EditText nstartField = (EditText) findViewById(R.id.nStart);
		String nstartString = nstartField.getText().toString();
		nStart.add(nstartString);
		
		EditText payloadsizeField = (EditText) findViewById(R.id.payloadSize);
		String payloadsizeString = payloadsizeField.getText().toString();
		payloadSize.add(payloadsizeString);
		
		EditText sleepField = (EditText) findViewById(R.id.sleep);
		String sleepString = sleepField.getText().toString();
		sleep.add(sleepString);
	
		EditText ipField = (EditText) findViewById(R.id.IPAddress);
		String ipString = ipField.getText().toString();
		try {
			Inet4Address.getByName(ipString);
			ip.add(ipString);
			return true;
		} catch (Exception e) {
			TextView infoField = (TextView) findViewById(R.id.Error);
			infoField.setText("IP-Address not valid");
			return false;
		}
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == ResultType.LOAD_FILE.ordinal())
    	{
    		if(resultCode == RESULT_OK)
    		{
    			filePath.add(data.getStringExtra("path"));
    			fileName.add(data.getStringExtra("name"));
    			totalConfigs++;
    			TextView infoField = (TextView) findViewById(R.id.Error);
    			infoField.setText("Config file #" + (totalConfigs) + ": "  + fileName.get(fileName.size()-1));
    		}
    		if(resultCode == RESULT_CANCELED)
    		{
    			
    		}
    	}
    	if(requestCode == thisResultType)
    	{
    		if(resultCode == RESULT_OK)
    		{
    			setResult(RESULT_OK);
    			finish();
    		}
    		if(resultCode == RESULT_CANCELED)
    		{
    			setResult(RESULT_CANCELED);
    			finish();
    		}
    	}
    }
}


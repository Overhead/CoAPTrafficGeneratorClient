package se.ltu.trafikgeneratorcoap;

import java.util.ArrayList;

import se.ltu.trafikgeneratorcoap.testing.Tester;
import se.ltu.trafikgeneratorcoap.testing.Settings;
import se.ltu.trafikgeneratorcoap.testing.TrafficConfig;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

public class HandleData extends AbstractActivity {  
	  
	private ArrayList<String> filePath = 		new ArrayList<String>();
	private ArrayList<String> ip = 				new ArrayList<String>();
	private ArrayList<String> timeout = 		new ArrayList<String>();
	private ArrayList<String> retransmit = 		new ArrayList<String>();		
	private ArrayList<String> nStart = 			new ArrayList<String>();
	private ArrayList<String> payloadSize =	 	new ArrayList<String>();
	private ArrayList<String> port = 			new ArrayList<String>();
	private ArrayList<String> seconds = 		new ArrayList<String>();
	private ArrayList<String> sleep = 			new ArrayList<String>();
	private ArrayList<String> random = 			new ArrayList<String>();
	
    private ArrayList<TrafficConfig> config = 	new ArrayList<TrafficConfig>();
    
    private ProgressDialog progressDialog;  
	private Intent intent;
	private int totalConfigs;
    private int indexer = 0;
    private int progressbarUpdate = 0;
    
    private int thisResultType;
 
    @Override  
    public void onCreate(Bundle savedInstanceState)  
    {  
		Log.d("HandleData", "Task Started!");
        super.onCreate(savedInstanceState);  
        
        intent = getIntent();
        thisResultType =	intent.getIntExtra("ResultType", -1);
        timeout = 			intent.getStringArrayListExtra("timeout");
        retransmit = 		intent.getStringArrayListExtra("retransmit");
        nStart = 			intent.getStringArrayListExtra("nStart");
	    payloadSize = 		intent.getStringArrayListExtra("payloadSize");
	    port = 				intent.getStringArrayListExtra("port");
	    seconds = 			intent.getStringArrayListExtra("seconds");
        random = 			intent.getStringArrayListExtra("random");
	    filePath = 			intent.getStringArrayListExtra("filePath");
	    ip = 				intent.getStringArrayListExtra("ip");
	    sleep = 			intent.getStringArrayListExtra("sleep");
	    totalConfigs =		intent.getIntExtra("totalConfigs", 0);
	    
	    Log.d("HandleData", "Configs: " + totalConfigs);

	    nextTask(indexer);
    }
    
    private void nextTask(int taskIndex)
    {	    
    	String fp = filePath.get(taskIndex);
    	Log.d("HandleData", "Creating config from: " + fp);
    	config.add(new TrafficConfig(TrafficConfig.fileToString(fp)));
    	
	    if(!timeout.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.COAP_ACK_TIMEOUT, parseInt(timeout.get(taskIndex)));
	    if(!retransmit.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.COAP_MAX_RETRANSMIT, parseInt(retransmit.get(taskIndex)));
	    if(!nStart.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.COAP_NSTART, parseInt(nStart.get(taskIndex)));
	    if(!payloadSize.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.TRAFFIC_MESSAGESIZE, parseInt(payloadSize.get(taskIndex)));
	    if(!port.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.TEST_TESTPORT, parseInt(port.get(taskIndex)));
	    if(!seconds.get(taskIndex).equals(""))
	    	config.get(taskIndex).setIntegerSetting(Settings.TRAFFIC_MAXSENDTIME, parseInt(seconds.get(taskIndex)));	 
	    
	    if(!random.get(taskIndex).equals(""))
	    	config.get(taskIndex).setDecimalSetting(Settings.COAP_ACK_RANDOM_FACTOR, parseFloat(random.get(taskIndex)));
    	
	    if(!ip.get(taskIndex).equals(""))
	    	config.get(taskIndex).setStringSetting(Settings.TEST_SERVER, ip.get(taskIndex));
	    
	    new LoadViewTask().execute();
    }
    
    private Float parseFloat(String s)
    {
    	Log.d("parseFloat","parseFloat: " + s);
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return null;
		}
    }
    
    private Integer parseInt(String s)
    {
    	Log.d("parseInt","parseInt: " + s);
	    try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
    }
    
    private Long parseLong(String s)
    {
    	Log.d("parseLong","parseLong: " + s);
    	try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return (long) 1000;
		}
    }
    
    
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>  
    {  
    	private int processNumber;
    	
        @Override  
        protected void onPreExecute()  
        {  
        	this.processNumber = indexer++;
        	Log.d("HandleData", "Creating process nr: " + this.processNumber);
        	if(this.processNumber == 0)
        	{
	            progressDialog = new ProgressDialog(HandleData.this);  
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
	            progressDialog.setTitle("Working..."); 
	            progressDialog.setMessage(getMessage());  
	            progressDialog.setCancelable(true);
	            progressDialog.setCanceledOnTouchOutside(false);
	            progressDialog.setOnCancelListener(new OnCancelListener() {
	                @Override
	                public void onCancel(DialogInterface dialog) {
	                    cancel(true);
	                }
	            });
	            progressDialog.setIndeterminate(false);  
	            progressDialog.setMax(totalConfigs);   
	            progressDialog.setProgress(0);  
	            progressDialog.show();
        	}
        }  
  
        //The code to be executed in a background thread.  
        @Override  
        protected Void doInBackground(Void... params)  
        {   
    	    Log.d("HandleData", "IP: " + config.get(this.processNumber).getStringSetting(Settings.TEST_SERVER));
    	    pickType();
        	publishProgress(++progressbarUpdate);
        	Log.d("HandleData", "End of process nr : " + this.processNumber);
        	if(this.processNumber != (totalConfigs-1))
        	{
        		try { Thread.sleep(parseLong(sleep.get(this.processNumber))); } catch (InterruptedException e) {}
        		nextTask(this.processNumber + 1);
        	}
        	return null;
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... values)  
        {  
            //set the current progress of the progress dialog  
            progressDialog.setProgress(values[0]);  
        }  
        
        @Override
        protected void onCancelled() {
        	Log.d("HandleData", "Task Canceled!");
        	//Sending.abort(config[this.processNumber]);
            progressDialog.dismiss();
			setResult(RESULT_CANCELED);
			finish();
        }
  
        @Override  
        protected void onPostExecute(Void result)  
        {  
			if(progressbarUpdate == (totalConfigs))
			{
				new CountDownTimer((parseLong(seconds.get(0)) * 10), 1000) {

					 public void onTick(long millisUntilFinished) {
						 Log.d("HandleData", "seconds remaining: " + millisUntilFinished / 1000);
					 }

					 public void onFinish() {
						Log.d("HandleData", "Task Done!");
			            progressDialog.dismiss();
						setResult(RESULT_OK);
						finish();
					 }
					}.start();
	        	
			}
        } 
        
        private String getMessage(){
        	ResultType type = ResultType.values()[thisResultType];
        	switch(type) {
    	    	case SEND_DATA:
    	    		return "Sending data... Please wait.";
    	    	case RECEIVE_DATA:
    	    		return "Receiving data... Please wait.";
    	    	default:
    	    		return "Error";
        	}
        }
        
        private void pickType() {
        	ResultType type = ResultType.values()[thisResultType];
        	switch(type) {
		    	case SEND_DATA:
		    		Tester sndData = null;
					try {
						sndData = new Tester(config.get(this.processNumber), getApplicationContext());
						sndData.send();
					} catch (Exception e1) {
						Log.e("HandleData", "Something went terribly wrong in sendData!");
						e1.printStackTrace();
						try {
							sndData.abort();
						} catch (InterruptedException e) {
							e1.printStackTrace();
						}
					}
		        	break;
		    	case RECEIVE_DATA:
		    		Tester rcvData = null;
		        	try {
		        		rcvData = new Tester(config.get(this.processNumber), getApplicationContext());
		        		rcvData.receive();
					} catch (Exception e1) {
						Log.e("HandleData", "Something went terribly wrong in receiveData!");
						try {
							rcvData.abort();
						} catch (InterruptedException e) {
							e1.printStackTrace();
						}
					}
		        	break;
		    	default:
		    		break;
        	}
        }
    }  
}  

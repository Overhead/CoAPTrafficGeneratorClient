package se.ltu.trafikgeneratorcoap.testing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.util.Log;
import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;

public class SendTest {
	/*
	 * The send methods have rate limiting functionality through a kind of token bucket.
	 */
	private static int headersize = 59, maxpacketsize = 1500;//1024
	private static Random random = new Random();
	public static void run(TrafficConfig config) throws InterruptedException, IOException {	
		CoAPEndpoint dataEndpoint = new CoAPEndpoint(config.toNetworkConfig());
		dataEndpoint.start();
		for (int i = 1; i <= config.getIntegerSetting(Settings.TEST_REPEATS); i++) {
			if (config.getStringSetting(Settings.TRAFFIC_TYPE).equals("CONSTANT_SOURCE")) {
				if (config.getStringSetting(Settings.TRAFFIC_MODE).equals("TIME")) {			
					runTimeTest(config, dataEndpoint);
				}
				else if (config.getStringSetting(Settings.TRAFFIC_MODE).equals("MESSAGES")) {
					runMessageTest(config, dataEndpoint);
				}
				else if (config.getStringSetting(Settings.TRAFFIC_MODE).equals("FILETRANSFER")) {
					runFileTest(config, dataEndpoint);
				} else if (config.getStringSetting(Settings.TRAFFIC_MODE).equals("THREAD")) {
					dataEndpoint.stop();
					runMultipleThreads(config);
				}
			}
    		if (i < config.getIntegerSetting(Settings.TEST_REPEATS))
				Thread.sleep(Math.round(config.getDecimalSetting(Settings.TEST_INTERMISSION)));
		}
	}
	private static void runTimeTest(TrafficConfig config, CoAPEndpoint endpoint) {
		/*
		 * TODO: Solve the problem of unlimited rate leading to a time shift.
		 * I.e. when sending as many packages as possible without intermission,
		 * system time seems to pass slower than real time; a 10 second send
		 * can become a 15 second send.
		 */
		boolean unlimitedRate = true, bucketFull = true;
		int payloadsize = config.getIntegerSetting(Settings.TRAFFIC_MESSAGESIZE),
				rate = config.getIntegerSetting(Settings.TRAFFIC_RATE),
				packetsize = payloadsize+headersize;
		double sendtime = config.getDecimalSetting(Settings.TRAFFIC_MAXSENDTIME);
		long bucketFillDelay = 0;
		String testURI = String.format(Locale.ROOT, "coap://%1$s:%2$d/test", config.getStringSetting(Settings.TEST_SERVER), config.getIntegerSetting(Settings.TEST_TESTPORT));
		CoAP.Type type = config.getStringSetting(Settings.COAP_MESSAGETYPE).equals("CON")?CoAP.Type.CON:CoAP.Type.NON;
		
		if (packetsize > maxpacketsize)
			payloadsize = maxpacketsize-headersize;
		if (rate > 0 && payloadsize > 0 && headersize > 0) {
			unlimitedRate = false;
			bucketFillDelay = Math.round(1000d/(((double) rate)/((double) packetsize))) * 1000000;
		}
		
		long timeToStopTest = Math.round(sendtime * 1000000000) + System.nanoTime();
		long nextTimeToFillBucket = bucketFillDelay + System.nanoTime();
		
		while (System.nanoTime() < timeToStopTest) {
			if (unlimitedRate || bucketFull) {
				Request test = Request.newPost();
				test.setURI(testURI);
				test.setType(type);
				test.setPayload(PayloadGenerator.generateRandomData(random.nextLong(), payloadsize));
				test.send(endpoint);
				bucketFull = false;
				try {
					Thread.sleep(1); //TODO: figure out why absence of interpacket sleep time leads to infinite loop... 
					while (test.isConfirmable() && (test.isAcknowledged() || test.isTimedOut() || test.isCanceled() || test.isRejected()))
						Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!unlimitedRate && System.nanoTime() > nextTimeToFillBucket) {
				bucketFull = true;
				nextTimeToFillBucket += bucketFillDelay;
			}
		}
	}
	private static void runMessageTest(TrafficConfig config, CoAPEndpoint endpoint) {
		boolean unlimitedRate = true, bucketFull = true;
		int payloadsize = config.getIntegerSetting(Settings.TRAFFIC_MESSAGESIZE),
				rate = config.getIntegerSetting(Settings.TRAFFIC_RATE),
				packetsize = payloadsize+headersize,
				sentMessages = 0,
				maxMessages = config.getIntegerSetting(Settings.TRAFFIC_MAXMESSAGES);
		long bucketFillDelay = 0;
		String testURI = String.format(Locale.ROOT, "coap://%1$s:%2$d/test", config.getStringSetting(Settings.TEST_SERVER), config.getIntegerSetting(Settings.TEST_TESTPORT));
		CoAP.Type type = config.getStringSetting(Settings.COAP_MESSAGETYPE).equals("CON")?CoAP.Type.CON:CoAP.Type.NON;

		if (packetsize > maxpacketsize)
			payloadsize = maxpacketsize-headersize;
		if (rate > 0 && payloadsize > 0 && headersize > 0) {
			unlimitedRate = false;
			bucketFillDelay = Math.round(1000d/(((double) rate)/((double) packetsize))) * 1000000;
		}

		long nextTimeToFillBucket = bucketFillDelay + System.nanoTime();
		
		while (sentMessages < maxMessages) {
			if (unlimitedRate || bucketFull) {
				Request test = Request.newPost();
				test.setURI(testURI);
				test.setType(type);
				test.setPayload(PayloadGenerator.generateRandomData(random.nextLong(), payloadsize));
				test.send(endpoint);
				bucketFull = false;
				sentMessages += 1;
				while (test.isConfirmable() && (test.isAcknowledged() || test.isTimedOut() || test.isCanceled() || test.isRejected()))
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
			if (!unlimitedRate && System.nanoTime() > nextTimeToFillBucket) {
				bucketFull = true;
				nextTimeToFillBucket += bucketFillDelay;
			}
		}
	}
	private static void runFileTest(TrafficConfig config, CoAPEndpoint endpoint) throws InterruptedException {
		//TODO: Implement rate limiting -- by taking test.send(endpoint) in a pausable thread?
		byte[] dummyfile = PayloadGenerator.generateRandomData(random.nextLong(), config.getIntegerSetting(Settings.TRAFFIC_FILESIZE));
		Request test;
		test = Request.newPost();
		String testURI = String.format(Locale.ROOT, "coap://%1$s:%2$d/test", config.getStringSetting(Settings.TEST_SERVER), config.getIntegerSetting(Settings.TEST_TESTPORT));
		test.setURI(testURI);
		test.setPayload(dummyfile);
		test.send(endpoint);
		test.waitForResponse();
	}
	
	private static void runMultipleThreads(TrafficConfig config) throws InterruptedException {
		try {
			String s = config.getStringSetting(Settings.TEST_SERVER);
			Log.i("SENDTEST", "Nr of threads starting: " + config.getIntegerSetting(Settings.TRAFFIC_NRTHREADS));
			for(int i=0; i < config.getIntegerSetting(Settings.TRAFFIC_NRTHREADS); i++) {
				//Thread.sleep(200);
				new SendDataThread(i+1, config).start();
			}
		} catch(Exception e) {
			Log.e("THREAD", "Error in threadloop: " + e.getMessage());
			e.printStackTrace();
		}
	}	

}

package se.ltu.trafikgeneratorcoap.testing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;

public class Tester {
	private CoAPEndpoint control = null;
	private boolean openChannel = false, sending = false;
	private String timestamp, token;
	private TrafficConfig config;
	        Context context = null;
	        enum Xfer {SEND, RECEIVE};
	public Tester(TrafficConfig config) {
		this.config = config;
	}
	public Tester(TrafficConfig config, Context context) {
		this.config = config;
		this.context = context;
	}
	public TrafficConfig getConfig() {
		return config;
	}
	public void setConfig(TrafficConfig config) {
		this.config = config;
	}
	private boolean negotiate(Xfer type) throws InterruptedException, IOException {
		control = new CoAPEndpoint(NetworkConfig.createStandardWithoutFile());
		control.start();
		Request controlRequest = type.equals(Xfer.SEND)?
									Request.newPost():
									Request.newGet();
		controlRequest.setURI(String.format("coap://%1$s:%2$d/control?time=%3$s",
				config.getStringSetting(Settings.TEST_SERVER),
				config.getIntegerSetting(Settings.TEST_SERVERPORT),
				timestamp));
		controlRequest.setPayload(type.equals(Xfer.SEND)?
									TrafficConfig.networkConfigToStringList(config.toNetworkConfig()):
									config.getOriginal());
		controlRequest.send(control);
		Response response = controlRequest.waitForResponse();
		token = controlRequest.getTokenString();
		if (response != null && response.getCode().equals(type.equals(Xfer.SEND)?
															ResponseCode.CREATED:
															ResponseCode.CONTINUE)) {
			openChannel = true;
			sending = type.equals(Xfer.SEND)?true:false;
			return true;
		}
		else
			return false;
	}
	public void send() throws InterruptedException, IOException {
		timestamp = (new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())).format(new Date());

		SendTest.run(config);
			
		
		/*if (negotiate(Xfer.SEND)) {
			Logfile logfile = new Logfile(Xfer.SEND, config, timestamp, token);
			if (logfile.startLogging()) {
				Metafile metafile = new Metafile(config, timestamp, token);
				if (metafile.synchronize()) {
					SendTest.run(config);
					if (metafile.synchronize()) {
						metafile.write();
						logfile.stopAllLogging();
						if (abort())
							sendLogs(Xfer.SEND);
					}
				}
			}
			abort();
		}*/
	}
	public void receive() throws InterruptedException, IOException {
		//TODO: Make it work...
		timestamp = (new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())).format(new Date());
		if (negotiate(Xfer.RECEIVE)) {
			Logfile logfile = new Logfile(Xfer.RECEIVE, config, timestamp, token);
			if (logfile.startLogging()) {
				Metafile metafile = new Metafile(config, timestamp, token);
				if (metafile.synchronize()) {
					ReceiveTest.run(config);
					while(ReceiveTest.running)
						Thread.sleep(100);
					if (metafile.synchronize()) {
						metafile.write();
						logfile.stopAllLogging();
						if (abort())
							sendLogs(Xfer.RECEIVE);
					}
				}
			}
			abort();
		}
	}
	private boolean sendLogs(Xfer type) throws IOException, InterruptedException {
		return FileSender.sendMetafile(config.getStringSetting(Settings.TEST_SERVER), token, timestamp) &&
				FileSender.sendLogfile(type, config.getStringSetting(Settings.TEST_SERVER), token, timestamp);
	}
	public boolean abort() throws InterruptedException {
		//TODO: Clean up when acting as server.
		if (openChannel && sending) {
			if (control != null) {
				Request controlRequest = Request.newDelete();
				controlRequest.setURI(String.format("coap://%1$s/control?token=%2$s", config.getStringSetting(Settings.TEST_SERVER), token));
				controlRequest.send(control);
				Response response = controlRequest.waitForResponse();
				if (response != null && response.getCode().equals(ResponseCode.DELETED)) {
					openChannel = sending = false;
					return true;
				}
				else
					return false;
			}
			else
				return false;
		}
		else if (openChannel) {
			ReceiveTest.stop();
			return true;
		}
		else
			return false;
	}
}
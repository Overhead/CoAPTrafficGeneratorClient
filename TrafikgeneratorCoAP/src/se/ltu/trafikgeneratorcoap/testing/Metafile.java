package se.ltu.trafikgeneratorcoap.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
/*import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;*/
import android.os.SystemClock;

public class Metafile {
	//private static int gsmSignalStrength = 99;
	private static File logDirectory = new File(new File(Environment.getExternalStorageDirectory(), "trafikgeneratorcoap"), "logs");
	private TrafficConfig config;
	private String timestamp, token;
	private File metafile;
	SntpClient timeBeforeTest = null;
	SntpClient timeAfterTest = null;
	Metafile(TrafficConfig config, String timestamp, String token) {
		this.config = config;
		this.timestamp = timestamp;
		this.token = token;
	}
	boolean synchronize() {
		/*
		 * Synchronizing with the test server's NTP server.
		 */
		if (timeBeforeTest == null) {
			timeBeforeTest = new SntpClient();
			return timeBeforeTest.requestTime(config.getStringSetting(Settings.TEST_SERVER).split(":")[0], config.getIntegerSetting(Settings.TEST_NTPPORT), 1000) || timeBeforeTest.requestTime("pool.ntp.org", 123, 1000);
		}
		else if (timeAfterTest == null) {
			timeAfterTest = new SntpClient();
			return timeAfterTest.requestTime(config.getStringSetting(Settings.TEST_SERVER).split(":")[0], config.getIntegerSetting(Settings.TEST_NTPPORT), 1000) || timeBeforeTest.requestTime("pool.ntp.org", 123, 1000);
		}
		else {
			return false;
		}
	}
	void write() throws IOException {
		metafile = new File(logDirectory, timestamp + "-" + token + "-meta.txt");
		/*Context context = Sending.context;
		PhoneStateListener listener = new PhoneStateListener() {
			public void onSignalStrengthsChanged(SignalStrength signalStrength){
				Meta.gsmSignalStrength = signalStrength.getGsmSignalStrength();
			}
		};
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		manager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		String networkType;
		switch(manager.getNetworkType()){
			case 0: networkType = "UNKNOWN"; break;
			case 1: networkType = "GPRS"; break;
			case 2: networkType = "EDGE"; break;
			case 3: networkType = "UMTS"; break;
			case 4: networkType = "CDMA"; break;
			case 5: networkType = "EVDO_0"; break;
			case 6: networkType = "EVDO_A"; break;
			case 7: networkType = "1xRTT"; break;
			case 8: networkType = "HSDPA"; break;
			case 9: networkType = "HSUPA"; break;
			case 10: networkType = "HSPA"; break;
			case 11: networkType = "iDen"; break;
			case 12: networkType = "EVDO_B"; break;
			case 13: networkType = "LTE"; break;
			case 14: networkType = "eHRPD"; break;
			case 15: networkType = "HSPA+"; break;
			default: networkType = "UNKNOWN";
		}*/
		metafile.getParentFile().mkdirs();
		BufferedWriter buf = new BufferedWriter(new FileWriter(metafile));
		buf.write("DATETIME=" + timestamp); buf.newLine();
		buf.write(config.getOriginal()); buf.newLine();
		buf.write("BEFORE_TEST NTP_SERVER=" + timeBeforeTest.getHost()); buf.newLine();
		buf.write("BEFORE_TEST NTP_ERROR=" + (timeBeforeTest.getNtpTime() + SystemClock.elapsedRealtime() - timeBeforeTest.getNtpTimeReference() - System.currentTimeMillis())); buf.newLine();
		buf.write("AFTER_TEST NTP_SERVER=" + timeAfterTest.getHost()); buf.newLine();
		buf.write("AFTER_TEST NTP_ERROR=" + (timeAfterTest.getNtpTime() + SystemClock.elapsedRealtime() - timeAfterTest.getNtpTimeReference() - System.currentTimeMillis())); buf.newLine();
		//buf.write("BEFORE_TEST NETWORK_TYPE=" + "networkType"); buf.newLine();
		//buf.write("BEFORE_TEST SIGNAL_STRENGTH=" + Metafile.gsmSignalStrength); buf.newLine();
		buf.close();
	}
}

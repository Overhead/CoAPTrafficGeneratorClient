package se.ltu.trafikgeneratorcoap.testing;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import android.os.Environment;

public class Logfile {
	//TODO: Move out tweakable settings like these below? Perhaps into a settings file?
	private static String tcpdump = "tcpdump-coap", tcpdumpPath = "/data/local/", tcpdumpInterface = "";//"-i wlan0";
	private static int packetCutoff = 84, tcpdumpPrepareTime = 5000;
	private static File logDirectory = new File(new File(Environment.getExternalStorageDirectory(), "trafikgeneratorcoap"), "logs");
	private static File script = new File(new File(new File(Environment.getExternalStorageDirectory(), "trafikgeneratorcoap"), "scripting"), "SaveMyPID.sh");
	private int port;
	private String timestamp, token, type;
	private File logfile;
	private Process logProcess;
	Logfile(Tester.Xfer type, TrafficConfig config, String timestamp, String token) {
		this.type = type.equals(Tester.Xfer.SEND)?"sndr":"rcvr";
		this.port = config.getIntegerSetting(Settings.TEST_TESTPORT);
		this.timestamp = timestamp;
		this.token = token;
	}
	boolean startLogging() throws IOException, InterruptedException {
		/*
		 * Logging is done through a tcpdump binary, and it is started through a shell script
		 * in order to save its process ID so it can be used to kill it later on. Something
		 * like "killall tcpdump" can be used, of course, but not all Android machines even
		 * have killall...
		 */
		if (logfile != null)
			return false;
		logfile = new File(logDirectory, timestamp + "-" + token + "-" + type + ".pcap");
		logfile.getParentFile().mkdirs();
		if (!logfile.exists()) {
			String command = String.format(Locale.ROOT, "%1$s", (tcpdumpPath+tcpdump));
			String arguments = String.format(Locale.ROOT, " %1$s -s %2$d -w %3$s 'port %4$d'", tcpdumpInterface, packetCutoff, logfile.toString(), port);
			String commandAsArgument = (command + arguments);
			String shellCommand = String.format(Locale.ROOT, "sh %1$s %2$s \"%3$s\"", script, token, commandAsArgument);
			logProcess = (new ProcessBuilder()).command(new String[] {"su", "-c", shellCommand}).start();
			Thread.sleep(tcpdumpPrepareTime);
			return true;
		}
		else
			return false;
	}
	void stopAllLogging() throws InterruptedException, IOException {
		File pidFile = new File(new File(new File(Environment.getExternalStorageDirectory(), "trafikgeneratorcoap"), "scripting"), token + ".pid");
		if (pidFile.exists()) {
			String command = "su ; kill -s SIGINT `cat " + pidFile.toString() + "`";
			Thread.sleep(tcpdumpPrepareTime);
			Runtime.getRuntime().exec(command).waitFor();
			logProcess.destroy();
			pidFile.delete();
		}
	}
}

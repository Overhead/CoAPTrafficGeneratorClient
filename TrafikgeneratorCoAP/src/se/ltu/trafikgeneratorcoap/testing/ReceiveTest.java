package se.ltu.trafikgeneratorcoap.testing;

import java.util.Locale;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ReceiveTest {
	/*
	 * To receive data, we need to start a server. Ideally, we would reuse
	 * or at least build upon the classes from TrafikgeneratorServer, but
	 * I don't know how to do that without having cyclic dependencies.
	 */
	private static Server testserver;
	static boolean running = false;
	static void run(TrafficConfig config) {
		//TODO: Start server, tell PC to start sending, ...
		NetworkConfig testConfig = TrafficConfig.stringListToNetworkConfig(TrafficConfig.configToString(config));
		testserver = new Server(testConfig, config.getIntegerSetting(Settings.TEST_TESTPORT));
		testserver.setExecutor(Executors.newScheduledThreadPool(4));
		TestResource test = new TestResource("test", null, null);
		testserver.add(test);
		testserver.start();
		running = true;
		String testURI = String.format(Locale.ROOT, "coap://%1$s:%2$d/control", config.getStringSetting(Settings.TEST_SERVER), config.getIntegerSetting(Settings.TEST_SERVERPORT));
		Request.newGet().setURI(testURI).send();
	}
	static void stop() {
		testserver.stop();
		testserver.destroy();
		running = false;
	}
	private static class TestResource extends ResourceBase  {
		public TestResource(String name, String IP, Long TTL) {
			super(name);
		}
		public void handlePOST(CoapExchange exchange) {
			if (exchange.advanced().getCurrentRequest().isConfirmable())
				exchange.respond(ResponseCode.CONTINUE);
		}
		public void handleDELETE(CoapExchange exchange) {
			testserver.stop();
			testserver.destroy();
			exchange.respond(ResponseCode.DELETED);
			running = false;
		}
	}
}

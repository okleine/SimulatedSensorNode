package de.uniluebeck.itm.ncoap.examples.nodesimulation;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.client.CoapResponseProcessor;
import de.uniluebeck.itm.ncoap.communication.observe.ObservationTimeoutProcessor;
import de.uniluebeck.itm.ncoap.examples.nodesimulation.SimulatedSensorNode;
import de.uniluebeck.itm.ncoap.examples.nodesimulation.SimulatedTemperatureService;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.header.Code;
import de.uniluebeck.itm.ncoap.message.header.MsgType;
import org.apache.log4j.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 21.10.13
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
public class ParallelObservations {

    private Logger log = Logger.getLogger(this.getClass().getName());

    @Test
    public void testParallelObservations() throws Exception {
        SimulatedSensorNode sensorNode = new SimulatedSensorNode(new InetSocketAddress(5683));

        sensorNode.addWebservice(new SimulatedTemperatureService(InetAddress.getLocalHost(), "/temperature-1", 5));
        sensorNode.addWebservice(new SimulatedTemperatureService(InetAddress.getLocalHost(), "/temperature-2", 6));
        sensorNode.addWebservice(new SimulatedTemperatureService(InetAddress.getLocalHost(), "/temperature-3", 7));

        CoapClientApplication coapClientApplication = new CoapClientApplication(2);


        for(int i = 1; i <= 3; i++){
            String path = "/temperature-" + i;
            CoapRequest coapRequest = new CoapRequest(MsgType.CON, Code.GET,
                    new URI("coap", null, "localhost", -1, path, null, null));
            coapRequest.setObserveOptionRequest();
            coapClientApplication.writeCoapRequest(coapRequest, new CoapObservationResponseProcessor(path));
        }

        Thread.sleep(30000);
    }

    private class CoapObservationResponseProcessor implements CoapResponseProcessor, ObservationTimeoutProcessor{

        private String path;

        public CoapObservationResponseProcessor(String path){
            this.path = path;
        }

        @Override
        public void processObservationTimeout(InetSocketAddress remoteAddress) {
            log.error("Observation Timeout! Path: " + path);
        }

        @Override
        public void processCoapResponse(CoapResponse coapResponse) {
            log.info("Received Update Notification (path: " + path + "): " + coapResponse);
        }
    }
}

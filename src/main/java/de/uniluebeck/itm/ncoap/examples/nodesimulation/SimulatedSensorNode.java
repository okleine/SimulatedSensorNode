package de.uniluebeck.itm.ncoap.examples.nodesimulation;

import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.application.server.webservice.WebService;

import java.net.*;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 27.09.13
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class SimulatedSensorNode {

    private InetSocketAddress socketAddress;
    private CoapServerApplication coapServerApplication;


    public SimulatedSensorNode(InetSocketAddress socketAddress) throws SocketException {
        this.socketAddress = socketAddress;
        coapServerApplication = new CoapServerApplication(socketAddress);
    }

    public static void main(String[] args) throws Exception {
        InetAddress inetAddress = InetAddress.getByName(args[0]);

        SimulatedSensorNode sensorNode = new SimulatedSensorNode(new InetSocketAddress(inetAddress, 5683));
    }
}

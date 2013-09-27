package de.uniluebeck.itm.ncoap.examples.nodesimulation;

import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.application.server.webservice.WebService;
import org.apache.log4j.*;

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

    static{
        String pattern = "%-23d{yyyy-MM-dd HH:mm:ss,SSS} | %-32.32t | %-30.30c{1} | %-5p | %m%n";
        PatternLayout patternLayout = new PatternLayout(pattern);

        AsyncAppender appender = new AsyncAppender();
        appender.addAppender(new ConsoleAppender(patternLayout));
        Logger.getRootLogger().addAppender(appender);

        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getLogger("de.uniluebeck.itm.ncoap.examples.nodesimulation").setLevel(Level.DEBUG);
    }

    private InetSocketAddress socketAddress;
    private CoapServerApplication coapServerApplication;


    public SimulatedSensorNode(InetSocketAddress socketAddress) throws SocketException {
        this.socketAddress = socketAddress;
        coapServerApplication = new CoapServerApplication(socketAddress);
    }

    public void addWebservice(WebService webservice){
        coapServerApplication.registerService(webservice);
    }

    public static void main(String[] args) throws Exception {
        InetAddress inetAddress = InetAddress.getByName(args[0]);

        SimulatedSensorNode sensorNode = new SimulatedSensorNode(new InetSocketAddress(inetAddress, 5683));

        SimulatedTemperatureService service = new SimulatedTemperatureService(inetAddress, "/temperature", 5);

        sensorNode.addWebservice(service);
    }
}

package de.uniluebeck.itm.ncoap.examples.nodesimulation;

import com.google.common.util.concurrent.SettableFuture;
import com.hp.hpl.jena.rdf.model.Model;
import de.uniluebeck.itm.ncoap.application.server.webservice.MediaTypeNotSupportedException;
import de.uniluebeck.itm.ncoap.application.server.webservice.ObservableWebService;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.header.Code;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
* Created with IntelliJ IDEA.
* User: olli
* Date: 27.09.13
* Time: 17:47
* To change this template use File | Settings | File Templates.
*/
public class SimulatedTemperatureService extends ObservableWebService<Double> {

    public static final MediaType DEFAULT_MEDIA_TYPE = MediaType.APP_N3;

    public static final Map<MediaType, String> SUPPORTED_MEDIATYPES = new HashMap<>(3);
    static{
        SUPPORTED_MEDIATYPES.put(MediaType.APP_N3, "N3");
        SUPPORTED_MEDIATYPES.put(MediaType.APP_RDF_XML, "RDF/XML");
        SUPPORTED_MEDIATYPES.put(MediaType.APP_TURTLE, "TURTLE");
    }

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Random rand = new Random(System.currentTimeMillis());

    private URI resourceUri;
    private int updateIntervalSec;

    public SimulatedTemperatureService(InetAddress hostAddress, String path, int updateIntervalSec)
            throws URISyntaxException {
        super(path, 20.0);
        this.resourceUri = new URI("coap", null, hostAddress.getHostAddress(), -1, path, null, null);
        this.updateIntervalSec = updateIntervalSec;
    }

    @Override
    public void setScheduledExecutorService(ScheduledExecutorService executorService){
        super.setScheduledExecutorService(executorService);
        schedulePeriodicResourceUpdate();
    }

    private void schedulePeriodicResourceUpdate(){
        getScheduledExecutorService().scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                setResourceStatus(getResourceStatus() + (rand.nextBoolean() ? 0.1 : -0.1));
                log.info("New status of resource " + getPath() + ": " + getResourceStatus());
            }
        }, 0, updateIntervalSec, TimeUnit.SECONDS);
    }

    @Override
    public void processCoapRequest(SettableFuture<CoapResponse> responseFuture, CoapRequest coapRequest,
                                   InetSocketAddress inetSocketAddress) {
        try{
            if(coapRequest.getCode() == Code.GET){
                MediaType mediaType = determineMediaType(coapRequest.getAcceptedMediaTypes());
                byte[] payload = getSerializedResourceStatus(mediaType);

                CoapResponse coapResponse = new CoapResponse(Code.CONTENT_205);
                coapResponse.setContentType(mediaType);
                coapResponse.setPayload(ChannelBuffers.wrappedBuffer(payload));

                responseFuture.set(coapResponse);
            }
            else{
                responseFuture.set(new CoapResponse(Code.METHOD_NOT_ALLOWED_405));
            }
        }
        catch (MediaTypeNotSupportedException e) {
            if(log.isWarnEnabled()){
                StringBuffer buffer = new StringBuffer();
                for(MediaType mediaType : e.getUnsupportedMediaTypes())
                    buffer.append(mediaType.toString() + " ");
                log.warn("Accepted media type(s) not supported: {}", buffer.toString());
            }

            responseFuture.set(new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415));
        }
        catch (Exception e) {
            log.error("Error while processing CoAP request.", e);
            responseFuture.set(new CoapResponse(Code.INTERNAL_SERVER_ERROR_500));
        }
    }

    private MediaType determineMediaType(Set<MediaType> accpeted) throws MediaTypeNotSupportedException {
        if(accpeted.isEmpty()){
           return DEFAULT_MEDIA_TYPE;
        }
        else{
            for(MediaType mediaType : accpeted){
                if(SUPPORTED_MEDIATYPES.containsKey(mediaType)){
                    log.debug("Found accepted and supported mediatype: {}", mediaType);
                    return mediaType;
                }
                else{
                    log.debug("Accepted mediatype not supported: {}", mediaType);
                }
            }
        }

        MediaType[] tmp = Arrays.copyOf(accpeted.toArray(), accpeted.size(), MediaType[].class);

        throw new MediaTypeNotSupportedException(tmp);
    }

    @Override
    public byte[] getSerializedResourceStatus(MediaType mediaType) throws MediaTypeNotSupportedException {

        String language = SUPPORTED_MEDIATYPES.get(mediaType);

        if(language == null)
            throw new MediaTypeNotSupportedException(mediaType);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.applyPattern("#.#");
        Model model = TemperaturSensorModelFactory.getModel(resourceUri, df.format(getResourceStatus()));
        //Serialize the model associated with the resource and write on OutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        model.write(byteArrayOutputStream, language);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

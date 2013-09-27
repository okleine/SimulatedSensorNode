import com.google.common.util.concurrent.SettableFuture;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import de.uniluebeck.itm.ncoap.application.server.webservice.MediaTypeNotSupportedException;
import de.uniluebeck.itm.ncoap.application.server.webservice.ObservableWebService;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.header.Code;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: olli
* Date: 27.09.13
* Time: 17:47
* To change this template use File | Settings | File Templates.
*/
public class SimulatedTemperatureService extends ObservableWebService<Long> {

    public static final Set<MediaType> SUPPORTED_MEDIATYPES = new HashSet<MediaType>(3);
    static{
        SUPPORTED_MEDIATYPES.add(MediaType.APP_N3);
        SUPPORTED_MEDIATYPES.add(MediaType.APP_RDF_XML);
        SUPPORTED_MEDIATYPES.add(MediaType.APP_TURTLE);
    }

    private Model model;

    public SimulatedTemperatureService(InetAddress hostAddress, String path, Long initialStatus)
            throws URISyntaxException {
        super(path, initialStatus);

        URI resourceUri = new URI("coap", null, hostAddress.getHostAddress(), -1, path, null, null);

        model = ModelFactory.createDefaultModel();
        Resource resource = model.getResource(resourceUri.toString());
        Resource property = model.getProperty("http")
        resource.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void processCoapRequest(SettableFuture<CoapResponse> responseFuture, CoapRequest coapRequest,
                                   InetSocketAddress inetSocketAddress) {
        try{
            if(coapRequest.getCode() == Code.GET)
                responseFuture.set(processGet(coapRequest));
            else
                responseFuture.set(new CoapResponse(Code.METHOD_NOT_ALLOWED_405));
        }
        catch(Exception e){
            log.error("Exception", e);
            responseFuture.set(new CoapResponse(Code.INTERNAL_SERVER_ERROR_500));
        }
    }

    private CoapResponse processGet(CoapRequest coapRequest) {
        //Try to get the payload according to the requested media type
        MediaType contentType = null;
        byte[] payload = null;
        if(coapRequest.getAcceptedMediaTypes().isEmpty()){
            try{
                payload = getSerializedResourceStatus(MediaType.TEXT_PLAIN_UTF8) ;
                contentType = MediaType.TEXT_PLAIN_UTF8;
            }
            catch (MediaTypeNotSupportedException e) {
                return new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415);
            }
        }
        else{
            for(MediaType mediaType : coapRequest.getAcceptedMediaTypes()){
                try{
                    payload = getSerializedResourceStatus(mediaType) ;
                    if(payload != null){
                        contentType = mediaType;
                        break;
                    }
                } catch (MediaTypeNotSupportedException e) {
                    continue;
                }
                return new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415);
            }
        }



    }

    @Override
    public byte[] getSerializedResourceStatus(MediaType mediaType) throws MediaTypeNotSupportedException {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}

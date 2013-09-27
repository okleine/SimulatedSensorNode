package de.uniluebeck.itm.ncoap.examples.nodesimulation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 27.09.13
 * Time: 21:13
 * To change this template use File | Settings | File Templates.
 */
public abstract class TemperaturSensorModelFactory {

    private static Multimap<String, String> properties = HashMultimap.create();
    static{
        properties.put("http://www.w3.org/2000/01/rdf-schema#type",
                                "http://purl.oclc.org/NET/ssnx/ssn#Sensor");
        properties.put("http://spitfire-project.eu/ontology/ns/obs",
                                "http://spitfire-project.eu/property/Temperature");
        properties.put("http://spitfire-project.eu/ontology/ns/uom",
                                "http://spitfire-project.eu/uom/Celsius");
    }

    public static Model getModel(URI resourceUri, String temperature){
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.getResource(resourceUri.toString());

        //Add static properties
        for(String property : properties.keySet()){
            for(String object : properties.get(property)){
                Property p = model.getProperty(property);
                RDFNode o = model.getResource(object);
                resource.addProperty(p, o);
            }
        }

        //Add value property
        Property p = model.getProperty("http://spitfire-project.eu/ontology/ns/value");
        RDFNode o = model.createTypedLiteral(temperature, XSDDatatype.XSDfloat);
        resource.addProperty(p, o);

        return model;
    }
}

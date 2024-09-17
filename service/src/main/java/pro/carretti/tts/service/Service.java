package pro.carretti.tts.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/hello")
@RegisterRestClient
public interface Service {

    @GET
    String get(@HeaderParam("Txn-Token") String txnToken);

}

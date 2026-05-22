package pro.carretti.tts.service;

import io.quarkus.security.Authenticated;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.net.URI;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

@Authenticated
@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    private final Optional<Service> service;

    @Inject
    JsonWebToken txToken;

    public GreetingResource() {
        service = ConfigProvider.getConfig().getOptionalValue("carretti.service.downstream.url", String.class).map(uri ->
            RestClientBuilder.newBuilder()
                    .baseUri(URI.create(uri))
                    .build(Service.class)
        );
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOG.info("hello");

        // Decode and pretty print the transaction token as JSON
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SignedJWT signedJWT = SignedJWT.parse(txToken.getRawToken());
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(signedJWT.getPayload().toJSONObject());
            LOG.infov("Received transaction token:\n{0}", prettyJson);
        } catch (java.text.ParseException e) {
            LOG.warnv("Failed to parse transaction token: {0}", e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return service.map(s -> s.get(txToken.getRawToken())).orElse("Hello from Service!");
    }
}

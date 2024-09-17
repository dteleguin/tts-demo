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

@Authenticated
@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    private final Optional<Service> service;

    @Inject
    JsonWebToken jwt;

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
        LOG.infov("JWT = {0}", jwt);
        return service.map(s -> s.get(jwt.getRawToken())).orElse("Hello from Service!");
    }
}

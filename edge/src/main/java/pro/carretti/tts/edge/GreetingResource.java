package pro.carretti.tts.edge;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.*;
import com.nimbusds.oauth2.sdk.http.*;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.oauth2.sdk.token.*;
import com.nimbusds.oauth2.sdk.tokenexchange.*;

import io.quarkus.security.Authenticated;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.net.*;
import java.util.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    @RestClient
    Service service;

    private static final Identifier N_A = new Identifier("N_A");
    private static TokenTypeURI TXN_TOKEN_TYPE;

    private static final String DEFAULT_TTS_ENDPOINT = "http://keycloak-tts:8081/realms/internal/protocol/openid-connect/token";
    private static final String DEFAULT_TTS_AUDIENCE = "http://trust-domain.example";
    private static final String DEFAULT_TTS_RESOURCE = "https://backend.example.com/api";
    private static final String DEFAULT_TTS_CLIENT = "tts-client";
    private static final String DEFAULT_TTS_CLIENT_SECRET = "my-special-client-secret";

    static {
        try {
            TXN_TOKEN_TYPE = TokenTypeURI.parse("urn:ietf:params:oauth:token-type:txn_token");
        } catch (ParseException ex) {
        }
    }

    @Inject
    JsonWebToken jwt;

    @Inject
    @ConfigProperty(name = "carretti.edge.tts.endpoint", defaultValue = DEFAULT_TTS_ENDPOINT)
    private String endpoint;

    @Inject
    @ConfigProperty(name = "carretti.edge.tts.audience", defaultValue = DEFAULT_TTS_AUDIENCE)
    private String audience;

    @Inject
    @ConfigProperty(name = "carretti.edge.tts.resource", defaultValue = DEFAULT_TTS_RESOURCE)
    private String resource;

    @Inject
    @ConfigProperty(name = "carretti.edge.tts.client", defaultValue = DEFAULT_TTS_CLIENT)
    private String client;

    @Inject
    @ConfigProperty(name = "carretti.edge.tts.client.secret", defaultValue = DEFAULT_TTS_CLIENT_SECRET)
    private String secret;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    public String hello() throws URISyntaxException, IOException, ParseException {
        LOG.info("hello");
        String token = jwt.getRawToken();
        LOG.info("Obtaining transaction token...");
        AccessToken txToken = getTxToken(token);
        String s = txToken.toString();
        LOG.infov("Obtained transaction token: {0}...", s.substring(0, Math.min(s.length(), 16)));
        return service.get(txToken.getValue());
    }

    private AccessToken getTxToken(String token) throws URISyntaxException, IOException, ParseException {
        // The client credentials for a basic authentication
        ClientID clientID = new ClientID(client);
        Secret clientSecret = new Secret(secret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        // The upstream access token (must have been validated)
        AccessToken accessToken = new BearerAccessToken(token);

        // Compose the token exchange request
        URI tokenEndpoint = new URI(endpoint);
        Scope scope = null; // default scope for resource
        TokenTypeURI txnTokenType = TokenTypeURI.parse("urn:ietf:params:oauth:token-type:txn_token");
        List<Audience> audiences = Audience.create(audience);
        List<URI> resources = Collections.singletonList(new URI(resource));
        TokenRequest tokenRequest = new TokenRequest(
                tokenEndpoint,
                clientSecretBasic,
                new TokenExchangeGrant(accessToken, TokenTypeURI.ACCESS_TOKEN, null, null, txnTokenType, audiences),
                scope,
                resources,
                null);

        // Send the token request
        HTTPRequest httpRequest = tokenRequest.toHTTPRequest();
        HTTPResponse httpResponse = httpRequest.send();

        // Parse the token response
        TokenResponse tokenResponse = TokenResponse.parse(httpResponse);

        if (!tokenResponse.indicatesSuccess()) {
            // The token request failed
            ErrorObject errorObject = tokenResponse.toErrorResponse().getErrorObject();
            LOG.warnv("Token request failed: {0}", errorObject.getHTTPStatusCode());
            return null;
        }

        AccessTokenResponse tokenSuccessResponse = tokenResponse.toSuccessResponse();

        // Expecting access token of type Bearer
        AccessToken downstreamToken = tokenSuccessResponse.getTokens().getAccessToken();

        if (!N_A.equals(downstreamToken.getType())
                && !TXN_TOKEN_TYPE.equals(downstreamToken.getIssuedTokenType())) {
            // Unexpected token type
            LOG.warnv("Received unexpected token: {0}", downstreamToken.getIssuedTokenType());
            return null;
        }
        return downstreamToken;
    }

}

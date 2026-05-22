package pro.carretti.tts.edge;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.spiffe.exception.JwtSourceException;
import io.spiffe.exception.JwtSvidException;
import io.spiffe.exception.SocketEndpointAddressException;
import io.spiffe.svid.jwtsvid.JwtSvid;
import io.spiffe.workloadapi.DefaultJwtSource;
import io.spiffe.workloadapi.JwtSource;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger LOG = Logger.getLogger(AppLifecycleBean.class);

    void onStart(@Observes StartupEvent ev) {
        LOG.info("The application is starting...");

        try {
            JwtSource source = DefaultJwtSource.newSource();
            JwtSvid svid = source.fetchJwtSvid("foo", "bar");
            LOG.infov("Obtained SVID: {0}", svid.getSpiffeId());
        } catch (JwtSourceException | SocketEndpointAddressException | JwtSvidException e) {
            LOG.error("Error obtaining SVID", e);
        }

    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("The application is stopping...");
    }

}
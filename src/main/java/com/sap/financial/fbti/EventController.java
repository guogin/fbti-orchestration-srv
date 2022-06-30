package com.sap.financial.fbti;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
public class EventController {

    private final DestinationConfigurationProperties destinationConfigurationProperties;
    private static final EventFormat CE_JSON_FORMATTER =
            EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);

    @PostMapping(value = "/")
    public ResponseEntity<String> handleEvent(@RequestBody CloudEvent event) {
        String request = stringify(event);
        log.debug("Event received:\n" + request);

        final String destinationName = destinationConfigurationProperties.getName();
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp();
        log.debug("Destination '{}' is retrieved", destinationName);
        log.debug("Destination URI: {}", destination.getUri());

        HttpClient httpClient = HttpClientAccessor.getHttpClient(destination);


        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/", method = RequestMethod.OPTIONS)
    public ResponseEntity<String> handShake() {
        log.debug("Received handshake request");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("WebHook-Allowed-Origin", "*");
        return ResponseEntity.ok().headers(responseHeaders).body(null);
    }

    private String stringify(CloudEvent event) {
        return new String(CE_JSON_FORMATTER.serialize(event));
    }
}

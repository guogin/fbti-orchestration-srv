package com.sap.financial.fbti;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.s4hana.connectivity.DefaultErpHttpDestination;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

@RestController
@AllArgsConstructor
@Slf4j
public class EventController {

    private final DestinationConfigurationProperties destinationConfigurationProperties;
    private static final EventFormat CE_JSON_FORMATTER =
            EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);

    @PostMapping(value = "/")
    public ResponseEntity<String> handleEvent(@RequestBody CloudEvent event) throws IOException {
        String request = stringify(event);
        log.debug("Event received:\n" + request);

        final String destinationName = destinationConfigurationProperties.getName();
        HttpDestination destination = DestinationAccessor.getDestination(destinationName).asHttp().decorate(DefaultErpHttpDestination::new);
        log.debug("Destination '{}' is retrieved", destinationName);
        log.debug("Destination URI: {}", destination.getUri());

        HttpClient httpClient = HttpClientAccessor.getHttpClient(destination);
        HttpGet httpGet = new HttpGet(destination.getUri());

        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            log.debug("Response: {}", result);
        }

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

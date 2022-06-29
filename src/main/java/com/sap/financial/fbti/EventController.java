package com.sap.financial.fbti;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.rw.CloudEventRWException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
public class EventController {
    private static final EventFormat CE_JSON_FORMATTER =
            EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);

    @PostMapping(value = "/")
    public ResponseEntity<String> handleEvent(@RequestBody CloudEvent event) {
        String request = stringify(event);
        log.debug("Event received:\n" + request);

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

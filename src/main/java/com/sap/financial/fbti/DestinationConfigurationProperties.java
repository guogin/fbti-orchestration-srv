package com.sap.financial.fbti;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "my.destination")
@ConstructorBinding
@AllArgsConstructor
@Getter
public class DestinationConfigurationProperties {
    private final String name;
}

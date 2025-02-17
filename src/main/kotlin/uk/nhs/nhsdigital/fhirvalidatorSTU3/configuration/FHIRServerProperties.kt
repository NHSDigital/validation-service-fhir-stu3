package uk.nhs.nhsdigital.fhirvalidatorSTU3.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "fhir")
data class FHIRServerProperties(
    var server: Server
) {
    data class Server(
        var baseUrl: String,
        var name: String,
        var version: String
    )
}

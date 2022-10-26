package uk.nhs.nhsdigital.fhirvalidatorSTU3.configuration

import ca.uhn.fhir.context.FhirContext
import uk.nhs.nhsdigital.fhirvalidatorSTU3.model.SimplifierPackage
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.hl7.fhir.dstu3.model.Bundle
import org.hl7.fhir.utilities.npm.NpmPackage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.streams.toList


@Configuration
open class PackageConfiguration(val objectMapper: ObjectMapper) {
    companion object : KLogging()



    @Bean
    open fun getPackages(): List<NpmPackage> {
        val inputStream = ClassPathResource("manifest.json").inputStream
        val packages = objectMapper.readValue(inputStream, Array<SimplifierPackage>::class.java)
        return Arrays.stream(packages)
            .map { "${it.packageName}-${it.version}.tgz" }
            .map { ClassPathResource(it).inputStream }
            .map { NpmPackage.fromPackage(it) }
            .toList()
    }

    @Bean
    open fun getCoreSearchParamters(@Qualifier("STU3") ctx: FhirContext) : Bundle? {

        // TODO could maybe get this from packages
        val u = URL("http://hl7.org/fhir/STU3/search-parameters.json")
        try {
            val io: InputStream = u.openStream()
            val inputStreamReader = InputStreamReader(io, Charset.forName("UTF-8"))
            return ctx.newJsonParser().parseResource(inputStreamReader) as Bundle
        }
        catch (ex : Exception) {
            return null
        }
    }
}

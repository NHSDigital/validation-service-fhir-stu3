package uk.nhs.nhsdigital.fhirvalidatorSTU3.provider

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.annotation.RequiredParam
import ca.uhn.fhir.rest.annotation.Search
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.dstu3.model.NamingSystem
import org.hl7.fhir.utilities.npm.NpmPackage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.nhs.nhsdigital.fhirvalidatorSTU3.service.ImplementationGuideParser
import java.nio.charset.StandardCharsets

@Component
class NamingSystemProvider (@Qualifier("STU3") private val fhirContext: FhirContext, private val npmPackages: List<NpmPackage>) : IResourceProvider {
    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    override fun getResourceType(): Class<NamingSystem> {
        return NamingSystem::class.java
    }

    var implementationGuideParser: ImplementationGuideParser? = ImplementationGuideParser(fhirContext)


    @Search
    fun search(@RequiredParam(name = NamingSystem.SP_VALUE) value: TokenParam): List<NamingSystem> {
        val list = mutableListOf<NamingSystem>()
        var decodeUri = java.net.URLDecoder.decode(value.value, StandardCharsets.UTF_8.name());
        for (npmPackage in npmPackages) {
            if (!npmPackage.name().equals("hl7.fhir.r4.core")) {
                for (resource in implementationGuideParser!!.getResourcesOfTypeFromPackage(
                    npmPackage,
                    NamingSystem::class.java
                )) {
                    for (uniqueId in resource.uniqueId)
                    if (uniqueId.value.equals(decodeUri)) {
                        list.add(resource)
                    }
                }
            }
        }
        return list
    }
}

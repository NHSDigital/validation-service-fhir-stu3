package uk.nhs.nhsdigital.fhirvalidatorSTU3

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.IValidationSupport
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.server.RestfulServer
import org.springframework.beans.factory.annotation.Qualifier
import uk.nhs.nhsdigital.fhirvalidatorSTU3.provider.ConversionProviderSTU3
import uk.nhs.nhsdigital.fhirvalidatorSTU3.provider.StructureDefinitionProvider
import uk.nhs.nhsdigital.fhirvalidatorSTU3.provider.ValidateProviderSTU3
import java.util.*
import javax.servlet.annotation.WebServlet

@WebServlet("/FHIR/STU3/*", loadOnStartup = 1)
class FHIRSTU3RestfulServer(
    @Qualifier("STU3") fhirContext: FhirContext,
    private val validateSTU3Provider: ValidateProviderSTU3,
    val conversionProviderSTU3: ConversionProviderSTU3,
    val structureDefinitionProvider: StructureDefinitionProvider,
    @Qualifier("SupportChain") private val supportChain: IValidationSupport

    ) : RestfulServer(fhirContext) {

    override fun initialize() {
        super.initialize()

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        registerProvider(validateSTU3Provider)
        registerProvider(conversionProviderSTU3)
        registerProvider(structureDefinitionProvider)

        isDefaultPrettyPrint = true
        defaultResponseEncoding = EncodingEnum.JSON
    }
}

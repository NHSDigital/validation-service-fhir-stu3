package uk.nhs.nhsdigital.fhirvalidatorSTU3.provider

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.IValidationSupport
import ca.uhn.fhir.context.support.ValidationSupportContext
import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.param.DateParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.dstu3.model.*
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.utilities.npm.NpmPackage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.nhs.nhsdigital.fhirvalidatorSTU3.service.CodingSupport
import uk.nhs.nhsdigital.fhirvalidatorSTU3.service.ImplementationGuideParser
import uk.nhs.nhsdigital.fhirvalidatorSTU3.shared.LookupCodeResultUK
import java.nio.charset.StandardCharsets

@Component
class CodeSystemProvider (@Qualifier("STU3") private val fhirContext: FhirContext,
                          private val supportChain: ValidationSupportChain,
                          private val npmPackages: List<NpmPackage>,
                          private val codingSupport: CodingSupport
) : IResourceProvider {
    /**
     * The getResourceType method comes from IResourceProvider, and must
     * be overridden to indicate what type of resource this provider
     * supplies.
     */
    override fun getResourceType(): Class<CodeSystem> {
        return CodeSystem::class.java
    }
    private val validationSupportContext = ValidationSupportContext(supportChain)

    var implementationGuideParser: ImplementationGuideParser? = ImplementationGuideParser(fhirContext)


    @Search
    fun search(@RequiredParam(name = CodeSystem.SP_URL) url: TokenParam): List<CodeSystem> {
        val list = mutableListOf<CodeSystem>()
        var decodeUri = java.net.URLDecoder.decode(url.value, StandardCharsets.UTF_8.name());
        for (npmPackage in npmPackages) {
            if (!npmPackage.name().equals("hl7.fhir.r4.core")) {
                for (resource in implementationGuideParser!!.getResourcesOfTypeFromPackage(
                    npmPackage,
                    CodeSystem::class.java
                )) {
                    if (resource.url.equals(decodeUri)) {
                        if (resource.id == null) resource.setId(decodeUri)
                        list.add(resource)
                    }
                }
            }
        }
        return list
    }

    @Operation(name = "\$subsumes", idempotent = true)
    fun subsumes (  @OperationParam(name = "codeA") codeA: String?,
                    @OperationParam(name = "codeB") codeB: String?,
                    @OperationParam(name = "system") system: String?) : Parameters? {
        return codingSupport.subsumes(codeA,codeB,java.net.URLDecoder.decode(system, StandardCharsets.UTF_8.name()))
    }



    @Operation(name = "\$lookup", idempotent = true)
    fun validateCode (

        @OperationParam(name = "code") code: String?,
        @OperationParam(name = "system") system: String?,
        @OperationParam(name = "version") version: String?,
        @OperationParam(name = "coding") coding: TokenParam?,
        @OperationParam(name = "date") date: DateParam?,
        @OperationParam(name = "displayLanguage") displayLanguage: CodeType?
    ) : IBaseResource? {
        val input = Parameters()

        if (code != null) {

            var lookupCodeResult: IValidationSupport.LookupCodeResult? =
                supportChain.lookupCode(this.validationSupportContext,  system, code)

            if (lookupCodeResult != null) {
                if (lookupCodeResult is LookupCodeResultUK) {
                    return (lookupCodeResult).originalParameters
                } else {
                    if (lookupCodeResult.codeDisplay != null) {
                        input.addParameter(
                            Parameters.ParametersParameterComponent().setName("display")
                                .setValue(StringType(lookupCodeResult.codeDisplay))
                        )
                    }
                    if (lookupCodeResult.codeSystemDisplayName != null) {
                        input.addParameter(
                            Parameters.ParametersParameterComponent().setName("name")
                                .setValue(StringType(lookupCodeResult.codeSystemDisplayName))
                        )
                    }
                    if (lookupCodeResult.codeSystemVersion != null) {
                        input.addParameter(
                            Parameters.ParametersParameterComponent().setName("version")
                                .setValue(StringType(java.net.URLDecoder.decode(lookupCodeResult.codeSystemVersion, StandardCharsets.UTF_8.name())))
                        )
                    }
                    if (lookupCodeResult.searchedForCode != null) {
                        input.addParameter(
                            Parameters.ParametersParameterComponent().setName("code")
                                .setValue(StringType(lookupCodeResult.searchedForCode))
                        )
                    }
                    if (lookupCodeResult.searchedForSystem != null) {
                        input.addParameter(
                            Parameters.ParametersParameterComponent().setName("system")
                                .setValue(StringType(java.net.URLDecoder.decode(lookupCodeResult.searchedForSystem, StandardCharsets.UTF_8.name())))
                        )

                    }
                }
            }
        }
        return input;
    }


}

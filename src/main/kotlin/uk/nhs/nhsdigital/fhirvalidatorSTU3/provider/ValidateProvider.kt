package uk.nhs.nhsdigital.fhirvalidatorSTU3.provider

import ca.uhn.fhir.parser.DataFormatException
import ca.uhn.fhir.rest.annotation.ResourceParam
import ca.uhn.fhir.rest.annotation.Validate
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ValidationOptions
import mu.KLogging
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.dstu3.model.Bundle
import org.hl7.fhir.dstu3.model.OperationOutcome
import org.hl7.fhir.dstu3.model.Resource
import org.hl7.fhir.dstu3.model.ResourceType
import org.springframework.stereotype.Component
import uk.nhs.nhsdigital.fhirvalidatorSTU3.util.createSTU3OperationOutcome
import javax.servlet.http.HttpServletRequest

@Component
class ValidateProvider (
                        private val validator: FhirValidator
) {
    companion object : KLogging()


    @Validate
    fun validate(
        servletRequest: HttpServletRequest,
        @ResourceParam resource: IBaseResource,
        @Validate.Profile parameterResourceProfile: String?
    ): MethodOutcome {
        val profile = parameterResourceProfile ?: servletRequest.getParameter("profile")

        val resourceR3 = resource as Resource

        val operationOutcome = parseAndValidateResource(resourceR3, profile)
        val methodOutcome = MethodOutcome()
        methodOutcome.operationOutcome = operationOutcome
        return methodOutcome
    }


    fun parseAndValidateResource(inputResource: IBaseResource, profile: String?): OperationOutcome {
        return try {
            val resources = getResourcesToValidate(inputResource)
            val operationOutcomeList = resources.map { validateResource(it, profile) }
            val operationOutcomeIssues = operationOutcomeList.filterNotNull().flatMap { it.issue }
            return createSTU3OperationOutcome(operationOutcomeIssues)
        } catch (e: DataFormatException) {
         //   VerifyController.logger.error("Caught parser error", e)
            createSTU3OperationOutcome(e.message ?: "Invalid JSON", null)
        }
    }

    fun validateResource(resource: IBaseResource, profile: String?): OperationOutcome? {
        if (profile != null) return validator.validateWithResult(resource, ValidationOptions().addProfile(profile))
            .toOperationOutcome() as? OperationOutcome

        val operationOutcome = validator.validateWithResult(resource).toOperationOutcome()
        return operationOutcome as OperationOutcome
    }

    fun getResourcesToValidate(inputResource: IBaseResource?): List<IBaseResource> {
        if (inputResource == null) {
            return emptyList()
        }

        if (inputResource is Bundle
            && inputResource.type == Bundle.BundleType.SEARCHSET) {
            val bundleEntries = inputResource.entry
                .map { it }
            val bundleResources = bundleEntries.map { it.resource }
            if (bundleResources.all { it.resourceType == ResourceType.Bundle }) {
                return bundleResources
            }
        }

        return listOf(inputResource)
    }
}

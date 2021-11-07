package com.example.fhirvalidator.controller

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import ca.uhn.fhir.validation.FhirValidator
import com.example.fhirvalidator.service.CapabilityStatementApplier
import com.example.fhirvalidator.service.MessageDefinitionApplier
import com.example.fhirvalidator.util.createOperationOutcome
import mu.KLogging
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.ResourceType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ValidateController(
    private val fhirContext: FhirContext,
    private val validator: FhirValidator,
    private val messageDefinitionApplier: MessageDefinitionApplier,
    private val capabilityStatementApplier: CapabilityStatementApplier
) {
    companion object : KLogging()

    @PostMapping("/\$validate", produces = ["application/json", "application/fhir+json"])
    fun validate(
        @RequestBody input: String,
        @RequestHeader("x-request-id", required = false) requestId: String?
    ): String {
        requestId?.let { logger.info("started processing message $it") }
        val result = parseAndValidateResource(input)
        requestId?.let { logger.info("finished processing message $it") }
        return fhirContext.newJsonParser().encodeResourceToString(result)
    }

    fun parseAndValidateResource(input: String): OperationOutcome {
        return try {
            val inputResource = fhirContext.newJsonParser().parseResource(input)
            val resources = getResourcesToValidate(inputResource)
            val operationOutcomeList = resources.map { validateResource(it) }
            val operationOutcomeIssues = operationOutcomeList.filterNotNull().flatMap { it.issue }
            return createOperationOutcome(operationOutcomeIssues)
        } catch (e: DataFormatException) {
           if (e.message != null)  createOperationOutcome(e.message!!, null)
           else {
               logger.error("Caught parser error", e)
               createOperationOutcome("Invalid JSON", null)
           }
        }
    }

    fun validateResource(resource: IBaseResource): OperationOutcome? {
        // KGM changed order so message defintion will override profiles added by capability statement
        capabilityStatementApplier.applyCapabilityStatementProfiles(resource)
        val messageDefinitionErrors = messageDefinitionApplier.applyMessageDefinition(resource)
        if (messageDefinitionErrors != null) {
            return messageDefinitionErrors
        }
        val results = validator.validateWithResult(resource).toOperationOutcome() as? OperationOutcome
        if (resource is Bundle && resource.type == Bundle.BundleType.MESSAGE)
            if (results != null && results.hasIssue())  {
                results.issue.forEach {
                    if (it.severity == OperationOutcome.IssueSeverity.WARNING
                        && it.diagnostics.contains("URN reference is not locally contained within the bundle")
                    ) it.severity = OperationOutcome.IssueSeverity.ERROR
                }
            }
        return results
    }

    fun getResourcesToValidate(inputResource: IBaseResource?): List<IBaseResource> {
        return if (inputResource == null) {
            emptyList()
        } else if (inputResource is Bundle && inputResource.type == Bundle.BundleType.SEARCHSET) {

            val resourceList = inputResource.entry.filter { it.resource.resourceType == ResourceType.Bundle}.map { it.resource }
            if (resourceList.isEmpty()) {
                // TODO need to check searchSet bundle rules have been applied. This conversion to list disables the checks
                listOf(inputResource)
                } else {
                return resourceList
            }
        } else {
            listOf(inputResource)
        }
    }
}

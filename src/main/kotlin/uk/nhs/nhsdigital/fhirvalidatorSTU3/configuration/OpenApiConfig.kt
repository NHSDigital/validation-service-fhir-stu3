package uk.nhs.nhsdigital.fhirvalidatorSTU3.configuration


import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType

import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.nhs.nhsdigital.fhirvalidatorSTU3.util.FHIRExamples


@Configuration
open class OpenApiConfig(val ctx : FhirContext) {
    var VALIDATION = "FHIR Validation"

    @Bean
    open fun customOpenAPI(
        fhirServerProperties: FHIRServerProperties
       // restfulServer: FHIRR4RestfulServer
    ): OpenAPI? {

        val oas = OpenAPI()
            .info(
                Info()
                    .title("IOPS - Conformance Support")
                    .version(fhirServerProperties.server.version)
                    .description(fhirServerProperties.server.name
                            + "\n "
                            + "\n [Care Connect Implementation Guide (2.1.0)](https://simplifier.net/guide/hl7fhircareconnectprofilesstu3?version=current)"
                            + "\n\n [NHS Digital STU3 Implementation Guide (0.9.0)](https://fhir.nhs.uk/)"

                    )
                    .termsOfService("http://swagger.io/terms/")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )


        // VALIDATION

        oas.addTagsItem(io.swagger.v3.oas.models.tags.Tag()
            .name(VALIDATION)
            .description("[HL7 FHIR Validation](https://www.hl7.org/fhir/STU3/validation.html)")
        )
        val examplesTOC = LinkedHashMap<String,Example?>()

        examplesTOC.put("ITK3 + Clinic Letter",
            Example().value(FHIRExamples().loadExampleXML("clinic-letter.xml",ctx))
        )
        examplesTOC.put("Clinic Letter",
            Example().value(FHIRExamples().loadExampleXML("toc-clinic-letter.xml",ctx))
        )
        val validateItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(VALIDATION)
                    .summary(
                        "The validate operation checks whether the attached content would be acceptable either generally, as a create, an update or as a delete to an existing resource.")
                    .description("Validating a resource means, checking that the following aspects of the resource are valid: \n"
                            + " - **Structure:** Check that all the content in the resource is described by the specification, and nothing extra is present \n"
                            + " - **Cardinality:** Check that the cardinality of all properties is correct (min & max) \n"
                            + " - **Value Domains:** Check that the values of all properties conform to the rules for the specified types (including checking that enumerated codes are valid) \n"
                            + " - **Coding/CodeableConcept bindings:** Check that codes/displays provided in the Coding/CodeableConcept types are valid \n"
                            + " - **Invariants:** Check that the invariants (co-occurrence rules, etc.) have been followed correctly \n"
                            + " - **Profiles:** Check that any rules in profiles have been followed (including those listed in the Resource.meta.profile, or in CapabilityStatement, or in an ImplementationGuide, or otherwise required by context) \n"
                            + " - **Questionnaires:** Check that a QuestionnaireResponse is valid against its matching Questionnaire \n"
                            + " \n \n"
                            + "The validate operation checks whether the attached content would be acceptable either generally, as a create, an update or as a delete to an existing resource. \n"
                            + "Note that this operation is not the only way to validate resources - see [Validating Resources](https://www.hl7.org/fhir/R4/validation.html) for further information. \n"
                            + "\n"
                            + "The official URL for this operation definition is \n"
                            + " **http://hl7.org/fhir/OperationDefinition/Resource-validate** ")
                    .responses(getApiResponsesXMLJSON())
                    .addParametersItem(Parameter()
                        .name("profile")
                        .`in`("query")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("The uri that identifies the profile. If no profile uri is supplied, NHS Digital defaults will be used.")
                        .schema(StringSchema().format("token"))
                       )
                    .requestBody(RequestBody().content(Content()
                        .addMediaType("application/fhir+xml",MediaType()
                           // .examples(examplesTOC)
                            .schema(StringSchema()))
                        .addMediaType("application/fhir+json",MediaType().schema(StringSchema()._default("{\"resourceType\":\"Patient\"}")))

                    ))
            )
        oas.path("/FHIR/STU3/\$validate",validateItem)


        val convertR4Item = PathItem()
            .post(
                Operation()
                    .addTagsItem(VALIDATION)
                    .summary("Convert to FHIR R4 (Structure only)")
                    .addParametersItem(Parameter()
                        .name("Accept")
                        .`in`("header")
                        .required(true)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("Select response format")
                        .schema(StringSchema()._enum(listOf("application/fhir+json","application/fhir+xml"))))
                    .responses(getApiResponsesXMLJSON())
                    .requestBody(RequestBody().content(Content()
                        .addMediaType("application/fhir+xml",MediaType().schema(StringSchema()))
                        .addMediaType("application/fhir+json",
                            MediaType().schema(StringSchema()._default("{\"resourceType\":\"CapabilityStatement\"}")))
                    )))
        oas.path("/FHIR/STU3/\$convertR4",convertR4Item)


        return oas

    }





    fun getApiResponses() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content().addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }


    fun getApiResponsesXMLJSON() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content()
            .addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
            .addMediaType("application/fhir+xml", MediaType().schema(StringSchema()._default("<>")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }

}

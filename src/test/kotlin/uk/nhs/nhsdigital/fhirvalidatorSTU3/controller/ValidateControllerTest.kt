package uk.nhs.nhsdigital.fhirvalidatorSTU3.controller

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.IValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ValidateControllerTest {
    @Mock
    lateinit var mockFhirContext: FhirContext

    @Mock
    lateinit var mockValidator: FhirValidator


    @Mock
    lateinit var mockSupportChain: IValidationSupport

    @Mock
    lateinit var mockSearchParameters : Bundle


 /*
    @Test
    fun getResourcesToValidate_returns_inner_bundles_when_passed_searchset_containing_bundles() {
        val childBundle = Bundle()
        val bundle = Bundle()
        bundle.type = Bundle.BundleType.SEARCHSET
        val bundleEntry = bundle.addEntry()
        bundleEntry.resource = childBundle
        assertEquals(listOf(childBundle), testValidateController.getResourcesToValidate(bundle))
    }

    @Test
    fun getResourcesToValidate_returns_bundle_when_passed_searchset_containing_other_resource_type() {
        val bundle = Bundle()
        bundle.type = Bundle.BundleType.SEARCHSET
        val bundleEntry = bundle.addEntry()
        bundleEntry.resource = Patient()
        assertEquals(listOf(bundle), testValidateController.getResourcesToValidate(bundle))
    }

    @Test
    fun getResourcesToValidate_returns_bundle_when_passed_other_bundle_type() {
        val bundle = Bundle()
        bundle.type = Bundle.BundleType.COLLECTION
        val bundleEntry = bundle.addEntry()
        bundleEntry.resource = Patient()
        assertEquals(listOf(bundle), testValidateController.getResourcesToValidate(bundle))
    }

    @Test
    fun getResourcesToValidate_returns_resource_when_passed_other_resource_type() {
        val patient = Patient()
        assertEquals(listOf(patient), testValidateController.getResourcesToValidate(patient))
    }

  */
}

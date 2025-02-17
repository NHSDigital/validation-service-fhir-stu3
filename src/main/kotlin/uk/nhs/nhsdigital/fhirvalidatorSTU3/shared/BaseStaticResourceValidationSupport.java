package uk.nhs.nhsdigital.fhirvalidatorSTU3.shared;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.common.hapi.validation.support.BaseValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class BaseStaticResourceValidationSupport extends BaseValidationSupport implements IValidationSupport {
    protected BaseStaticResourceValidationSupport(FhirContext theFhirContext) {
        super(theFhirContext);
    }

    static <T extends IBaseResource> List<T> toList(Map<String, IBaseResource> theMap) {
        ArrayList<IBaseResource> retVal = new ArrayList(theMap.values());
        return (List<T>) Collections.unmodifiableList(retVal);
    }
}

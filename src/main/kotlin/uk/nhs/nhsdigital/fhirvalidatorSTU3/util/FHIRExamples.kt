package uk.nhs.nhsdigital.fhirvalidatorSTU3.util

import ca.uhn.fhir.context.FhirContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import java.io.Reader
import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader


class FHIRExamples {
    public fun loadExampleXML(fileName :String, ctx : FhirContext): JsonNode {
        /// NOT WORKING
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream("Examples/"+fileName)

        val jsonStrings = inputStream.bufferedReader().readLines()
        var sb = StringBuilder()
        for (str in jsonStrings) sb.append(str)

        val reader: Reader = StringReader(sb.toString())
        val factory = XMLInputFactory.newInstance() // Or newFactory()

      //  val xmlReader: XMLStreamReader = factory.createXMLStreamReader(reader)

        val xmlMapper = XmlMapper()
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val node = xmlMapper.readTree(reader)
        return node
    }

}

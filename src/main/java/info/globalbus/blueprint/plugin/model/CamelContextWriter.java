package info.globalbus.blueprint.plugin.model;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.aries.blueprint.plugin.spi.XmlWriter;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by globalbus on 15.04.17.
 */
@RequiredArgsConstructor
public class CamelContextWriter implements XmlWriter {
    private static final String NS_CAMEL = "http://camel.apache.org/schema/blueprint";
    final Collection<String> scanPackages;
    final Map<String, Object> camelOpts;

    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("camelContext");
        writer.writeDefaultNamespace(NS_CAMEL);
        if(StringUtils.isNotBlank(camelOpts.get("contextId").toString()))
            writer.writeAttribute("id", camelOpts.get("contextId").toString());
        writer.writeStartElement("package");
        writer.writeCharacters(scanPackages.stream().collect(Collectors.joining(",")));
        writer.writeEndElement();
        writer.writeEndElement();
    }

}

package info.globalbus.blueprint.plugin.handlers.camel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import lombok.RequiredArgsConstructor;
import org.apache.aries.blueprint.plugin.spi.XmlWriter;
import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isNotBlank(camelOpts.get("contextId").toString())) {
            writer.writeAttribute("id", camelOpts.get("contextId").toString());
        }
        if (StringUtils.isNotBlank(camelOpts.get("properties").toString())) {
            writePropertyPlaceholder(writer);
        }
        for (String pkg : scanPackages) {
            writer.writeStartElement("package");
            writer.writeCharacters(pkg);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void writePropertyPlaceholder(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement("propertyPlaceholder");
        writer.writeAttribute("id", "properties");
        List<?> propertiesList = (List<?>) camelOpts.get("properties");
        List<String> locations = new ArrayList<>();
        locations.add("blueprint:blueprint-properties");
        for (int i = 1; i < propertiesList.size(); i++) {
            Map<?, ?> map = (Map<?, ?>) propertiesList.get(i);
            locations.add("ref:" + map.get("id").toString());
        }
        writer.writeAttribute("location", locations.stream().collect(Collectors.joining(",")));
    }

}
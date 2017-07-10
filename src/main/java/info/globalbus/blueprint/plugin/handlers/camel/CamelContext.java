package info.globalbus.blueprint.plugin.handlers.camel;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;
import org.apache.camel.blueprint.BlueprintCamelContext;

/**
 * Created by globalbus on 15.04.17.
 */
@Slf4j
public class CamelContext implements ContextInitializationHandler {

    @Override
    public void initContext(ContextEnricher contextEnricher) {
        if (contextEnricher instanceof Blueprint) {
            initContext((Blueprint) contextEnricher);
        }
    }

    private void initContext(Blueprint contextEnricher) {
        BlueprintConfigurationImpl configuration = contextEnricher.getBlueprintConfiguration();
        List<String> toScan = configuration.getExtension().getScanPaths();
        Map<String, Object> customOptions = configuration.getExtension().getCustomOptions();
        if (toScan == null || toScan.isEmpty() || customOptions == null) {
            return;
        }
        if (!toScan.isEmpty() && customOptions.containsKey("contextId")) {
            CamelContextWriter writer = new CamelContextWriter(toScan, customOptions);
            contextEnricher.addBean((String) customOptions.get("contextId"), BlueprintCamelContext.class);
            contextEnricher.addBlueprintContentWriter(CamelContextWriter.class.getName(), writer);
        }
    }
}

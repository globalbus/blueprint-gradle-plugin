package info.globalbus.blueprint.plugin.handlers.blueprint.config;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.aries.blueprint.annotation.config.Config;
import org.apache.aries.blueprint.annotation.config.DefaultProperty;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;

public class PropertyDefaults implements ContextInitializationHandler {

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
        if (customOptions.containsKey("properties")) {
            Map<?, ?> firstElement = (Map<?, ?>) customOptions.get("properties");
            contextEnricher.addBlueprintContentWriter("cm/property-placeholder", new ConfigWriter(
                new RuntimeConfig(firstElement)));
        }
    }

    @RequiredArgsConstructor
    class RuntimeConfig implements Config {
        final Map<?, ?> map;

        @Override
        public String pid() {
            return (String) map.get("pid");
        }

        public String id() {
            return (String) map.get("id");
        }

        @Override
        public String updatePolicy() {
            return null;
        }

        @Override
        public String placeholderPrefix() {
            return "${";
        }

        @Override
        public String placeholderSuffix() {
            return "}";
        }

        @Override
        public DefaultProperty[] defaults() {
            if (map.get("defaults") == null) {
                return new DefaultProperty[0];
            }
            Map<?, ?> defaults = (Map<?, ?>) map.get("defaults");
            return defaults.entrySet().stream().map(e -> new DefaultProperty() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return DefaultProperty.class;
                }

                @Override
                public String key() {
                    return String.valueOf(e.getKey());
                }

                @Override
                public String value() {
                    return String.valueOf(e.getValue());
                }
            }).toArray(DefaultProperty[]::new);
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Config.class;
        }
    }
}

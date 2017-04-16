package info.globalbus.blueprint.plugin.handlers;

import lombok.RequiredArgsConstructor;
import info.globalbus.blueprint.plugin.handlers.javax.Namespaces;
import org.apache.aries.blueprint.plugin.spi.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

import static info.globalbus.blueprint.plugin.handlers.javax.Namespaces.NS_TX_1_2_0;
import static info.globalbus.blueprint.plugin.handlers.javax.Namespaces.PATTERN_NS_TX1;
import static info.globalbus.blueprint.plugin.handlers.javax.Namespaces.PATTERN_NS_TX2;

@RequiredArgsConstructor
public abstract class TransactionBase<T extends Annotation> implements BeanAnnotationHandler<T>, MethodAnnotationHandler<T> {
    private static final String ENABLE_ANNOTATION = "transaction.enableAnnotation";


    public abstract String getTransactionTypeName(T transactional);


    @Override
    public void handleMethodAnnotation(Class<?> clazz, List<Method> methods, ContextEnricher contextEnricher, BeanEnricher beanEnricher) {
        final String nsTx1 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), PATTERN_NS_TX1);
        if (nsTx1 != null) {
            enableAnnotationTx1(contextEnricher, nsTx1);
            for (final Method method : methods) {
                final T transactional = method.getAnnotation(getAnnotation());
                final String transactionTypeName = getTransactionTypeName(transactional);
                final String name = method.getName();
                beanEnricher.addBeanContentWriter("javax.transactional.method/" + clazz.getName() + "/" + name + "/" + transactionTypeName, writer -> {
                    writer.writeEmptyElement("transaction");
                    writer.writeDefaultNamespace(nsTx1);
                    writer.writeAttribute("method", name);
                    writer.writeAttribute("value", transactionTypeName);
                });
            }
        }
        final String nsTx2 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), PATTERN_NS_TX2);
        if ((nsTx2 != null) && getEnableAnnotationConfig(contextEnricher.getBlueprintConfiguration())) {
            insertEnableAnnotationTx2(contextEnricher, nsTx2);
        }
    }

    @Override
    public void handleBeanAnnotation(AnnotatedElement annotatedElement, String id, ContextEnricher contextEnricher, BeanEnricher beanEnricher) {
        final String nsTx1 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), PATTERN_NS_TX1);
        if (nsTx1 != null) {
            enableAnnotationTx1(contextEnricher, nsTx1);
            final T transactional = annotatedElement.getAnnotation(getAnnotation());
            final String transactionTypeName = getTransactionTypeName(transactional);
            beanEnricher.addBeanContentWriter("javax.transactional.method/" + annotatedElement + "/*/" + transactionTypeName, writer -> {
                writer.writeEmptyElement("transaction");
                writer.writeDefaultNamespace(nsTx1);
                writer.writeAttribute("method", "*");
                writer.writeAttribute("value", transactionTypeName);
            });
        }
        final String nsTx2 = Namespaces.getNamespaceByPattern(contextEnricher.getBlueprintConfiguration().getNamespaces(), PATTERN_NS_TX2);
        if (nsTx2 != null && getEnableAnnotationConfig(contextEnricher.getBlueprintConfiguration())) {
            insertEnableAnnotationTx2(contextEnricher, nsTx2);
        }
    }


    private void enableAnnotationTx1(ContextEnricher contextEnricher, final String nsTx1) {
        // TX1 enable-annotation are valid only in 1.2.0 schema
        if (NS_TX_1_2_0.equals(nsTx1) && getEnableAnnotationConfig(contextEnricher.getBlueprintConfiguration())) {
            insertEnableAnnotationTx1(contextEnricher, nsTx1);
        }
    }

    private boolean getEnableAnnotationConfig(BlueprintConfiguration blueprintConfig) {
        String enableAnnotation = blueprintConfig.getCustomParameters().get(ENABLE_ANNOTATION);
        return enableAnnotation == null || Boolean.parseBoolean(enableAnnotation);
    }

    private void insertEnableAnnotationTx1(ContextEnricher contextEnricher, final String namespace) {
        contextEnricher.addBlueprintContentWriter("transaction/enable-annotation", writer -> {
            writer.writeEmptyElement("enable-annotations");
            writer.writeDefaultNamespace(namespace);
        });
    }

    private void insertEnableAnnotationTx2(ContextEnricher contextEnricher, final String namespace) {
        contextEnricher.addBlueprintContentWriter("transaction/enable-annotation", writer -> {
            writer.writeEmptyElement("enable");
            writer.writeDefaultNamespace(namespace);
        });
    }
}
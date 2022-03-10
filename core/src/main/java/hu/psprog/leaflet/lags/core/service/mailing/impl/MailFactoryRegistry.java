package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.service.mailing.MailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry for {@link MailFactory} implementations.
 *
 * @author Peter Smith
 */
@Component
public class MailFactoryRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFactoryRegistry.class);

    private Map<Class<? extends MailFactory>, ? extends MailFactory> mailFactoryMap;

    @Autowired
    public MailFactoryRegistry(List<MailFactory> mailFactories) {
        this.mailFactoryMap = mailFactories.stream()
                .collect(Collectors.toMap(MailFactory::getClass, Function.identity()));
    }

    /**
     * Retrieves mail factory by its defining class.
     *
     * @param factoryClass classname of implementing factory class
     * @param <T> type of implementing factory class
     * @return factory instance if registered or {@link IllegalArgumentException} if requested factory is not registered
     */
    public <T extends MailFactory> T getFactory(Class<T> factoryClass) {

        MailFactory factory = mailFactoryMap.get(factoryClass);
        if (Objects.isNull(factory)) {
            LOGGER.error("Unregistered mail factory [{}] requested", factoryClass.getName());
            throw new IllegalArgumentException("Unregistered mail factory [" + factoryClass.getName() + "] requested");
        }

        return (T) factory;
    }
}

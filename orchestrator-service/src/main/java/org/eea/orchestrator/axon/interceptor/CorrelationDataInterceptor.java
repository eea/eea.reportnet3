package org.eea.orchestrator.axon.interceptor;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.unitofwork.UnitOfWork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CorrelationDataInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

    private final List<CorrelationDataProvider> correlationDataProviders;

    public CorrelationDataInterceptor(CorrelationDataProvider... correlationDataProviders) {
        this(Arrays.asList(correlationDataProviders));
    }

    /**
     * Initializes the interceptor that registers given {@code correlationDataProviders} with the current Unit of Work.
     *
     * @param correlationDataProviders The CorrelationDataProviders to register with the Interceptor
     */
    public CorrelationDataInterceptor(Collection<CorrelationDataProvider> correlationDataProviders) {
        this.correlationDataProviders = new ArrayList<>(correlationDataProviders);
    }

    @Override
    public Object handle(UnitOfWork unitOfWork, InterceptorChain interceptorChain) throws Exception {
        correlationDataProviders.forEach(unitOfWork::registerCorrelationDataProvider);
        return interceptorChain.proceed();
    }
}

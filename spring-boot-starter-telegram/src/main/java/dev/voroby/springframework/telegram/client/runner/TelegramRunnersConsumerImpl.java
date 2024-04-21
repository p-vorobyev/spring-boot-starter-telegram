package dev.voroby.springframework.telegram.client.runner;

import dev.voroby.springframework.telegram.TelegramRunner;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.OrderUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TelegramRunnersConsumerImpl implements TelegramRunnersConsumer {

    private final ClientAuthorizationState authorizationState;

    private final ApplicationArguments applicationArguments;

    private final ApplicationContext applicationContext;

    private final AtomicBoolean shutdownSignal = new AtomicBoolean();

    private record OrderedTelegramRunner(int order, TelegramRunner telegramRunner) { }

    public TelegramRunnersConsumerImpl(ClientAuthorizationState authorizationState,
                                       ApplicationArguments applicationArguments,
                                       ApplicationContext applicationContext) {
        this.authorizationState = authorizationState;
        this.applicationArguments = applicationArguments;
        this.applicationContext = applicationContext;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownSignal.set(true)));
    }

    @Override
    public void accept(Collection<TelegramRunner> telegramRunners) {
        if (!telegramRunners.isEmpty()) {
            awaitAuthorizationStateReady();
            runAll(telegramRunners);
        }
    }

    private void awaitAuthorizationStateReady() {
        while (!authorizationState.haveAuthorization() && !shutdownSignal.get()) {
            /*wait for authorization*/
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runAll(Collection<TelegramRunner> telegramRunners) {
        var beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        Map<String, TelegramRunner> beanNameToObj = applicationContext.getBeansOfType(TelegramRunner.class);
        Map<TelegramRunner, String> objToBeanName = new HashMap<>();
        beanNameToObj.forEach((k, v) -> objToBeanName.put(v, k));

        var runnerComparator = Comparator.comparing(OrderedTelegramRunner::order);
        telegramRunners.stream()
                .map(telegramRunner -> getOrderedTelegramRunner(beanFactory, objToBeanName, telegramRunner))
                .sorted(runnerComparator)
                .forEach(orderedTelegramRunner -> {
                    try {
                        orderedTelegramRunner.telegramRunner.run(applicationArguments);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private OrderedTelegramRunner getOrderedTelegramRunner(ConfigurableListableBeanFactory beanFactory,
                                                           Map<TelegramRunner, String> objToBeanName,
                                                           TelegramRunner telegramRunner) {
        RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanFactory.getMergedBeanDefinition(objToBeanName.get(telegramRunner));
        int order;
        if (rootBeanDefinition.getResolvedFactoryMethod() != null &&
                rootBeanDefinition.getResolvedFactoryMethod().getAnnotation(Order.class) != null) {
            order = rootBeanDefinition.getResolvedFactoryMethod().getAnnotation(Order.class).value(); // annotation on factory method in @Configuration
        } else {
            order = OrderUtils.getOrder(telegramRunner.getClass(), Ordered.LOWEST_PRECEDENCE); // annotation on class name or default
        }
        return new OrderedTelegramRunner(order, telegramRunner);
    }

}

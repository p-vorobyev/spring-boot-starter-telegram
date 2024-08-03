package dev.voroby.springframework.telegram.client.runner;

import dev.voroby.springframework.telegram.TelegramRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = TelegramRunnersConsumerConfig.class)
public class TelegramRunnersConsumerTest {

    private static final List<Integer> nums = new ArrayList<>();

    static TelegramRunner first = args -> nums.add(1);

    static TelegramRunner second = args -> nums.add(2);

    static TelegramRunner third = args -> nums.add(3);

    @Autowired
    private List<TelegramRunner> telegramRunners;

    @Autowired
    private TelegramRunnersConsumer telegramRunnersConsumer;

    @BeforeEach
    void cleanUpResources() {
        nums.clear();
    }

    @RepeatedTest(5)
    void invokeTelegramRunnersConsumer() {
        telegramRunnersConsumer.accept(telegramRunners);
        List<Integer> expectedList = List.of(1, 2, 3);
        assertEquals(expectedList, nums);
    }
}
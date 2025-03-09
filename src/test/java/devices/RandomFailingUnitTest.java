package devices;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RandomFailingUnitTest {
    FailingPolicy failingPolicy;
    @Mock
    RandomGenerator randomGenerator;
    private static final String RANDOM_FAILING_POLICY_FIELD_NAME = "random";
    
    @BeforeEach
    public void unit() {
        try {
            MockitoAnnotations.openMocks(this);
            this.failingPolicy = new RandomFailing();
            final Field field = this.failingPolicy.getClass().getDeclaredField(RANDOM_FAILING_POLICY_FIELD_NAME);
            field.setAccessible(true);
            field.set(this.failingPolicy, this.randomGenerator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    @Tag("UnitTest")
    @DisplayName("The policy name of a RandomFailing policy should be 'random'")
    public void testPolicyName() {
        assert(this.failingPolicy.policyName().equals("random"));
    }
    
    @Nested
    class AlwaysFalseRandomFailingPolicy {
        
        @BeforeEach
        public void init() {
            when(RandomFailingUnitTest.this.randomGenerator.nextBoolean()).thenReturn(false);
        }
        
        @Test
        @Tag("UnitTest")
        @DisplayName("The RandomFailing policy should always return false")
        public void testAttemptOn() {
            assertTrue(RandomFailingUnitTest.this.failingPolicy.attemptOn());
        }
        
        @Test
        @Tag("UnitTest")
        @DisplayName("The RandomFailing policy should always return false after reset")
        public void testReset() {
            boolean firstAttemptResult = RandomFailingUnitTest.this.failingPolicy.attemptOn();
            RandomFailingUnitTest.this.failingPolicy.reset();
            boolean secondAttemptResult = RandomFailingUnitTest.this.failingPolicy.attemptOn();
            assertAll(
                () -> assertTrue(firstAttemptResult),
                () -> assertTrue(secondAttemptResult)
            );
        }
    }
    
    @Nested
    class AlwaysTrueRandomFailingPolicy {
        @BeforeEach
        public void init() {
            when(RandomFailingUnitTest.this.randomGenerator.nextBoolean()).thenReturn(true);
        }
        
        @Test
        @Tag("UnitTest")
        @DisplayName("The RandomFailing policy should always return true")
        public void testAttemptOn() {
            assertFalse(RandomFailingUnitTest.this.failingPolicy.attemptOn());
        }
        
        @Test
        @Tag("UnitTest")
        @DisplayName("The RandomFailing policy should always return true after reset")
        public void testReset() {
            boolean firstAttemptResult = RandomFailingUnitTest.this.failingPolicy.attemptOn();
            RandomFailingUnitTest.this.failingPolicy.reset();
            boolean secondAttemptResult = RandomFailingUnitTest.this.failingPolicy.attemptOn();
            assertAll(
                () -> assertFalse(firstAttemptResult),
                () -> assertFalse(secondAttemptResult)
            );
        }
    }
}

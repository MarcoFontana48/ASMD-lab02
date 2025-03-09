package devices;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StandardDeviceUnitTest {
    private Device device;
    @Mock
    private FailingPolicy failingPolicy;
    
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        this.device = new StandardDevice(this.failingPolicy);
    }
    
    @Test
    @DisplayName("Device must specify a failing policy")
    @Tag("UnitTest")
    void testDeviceCannotBeCreatedWithoutFailingPolicy() {
        assertThrows(NullPointerException.class, () -> new StandardDevice(null));
    }
    
    @Test
    @DisplayName("Device is initially off")
    @Tag("UnitTest")
    void testDeviceIsInitiallyOff() {
        this.device = new StandardDevice(this.failingPolicy);
        assertFalse(this.device.isOn());
    }
    
    @Test
    @DisplayName("Throw exception if device is created with null failing policy")
    @Tag("UnitTest")
    void testDeviceCannotBeCreatedWithNullFailingPolicy() {
        assertThrows(NullPointerException.class, () -> new StandardDevice(null));
    }
    
    @Nested
    class AlwaysFailingPolicy {
        @Mock
        FailingPolicy alwaysFailingPolicyStub;
        
        @BeforeEach
        void init() {
            MockitoAnnotations.openMocks(this);
            when(this.alwaysFailingPolicyStub.attemptOn()).thenReturn(false);
            when(this.alwaysFailingPolicyStub.policyName()).thenReturn("alwaysFailingPolicy");
            StandardDeviceUnitTest.this.device = new StandardDevice(this.alwaysFailingPolicyStub);
        }
        
        @Test
        @DisplayName("Device turn on accordingly to failing policy")
        @Tag("UnitTest")
        void testDeviceDoNotTurnOnAccordinglyToFailingPolicy() {
            assertAll(
                    () -> assertThrows(IllegalStateException.class, () -> StandardDeviceUnitTest.this.device.on()),
                    () -> assertFalse(StandardDeviceUnitTest.this.device.isOn())
            );
        }
        
        @Test
        @DisplayName("Return the name of the failing policy")
        @Tag("UnitTest")
        void testReturnTheNameOfTheFailingPolicy() {
            assertEquals("StandardDevice{policy=alwaysFailingPolicy, on=false}", StandardDeviceUnitTest.this.device.toString());
        }
    }
    
    @Nested
    class NeverFailingPolicy {
        @Mock
        FailingPolicy neverFailingPolicyStub;
        
        @BeforeEach
        void init() {
            MockitoAnnotations.openMocks(this);
            when(this.neverFailingPolicyStub.attemptOn()).thenReturn(true);
            when(this.neverFailingPolicyStub.policyName()).thenReturn("NeverFailingPolicy");
            StandardDeviceUnitTest.this.device = new StandardDevice(this.neverFailingPolicyStub);
        }
        
        @Test
        @DisplayName("Return the name of the failing policy")
        @Tag("UnitTest")
        void testReturnTheNameOfTheFailingPolicy() {
            assertEquals("StandardDevice{policy=NeverFailingPolicy, on=false}", StandardDeviceUnitTest.this.device.toString());
        }
        
        @Test
        @DisplayName("Device turn on accordingly to failing policy")
        @Tag("UnitTest")
        void testDeviceTurnOnAccordinglyToFailingPolicy() {
            assertAll(
                    () -> assertDoesNotThrow(() -> StandardDeviceUnitTest.this.device.on()),
                    () -> assertTrue(StandardDeviceUnitTest.this.device.isOn())
            );
        }
        
        @Test
        @DisplayName("Device turn on and off accordingly to failing policy")
        @Tag("UnitTest")
        void testDeviceTurnOnAndOffAccordinglyToFailingPolicy() {
            boolean actualFirstIsOn = StandardDeviceUnitTest.this.device.isOn();
            StandardDeviceUnitTest.this.device.off();
            boolean actualSecondIsOff = StandardDeviceUnitTest.this.device.isOn();
            
            assertAll(
                    () -> assertTrue(actualFirstIsOn),
                    () -> assertFalse(actualSecondIsOff)
            );
        }
    }
    
    @Nested
    class RandomFailingPolicy {
        @Spy
        RandomFailing spyRandomPolicy;
        
        @BeforeEach
        void init() {
            MockitoAnnotations.openMocks(this);
            when(this.spyRandomPolicy.attemptOn()).thenReturn(true);
            StandardDeviceUnitTest.this.device = new StandardDevice(this.spyRandomPolicy);
        }
        
        @Test
        void testSpy() {
            try {
                StandardDeviceUnitTest.this.device.on();
            } catch (IllegalStateException _) {
            }
            verify(this.spyRandomPolicy).attemptOn();
        }
        
        @Test
        @DisplayName("Device turn on and off accordingly to failing policy")
        @Tag("UnitTest")
        void testDeviceTurnOnAndOffAccordinglyToFailingPolicy() {
            boolean actualFirstDeviceIsOn = StandardDeviceUnitTest.this.device.isOn();
            StandardDeviceUnitTest.this.device.off();
            boolean actualSecondDeviceIsOn = StandardDeviceUnitTest.this.device.isOn();
            StandardDeviceUnitTest.this.device.on();
            boolean actualThirdDeviceIsOn = StandardDeviceUnitTest.this.device.isOn();
            assertAll(
                    () -> assertTrue(actualFirstDeviceIsOn),
                    () -> assertFalse(actualSecondDeviceIsOn),
                    () -> assertTrue(actualThirdDeviceIsOn),
                    () -> assertEquals(2, Mockito.mockingDetails(this.spyRandomPolicy).getInvocations().size())
            );
        }
    }
}
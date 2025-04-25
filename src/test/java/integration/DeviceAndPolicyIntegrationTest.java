package integration;

import devices.Device;
import devices.FailingPolicy;
import devices.StandardDevice;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceAndPolicyIntegrationTest {
    @Mock
    private FailingPolicy mockFailingPolicy;
    @Spy
    private Device device;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.device = spy(new StandardDevice(this.mockFailingPolicy));
    }
    
    @Test
    @DisplayName("Constructor should throw NullPointerException when FailingPolicy is null")
    void constructorShouldThrowExceptionWhenPolicyIsNull() {
        assertThrows(NullPointerException.class, () -> new StandardDevice(null));
    }
    
    @Nested
    @DisplayName("Tests for device on() method")
    class OnMethodTests {
        
        @Test
        @DisplayName("on() should set device to on state when policy allows")
        void onShouldSetDeviceOnWhenPolicyAllows() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            DeviceAndPolicyIntegrationTest.this.device.on();
            
            assertAll(
                    () -> assertTrue(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).attemptOn(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).isOn()
            );
        }
        
        @Test
        @DisplayName("on() should throw IllegalStateException when policy denies")
        void onShouldThrowExceptionWhenPolicyDenies() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(false);
            
            assertAll(
                    () -> assertThrows(IllegalStateException.class, () -> DeviceAndPolicyIntegrationTest.this.device.on()),
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).attemptOn(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, times(1)).isOn()
            );
        }
        
        @Test
        @DisplayName("on() should be callable multiple times when policy allows")
        void onShouldBeCallableMultipleTimesWhenPolicyAllows() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            DeviceAndPolicyIntegrationTest.this.device.on();
            DeviceAndPolicyIntegrationTest.this.device.on(); // Second call
            
            assertAll(
                    () -> assertTrue(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy, times(2)).attemptOn(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, atLeastOnce()).isOn()
            );
        }
    }
    
    @Nested
    @DisplayName("Tests for device off() method")
    class OffMethodTests {
        
        @Test
        @DisplayName("off() should set device to off state when device is on")
        void offShouldSetDeviceOffWhenDeviceIsOn() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            boolean isOn = DeviceAndPolicyIntegrationTest.this.device.isOn();
            
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            assertAll(
                    () -> assertTrue(isOn),
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, times(1)).off(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, atLeast(2)).isOn()
            );
        }
        
        @Test
        @DisplayName("off() should maintain off state when device is already off")
        void offShouldMaintainOffStateWhenDeviceIsAlreadyOff() {
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            assertAll(
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, times(1)).off(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, atLeastOnce()).isOn()
            );
        }
        
        @Test
        @DisplayName("off() should not interact with the policy")
        void offShouldNotInteractWithPolicy() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            reset(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy);
            
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            assertAll(
                    () -> verifyNoInteractions(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off()
            );
        }
    }
    
    @Nested
    @DisplayName("Tests for device reset() method")
    class ResetMethodTests {
        
        @Test
        @DisplayName("reset() should turn device off and reset policy")
        void resetShouldTurnDeviceOffAndResetPolicy() {
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            
            boolean isOn = DeviceAndPolicyIntegrationTest.this.device.isOn();
            
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            assertAll(
                    () -> assertTrue(isOn),
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).reset(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off()
            );
        }
        
        @Test
        @DisplayName("reset() should reset policy even when device is already off")
        void resetShouldResetPolicyEvenWhenDeviceIsAlreadyOff() {
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            assertAll(
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).reset(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off()
            );
        }
        
        @Test
        @DisplayName("reset() should call off() internally")
        void resetShouldCallOffInternally() {
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            assertAll(
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).reset()
            );
        }
    }
    
    @Test
    @DisplayName("isOn() should return correct state")
    void isOnShouldReturnCorrectState() {
        boolean actualFirstOnState = this.device.isOn();
        
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        boolean actualSecondOnState = this.device.isOn();
        
        this.device.off();
        boolean actualThirdOnState = this.device.isOn();
        
        assertAll(
                () -> assertFalse(actualFirstOnState),
                () -> assertTrue(actualSecondOnState),
                () -> assertFalse(actualThirdOnState),
                () -> verify(this.device, times(3)).isOn(),
                () -> verify(this.device, times(1)).on(),
                () -> verify(this.device, times(1)).off()
        );
    }
    
    @Test
    @DisplayName("toString() should include policy name and on state")
    void toStringShouldIncludePolicyNameAndOnState() {
        when(this.mockFailingPolicy.policyName()).thenReturn("TestPolicy");
        
        String actualStandardDeviceString = this.device.toString();
        
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        
        assertAll(
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=false}", actualStandardDeviceString),
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=true}", this.device.toString()),
                () -> verify(this.device, times(2)).toString(),
                () -> verify(this.device, times(1)).on(),
                () -> verify(this.mockFailingPolicy, times(2)).policyName()
        );
    }
}
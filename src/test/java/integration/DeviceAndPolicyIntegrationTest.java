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
            // Arrange
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            DeviceAndPolicyIntegrationTest.this.device.on();
            
            // Assert
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
            // Arrange
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            DeviceAndPolicyIntegrationTest.this.device.on();
            DeviceAndPolicyIntegrationTest.this.device.on(); // Second call
            
            // Assert
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
            // Arrange
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            boolean isOn = DeviceAndPolicyIntegrationTest.this.device.isOn();
            
            // Act
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            // Assert
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
            // Act
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            // Assert
            assertAll(
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, times(1)).off(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device, atLeastOnce()).isOn()
            );
        }
        
        @Test
        @DisplayName("off() should not interact with the policy")
        void offShouldNotInteractWithPolicy() {
            // Arrange
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            reset(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy); // Clear previous interactions
            
            // Act
            DeviceAndPolicyIntegrationTest.this.device.off();
            
            // Assert
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
            // Arrange
            when(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceAndPolicyIntegrationTest.this.device.on();
            
            boolean isOn = DeviceAndPolicyIntegrationTest.this.device.isOn();
            
            // Act
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            // Assert
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
            // Act
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            // Assert
            assertAll(
                    () -> assertFalse(DeviceAndPolicyIntegrationTest.this.device.isOn()),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).reset(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off()
            );
        }
        
        @Test
        @DisplayName("reset() should call off() internally")
        void resetShouldCallOffInternally() {
            // Act
            DeviceAndPolicyIntegrationTest.this.device.reset();
            
            // Assert
            assertAll(
                    () -> verify(DeviceAndPolicyIntegrationTest.this.device).off(),
                    () -> verify(DeviceAndPolicyIntegrationTest.this.mockFailingPolicy).reset()
            );
        }
    }
    
    @Test
    @DisplayName("isOn() should return correct state")
    void isOnShouldReturnCorrectState() {
        // Initially off
        boolean actualFirstOnState = this.device.isOn();
        
        // When turned on
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        boolean actualSecondOnState = this.device.isOn();
        
        // When turned off
        this.device.off();
        boolean actualThirdOnState = this.device.isOn();
        
        // Verify method calls
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
        // Arrange
        when(this.mockFailingPolicy.policyName()).thenReturn("TestPolicy");
        
        // Act & Assert - Initially off
        String actualStandardDeviceString = this.device.toString();
        
        // Turn on and check again
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        
        // Assert - After turning on
        assertAll(
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=false}", actualStandardDeviceString),
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=true}", this.device.toString()),
                () -> verify(this.device, times(2)).toString(),
                () -> verify(this.device, times(1)).on(),
                () -> verify(this.mockFailingPolicy, times(2)).policyName()
        );
    }
}
# Lab 02 - R&D Task 04 "TESTING-LLM"

In this task it is required to write unit and integration tests using LLMs.
In order to do that, I will use the same prompt for each LLM to see if they help me in writing / complete tests based on
their answers.

I will be considering those LLMs:
- ChatGPT
- Gemini
- Perplexity
- Claude

Each LLM will be asked the following questions:

Q:

```text
Considering integration testing in java using mockito and junit, i need you to generate integration tests considering this StandardDevice class and a FailingPolicy interface, make sure to generate enough integration tests to make sure that the classes are tested completely, not missing anything: "package devices; 

import java.util.Objects;

public class StandardDevice implements Device {
    private FailingPolicy failingPolicy;
    private boolean on = false;

    public StandardDevice(FailingPolicy failingPolicy) {
        this.failingPolicy = Objects.requireNonNull(failingPolicy);
    }
    //...

    @Override
    public void on() throws IllegalStateException {
        if (!this.failingPolicy.attemptOn()){
            throw new IllegalStateException();
        }
        this.on = true;
    }

    @Override
    public void off() {
        this.on = false;
    }

    @Override
    public boolean isOn() {
        return this.on;
    }

    @Override
    public void reset() {
        this.off();
        this.failingPolicy.reset();
    }

    @Override
    public String toString() {
        return "StandardDevice{" +
                "policy=" + failingPolicy.policyName() +
                ", on=" + on +
                '}';
    }
}
" "package devices;

public interface FailingPolicy {
    boolean attemptOn();
    void reset();
    String policyName();
}
"
```

those are the answers:

ChatGPT:
```java
package devices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StandardDeviceTest {
    private FailingPolicy failingPolicy;
    private StandardDevice device;

    @BeforeEach
    void setUp() {
        failingPolicy = mock(FailingPolicy.class);
        device = new StandardDevice(failingPolicy);
    }

    @Test
    void shouldTurnOnWhenPolicyAllows() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        
        device.on();
        
        assertTrue(device.isOn(), "Device should be on when policy allows");
    }

    @Test
    void shouldNotTurnOnWhenPolicyDenies() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        
        assertThrows(IllegalStateException.class, device::on, "Should throw when policy denies");
        assertFalse(device.isOn(), "Device should remain off");
    }

    @Test
    void shouldTurnOffWhenRequested() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        
        device.on();
        device.off();
        
        assertFalse(device.isOn(), "Device should be off after calling off()");
    }

    @Test
    void shouldResetDeviceProperly() {
        device.reset();
        
        assertFalse(device.isOn(), "Device should be off after reset");
        verify(failingPolicy, times(1)).reset();
    }

    @Test
    void shouldReturnProperToString() {
        when(failingPolicy.policyName()).thenReturn("TestPolicy");
        
        String expected = "StandardDevice{policy=TestPolicy, on=false}";
        assertEquals(expected, device.toString(), "toString() should return correct format");
    }
}
```

ChatGPT has generated a test class that tests the StandardDevice class with the FailingPolicy interface. 
The tests cover all the methods in the StandardDevice class and check if they behave as expected when the FailingPolicy
allows or denies the operation.
However, the tests do not use the 'assertAll' method to group multiple assertions in a single test, resulting in multiple
assertions in a single test method.

Gemini:
```java
package devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandardDeviceIntegrationTest {

    private FailingPolicy failingPolicy;
    private StandardDevice standardDevice;

    @BeforeEach
    void setUp() {
        failingPolicy = Mockito.mock(FailingPolicy.class);
        standardDevice = new StandardDevice(failingPolicy);
    }

    @Test
    void testOn_SuccessfulAttempt() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        verify(failingPolicy, times(1)).attemptOn();
    }

    @Test
    void testOn_FailingAttempt() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        assertThrows(IllegalStateException.class, standardDevice::on);
        assertFalse(standardDevice.isOn());
        verify(failingPolicy, times(1)).attemptOn();
    }

    @Test
    void testOff() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        standardDevice.off();
        assertFalse(standardDevice.isOn());
    }

    @Test
    void testIsOn_InitiallyOff() {
        assertFalse(standardDevice.isOn());
    }

    @Test
    void testIsOn_AfterOn() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
    }

    @Test
    void testReset() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        standardDevice.reset();
        assertFalse(standardDevice.isOn());
        verify(failingPolicy, times(1)).reset();
    }

    @Test
    void testToString() {
        when(failingPolicy.policyName()).thenReturn("TestPolicy");
        String expected = "StandardDevice{policy=TestPolicy, on=false}";
        assertEquals(expected, standardDevice.toString());
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        expected = "StandardDevice{policy=TestPolicy, on=true}";
        assertEquals(expected, standardDevice.toString());
    }

    @Test
    void testConstructor_NullFailingPolicy() {
        assertThrows(NullPointerException.class, () -> new StandardDevice(null));
    }

    @Test
    void testOn_MultipleSuccessfulAttempts() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        standardDevice.off();
        assertFalse(standardDevice.isOn());
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        verify(failingPolicy, times(2)).attemptOn();
    }

    @Test
    void testOn_AlternatingAttempts() {
        when(failingPolicy.attemptOn()).thenReturn(true, false, true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        standardDevice.off();
        assertFalse(standardDevice.isOn());
        assertThrows(IllegalStateException.class, standardDevice::on);
        assertFalse(standardDevice.isOn());
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        verify(failingPolicy, times(3)).attemptOn();
    }

    @Test
    void testReset_MultipleTimes() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
        standardDevice.reset();
        assertFalse(standardDevice.isOn());
        standardDevice.reset();
        assertFalse(standardDevice.isOn());
        verify(failingPolicy, times(2)).reset();
    }
}
```

Gemini has generated more tests than ChatGPT, covering more edge cases and scenarios. The tests are well-structured and
cover all the methods in the StandardDevice class.
However, just like ChatGPT, it does not use the 'assertAll' method to group multiple assertions in a single test, 
resulting in multiple assertions in a single test method.

Perplexity:
```java
 package devices;

 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.mockito.Mockito;

 import static org.junit.jupiter.api.Assertions.*;
 import static org.mockito.Mockito.*;

 public class StandardDeviceIntegrationTest {

  private FailingPolicy failingPolicy;
  private StandardDevice device;

  @BeforeEach
  void setUp() {
  failingPolicy = Mockito.mock(FailingPolicy.class);
  device = new StandardDevice(failingPolicy);
  }

  @Test
  void testDeviceTurnsOnSuccessfullyWhenPolicyAllows() {
  when(failingPolicy.attemptOn()).thenReturn(true);
  device.on();
  assertTrue(device.isOn());
  verify(failingPolicy, times(1)).attemptOn();
  }

  @Test
  void testDeviceThrowsExceptionWhenPolicyForbidsTurningOn() {
  when(failingPolicy.attemptOn()).thenReturn(false);
  assertThrows(IllegalStateException.class, () -> device.on());
  assertFalse(device.isOn());
  verify(failingPolicy, times(1)).attemptOn();
  }

  @Test
  void testDeviceTurnsOffSuccessfully() {
  when(failingPolicy.attemptOn()).thenReturn(true);
  device.on();
  assertTrue(device.isOn());
  device.off();
  assertFalse(device.isOn());
  }

  @Test
  void testIsOffReturnsCorrectState() {
  assertFalse(device.isOn()); // Initially off
  when(failingPolicy.attemptOn()).thenReturn(true);
  device.on();
  assertTrue(device.isOn());
  device.off();
  assertFalse(device.isOn());
  }

  @Test
  void testResetTurnsOffDeviceAndResetsPolicy() {
  when(failingPolicy.attemptOn()).thenReturn(true);
  device.on();
  assertTrue(device.isOn());
  device.reset();
  assertFalse(device.isOn());
  verify(failingPolicy, times(1)).reset();
  }

  @Test
  void testToStringContainsPolicyNameAndOnState() {
  when(failingPolicy.policyName()).thenReturn("MockPolicy");
  String toString = device.toString();
  assertTrue(toString.contains("policy=MockPolicy"));
  assertTrue(toString.contains("on=false") || toString.contains("on=true")); // Accounts for possible state change
  }

  @Test
  void testMultipleOnAttemptsWithPolicyBehavior() {
  // Simulate a policy that allows one on attempt then fails
  when(failingPolicy.attemptOn()).thenReturn(true, false);

  device.on();
  assertTrue(device.isOn());

  assertThrows(IllegalStateException.class, () -> device.on());
  assertTrue(device.isOn()); // Should remain on from the successful attempt

  verify(failingPolicy, times(2)).attemptOn();
  }

  @Test
  void testResettingAnAlreadyOffDevice() {
  device.reset();
  assertFalse(device.isOn());
  verify(failingPolicy, times(1)).reset();
  }

  @Test
  void testOnThrowsExceptionOnlyOnce() {
  when(failingPolicy.attemptOn()).thenReturn(false);
  assertThrows(IllegalStateException.class, () -> device.on());
  assertThrows(IllegalStateException.class, () -> device.on()); // Calling it again
  verify(failingPolicy, times(2)).attemptOn(); // Verify policy is still consulted
  }

  @Test
  void testPolicyMethodsAreCalledCorrectNumberOfTimes() {
  when(failingPolicy.attemptOn()).thenReturn(true);
  device.on();
  device.off();
  device.reset();

  verify(failingPolicy, times(1)).attemptOn();
  verify(failingPolicy, times(1)).reset();
  verify(failingPolicy, times(1)).policyName(); // toString is called which uses policyName
  }
 }
```

Perplexity has generated a test class that tests the StandardDevice class with the FailingPolicy interface. The tests
cover all the methods in the StandardDevice class and check if they behave as expected when the FailingPolicy allows or
denies the operation. The tests are well-structured and cover more edge cases and scenarios than previous ChatGPT.
However, just like the previous LLMs, it does not use the 'assertAll' method to group multiple assertions in a single
test, resulting in multiple assertions in a single test method.

Claude:
```java
package devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandardDeviceIntegrationTest {
    
    @Mock
    private FailingPolicy mockFailingPolicy;
    
    private Device device;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.device = new StandardDevice(this.mockFailingPolicy);
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
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            DeviceWithPolicyTest.this.device.on();
            
            // Assert
            assertTrue(DeviceWithPolicyTest.this.device.isOn());
            verify(DeviceWithPolicyTest.this.mockFailingPolicy).attemptOn();
        }
        
        @Test
        @DisplayName("on() should throw IllegalStateException when policy denies")
        void onShouldThrowExceptionWhenPolicyDenies() {
            // Arrange
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(false);
            
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> DeviceWithPolicyTest.this.device.on());
            assertFalse(DeviceWithPolicyTest.this.device.isOn());
            verify(DeviceWithPolicyTest.this.mockFailingPolicy).attemptOn();
        }
        
        @Test
        @DisplayName("on() should be callable multiple times when policy allows")
        void onShouldBeCallableMultipleTimesWhenPolicyAllows() {
            // Arrange
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            DeviceWithPolicyTest.this.device.on();
            DeviceWithPolicyTest.this.device.on(); // Second call
            
            // Assert
            assertTrue(DeviceWithPolicyTest.this.device.isOn());
            verify(DeviceWithPolicyTest.this.mockFailingPolicy, times(2)).attemptOn();
        }
    }
    
    @Nested
    @DisplayName("Tests for device off() method")
    class OffMethodTests {
        
        @Test
        @DisplayName("off() should set device to off state when device is on")
        void offShouldSetDeviceOffWhenDeviceIsOn() {
            // Arrange
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceWithPolicyTest.this.device.on();
            assertTrue(DeviceWithPolicyTest.this.device.isOn());
            
            // Act
            DeviceWithPolicyTest.this.device.off();
            
            // Assert
            assertFalse(DeviceWithPolicyTest.this.device.isOn());
        }
        
        @Test
        @DisplayName("off() should maintain off state when device is already off")
        void offShouldMaintainOffStateWhenDeviceIsAlreadyOff() {
            // Act
            DeviceWithPolicyTest.this.device.off();
            
            // Assert
            assertFalse(DeviceWithPolicyTest.this.device.isOn());
        }
        
        @Test
        @DisplayName("off() should not interact with the policy")
        void offShouldNotInteractWithPolicy() {
            // Arrange
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceWithPolicyTest.this.device.on();
            reset(DeviceWithPolicyTest.this.mockFailingPolicy); // Clear previous interactions
            
            // Act
            DeviceWithPolicyTest.this.device.off();
            
            // Assert
            verifyNoInteractions(DeviceWithPolicyTest.this.mockFailingPolicy);
        }
    }
    
    @Nested
    @DisplayName("Tests for device reset() method")
    class ResetMethodTests {
        
        @Test
        @DisplayName("reset() should turn device off and reset policy")
        void resetShouldTurnDeviceOffAndResetPolicy() {
            // Arrange
            when(DeviceWithPolicyTest.this.mockFailingPolicy.attemptOn()).thenReturn(true);
            DeviceWithPolicyTest.this.device.on();
            assertTrue(DeviceWithPolicyTest.this.device.isOn());
            
            // Act
            DeviceWithPolicyTest.this.device.reset();
            
            // Assert
            assertFalse(DeviceWithPolicyTest.this.device.isOn());
            verify(DeviceWithPolicyTest.this.mockFailingPolicy).reset();
        }
        
        @Test
        @DisplayName("reset() should reset policy even when device is already off")
        void resetShouldResetPolicyEvenWhenDeviceIsAlreadyOff() {
            // Act
            DeviceWithPolicyTest.this.device.reset();
            
            // Assert
            assertFalse(DeviceWithPolicyTest.this.device.isOn());
            verify(DeviceWithPolicyTest.this.mockFailingPolicy).reset();
        }
    }
    
    @Test
    @DisplayName("isOn() should return correct state")
    void isOnShouldReturnCorrectState() {
        // Initially off
        assertFalse(this.device.isOn());
        
        // When turned on
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        assertTrue(this.device.isOn());
        
        // When turned off
        this.device.off();
        assertFalse(this.device.isOn());
    }
    
    @Test
    @DisplayName("toString() should include policy name and on state")
    void toStringShouldIncludePolicyNameAndOnState() {
        // Arrange
        when(this.mockFailingPolicy.policyName()).thenReturn("TestPolicy");
        
        // Act & Assert
        assertEquals("StandardDevice{policy=TestPolicy, on=false}", this.device.toString());
        
        // Turn on and check again
        when(this.mockFailingPolicy.attemptOn()).thenReturn(true);
        this.device.on();
        assertEquals("StandardDevice{policy=TestPolicy, on=true}", this.device.toString());
    }
}
```

Claude has generated the most complete and accurate test class among all the LLMs. The tests are well-structured and
cover all the methods in the StandardDevice class. The tests are grouped into nested classes based on the method they
are testing, making it easier to understand and maintain the test suite.
However, just like the previous LLMs, it does not use the 'assertAll' method to group multiple assertions in a single
test, resulting in multiple assertions in a single test method.

Since none of the LLMs have used the 'assertAll' method, i asked them a follow-up question to refactor the tests to use
it in each test where there are multiple assertions, in order to help me to speed up the process of writing the tests.

Q:

```text
refactor it to use assertAll method in each test where you have multiple asserts
```

those are the answers:

ChatGPT:
```java
package devices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StandardDeviceTest {
    private FailingPolicy failingPolicy;
    private StandardDevice device;

    @BeforeEach
    void setUp() {
        failingPolicy = mock(FailingPolicy.class);
        device = new StandardDevice(failingPolicy);
    }

    @Test
    void shouldTurnOnWhenPolicyAllows() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        
        device.on();
        
        assertAll(
            "Device should be on when policy allows",
            () -> assertTrue(device.isOn(), "Device should be on")
        );
    }

    @Test
    void shouldNotTurnOnWhenPolicyDenies() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        
        assertAll(
            "Should throw when policy denies",
            () -> assertThrows(IllegalStateException.class, device::on, "Should throw when policy denies"),
            () -> assertFalse(device.isOn(), "Device should remain off")
        );
    }

    @Test
    void shouldTurnOffWhenRequested() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        
        device.on();
        device.off();
        
        assertAll(
            "Device should be off after calling off()",
            () -> assertFalse(device.isOn(), "Device should be off")
        );
    }

    @Test
    void shouldResetDeviceProperly() {
        device.reset();
        
        assertAll(
            "Device should be off after reset",
            () -> assertFalse(device.isOn(), "Device should be off"),
            () -> verify(failingPolicy, times(1)).reset()
        );
    }

    @Test
    void shouldReturnProperToString() {
        when(failingPolicy.policyName()).thenReturn("TestPolicy");
        
        String expected = "StandardDevice{policy=TestPolicy, on=false}";
        
        assertAll(
            "toString() should return correct format",
            () -> assertEquals(expected, device.toString(), "toString() should match expected format")
        );
    }
}
```

ChatGPT has refactored the tests to use the 'assertAll' method in each test, also the ones that did not have multiple
assertions. But definitely speeded up the process of writing tests.

Gemini:
```java
package devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandardDeviceIntegrationTest {

    private FailingPolicy failingPolicy;
    private StandardDevice standardDevice;

    @BeforeEach
    void setUp() {
        failingPolicy = Mockito.mock(FailingPolicy.class);
        standardDevice = new StandardDevice(failingPolicy);
    }

    @Test
    void testOn_SuccessfulAttempt() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertAll("Successful on()",
                () -> assertTrue(standardDevice.isOn()),
                () -> verify(failingPolicy, times(1)).attemptOn()
        );
    }

    @Test
    void testOn_FailingAttempt() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        assertThrows(IllegalStateException.class, standardDevice::on);
        assertAll("Failing on()",
                () -> assertFalse(standardDevice.isOn()),
                () -> verify(failingPolicy, times(1)).attemptOn()
        );
    }

    @Test
    void testOff() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        standardDevice.off();
        assertFalse(standardDevice.isOn());
    }

    @Test
    void testIsOn_InitiallyOff() {
        assertFalse(standardDevice.isOn());
    }

    @Test
    void testIsOn_AfterOn() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        assertTrue(standardDevice.isOn());
    }

    @Test
    void testReset() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        standardDevice.reset();
        assertAll("Reset",
                () -> assertFalse(standardDevice.isOn()),
                () -> verify(failingPolicy, times(1)).reset()
        );
    }

    @Test
    void testToString() {
        when(failingPolicy.policyName()).thenReturn("TestPolicy");
        String expectedOff = "StandardDevice{policy=TestPolicy, on=false}";
        String expectedOn = "StandardDevice{policy=TestPolicy, on=true}";
        assertAll("toString()",
                () -> assertEquals(expectedOff, standardDevice.toString()),
                () -> {
                    when(failingPolicy.attemptOn()).thenReturn(true);
                    standardDevice.on();
                    assertEquals(expectedOn, standardDevice.toString());
                }
        );
    }

    @Test
    void testConstructor_NullFailingPolicy() {
        assertThrows(NullPointerException.class, () -> new StandardDevice(null));
    }

    @Test
    void testOn_MultipleSuccessfulAttempts() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        standardDevice.off();
        standardDevice.on();
        assertAll("Multiple successful on()",
                () -> assertTrue(standardDevice.isOn()),
                () -> verify(failingPolicy, times(2)).attemptOn()
        );
    }

    @Test
    void testOn_AlternatingAttempts() {
        when(failingPolicy.attemptOn()).thenReturn(true, false, true);
        standardDevice.on();
        standardDevice.off();
        standardDevice.on();
        assertAll("Alternating on()",
                () -> assertTrue(standardDevice.isOn()),
                () -> verify(failingPolicy, times(3)).attemptOn()
        );
    }

    @Test
    void testReset_MultipleTimes() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        standardDevice.on();
        standardDevice.reset();
        standardDevice.reset();
        assertAll("Multiple reset()",
                () -> assertFalse(standardDevice.isOn()),
                () -> verify(failingPolicy, times(2)).reset()
        );
    }
}
```

Gemini has refactored the tests to use the 'assertAll' method in each test where there are multiple assertions, 
correctly excluding the ones that did not have multiple assertions. This code can be used without any more human
intervention.

Perplexity:
```java
package devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StandardDeviceIntegrationTest {

    private FailingPolicy failingPolicy;
    private StandardDevice device;

    @BeforeEach
    void setUp() {
        failingPolicy = Mockito.mock(FailingPolicy.class);
        device = new StandardDevice(failingPolicy);
    }

    @Test
    void testDeviceTurnsOnSuccessfullyWhenPolicyAllows() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        device.on();
        assertAll(
                () -> assertTrue(device.isOn()),
                () -> verify(failingPolicy, times(1)).attemptOn()
        );
    }

    @Test
    void testDeviceThrowsExceptionWhenPolicyForbidsTurningOn() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> device.on()),
                () -> assertFalse(device.isOn()),
                () -> verify(failingPolicy, times(1)).attemptOn()
        );
    }

    @Test
    void testDeviceTurnsOffSuccessfully() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        device.on();
        device.off();
        assertAll(
                () -> assertFalse(device.isOn()),
                () -> verify(failingPolicy, times(1)).attemptOn()
        );
    }

    @Test
    void testIsOffReturnsCorrectState() {
        assertAll(
                () -> assertFalse(device.isOn()), // Initially off
                () -> {
                    when(failingPolicy.attemptOn()).thenReturn(true);
                    device.on();
                    assertTrue(device.isOn());
                },
                () -> {
                    device.off();
                    assertFalse(device.isOn());
                }
        );
    }

    @Test
    void testResetTurnsOffDeviceAndResetsPolicy() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        device.on();
        device.reset();
        assertAll(
                () -> assertFalse(device.isOn()),
                () -> verify(failingPolicy, times(1)).reset()
        );
    }

    @Test
    void testToStringContainsPolicyNameAndOnState() {
        when(failingPolicy.policyName()).thenReturn("MockPolicy");
        String toString = device.toString();
        assertAll(
                () -> assertTrue(toString.contains("policy=MockPolicy")),
                () -> assertTrue(toString.contains("on=false") || toString.contains("on=true"))
        );
    }

    @Test
    void testMultipleOnAttemptsWithPolicyBehavior() {
        // Simulate a policy that allows one on attempt then fails
        when(failingPolicy.attemptOn()).thenReturn(true, false);

        device.on();
        assertAll(
                () -> assertTrue(device.isOn()),
                () -> assertThrows(IllegalStateException.class, () -> device.on()),
                () -> verify(failingPolicy, times(2)).attemptOn()
        );
    }

    @Test
    void testResettingAnAlreadyOffDevice() {
        device.reset();
        assertAll(
                () -> assertFalse(device.isOn()),
                () -> verify(failingPolicy, times(1)).reset()
        );
    }

    @Test
    void testOnThrowsExceptionOnlyOnce() {
        when(failingPolicy.attemptOn()).thenReturn(false);
        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> device.on()),
                () -> assertThrows(IllegalStateException.class, () -> device.on()),
                () -> verify(failingPolicy, times(2)).attemptOn()
        );
    }

    @Test
    void testPolicyMethodsAreCalledCorrectNumberOfTimes() {
        when(failingPolicy.attemptOn()).thenReturn(true);
        device.on();
        device.off();
        device.reset();
        assertAll(
                () -> verify(failingPolicy, times(1)).attemptOn(),
                () -> verify(failingPolicy, times(1)).reset(),
                () -> verify(failingPolicy, times(1)).policyName() // toString is called which uses policyName
        );
    }
}
```

Perplexity has refactored the tests to use the 'assertAll' method in each test where there are multiple assertions,
correctly excluding the ones that did not have multiple assertions just like Gemini. 
The generated code can be used without any more human intervention.

Claude:
```java
package devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandardDeviceIntegrationTest {
    
    @Mock
    private FailingPolicy mockFailingPolicy;
    
    private StandardDevice device;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        device = new StandardDevice(mockFailingPolicy);
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
            when(mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            device.on();
            
            // Assert
            assertAll(
                    "Device should be on and policy should be called",
                    () -> assertTrue(device.isOn(), "Device should be turned on"),
                    () -> verify(mockFailingPolicy).attemptOn()
            );
        }
        
        @Test
        @DisplayName("on() should throw IllegalStateException when policy denies")
        void onShouldThrowExceptionWhenPolicyDenies() {
            // Arrange
            when(mockFailingPolicy.attemptOn()).thenReturn(false);
            
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> device.on(), "Should throw IllegalStateException when policy denies");
            
            assertAll(
                    "Device should remain off and policy should be called",
                    () -> assertFalse(device.isOn(), "Device should remain off"),
                    () -> verify(mockFailingPolicy).attemptOn()
            );
        }
        
        @Test
        @DisplayName("on() should be callable multiple times when policy allows")
        void onShouldBeCallableMultipleTimesWhenPolicyAllows() {
            // Arrange
            when(mockFailingPolicy.attemptOn()).thenReturn(true);
            
            // Act
            device.on();
            device.on(); // Second call
            
            // Assert
            assertAll(
                    "Device should be on and policy should be called twice",
                    () -> assertTrue(device.isOn(), "Device should be turned on"),
                    () -> verify(mockFailingPolicy, times(2)).attemptOn()
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
            when(mockFailingPolicy.attemptOn()).thenReturn(true);
            device.on();
            
            // Act
            device.off();
            
            // Assert
            assertAll(
                    "Device should be turned off after being on",
                    () -> assertTrue(device.isOn() == false, "Device should be off"),
                    () -> assertFalse(device.isOn(), "Device should be off (double check)")
            );
        }
        
        @Test
        @DisplayName("off() should maintain off state when device is already off")
        void offShouldMaintainOffStateWhenDeviceIsAlreadyOff() {
            // Act
            device.off();
            
            // Assert
            assertFalse(device.isOn(), "Device should remain off");
        }
        
        @Test
        @DisplayName("off() should not interact with the policy")
        void offShouldNotInteractWithPolicy() {
            // Arrange
            when(mockFailingPolicy.attemptOn()).thenReturn(true);
            device.on();
            reset(mockFailingPolicy); // Clear previous interactions
            
            // Act
            device.off();
            
            // Assert
            assertAll(
                    "Device should be off and policy should not be called",
                    () -> assertFalse(device.isOn(), "Device should be turned off"),
                    () -> verifyNoInteractions(mockFailingPolicy)
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
            when(mockFailingPolicy.attemptOn()).thenReturn(true);
            device.on();
            assertTrue(device.isOn(), "Device should be on before reset");
            
            // Act
            device.reset();
            
            // Assert
            assertAll(
                    "Device should be off and policy should be reset",
                    () -> assertFalse(device.isOn(), "Device should be turned off"),
                    () -> verify(mockFailingPolicy).reset()
            );
        }
        
        @Test
        @DisplayName("reset() should reset policy even when device is already off")
        void resetShouldResetPolicyEvenWhenDeviceIsAlreadyOff() {
            // Act
            device.reset();
            
            // Assert
            assertAll(
                    "Device should remain off and policy should be reset",
                    () -> assertFalse(device.isOn(), "Device should remain off"),
                    () -> verify(mockFailingPolicy).reset()
            );
        }
    }
    
    @Test
    @DisplayName("isOn() should return correct state")
    void isOnShouldReturnCorrectState() {
        // Initially off
        assertFalse(device.isOn(), "Device should initially be off");
        
        // When turned on
        when(mockFailingPolicy.attemptOn()).thenReturn(true);
        device.on();
        
        // When turned off
        device.off();
        
        // Each state is checked independently
        assertAll(
                "isOn() should report correct state throughout lifecycle",
                () -> {
                    // Reset and turn on again to check on state
                    when(mockFailingPolicy.attemptOn()).thenReturn(true);
                    device.on();
                    assertTrue(device.isOn(), "Device should report on state when turned on");
                },
                () -> {
                    // Turn off to check off state
                    device.off();
                    assertFalse(device.isOn(), "Device should report off state when turned off");
                }
        );
    }
    
    @Test
    @DisplayName("toString() should include policy name and on state")
    void toStringShouldIncludePolicyNameAndOnState() {
        // Arrange
        when(mockFailingPolicy.policyName()).thenReturn("TestPolicy");
        
        // Initial off state
        String offString = device.toString();
        
        // Turn on and get on state string
        when(mockFailingPolicy.attemptOn()).thenReturn(true);
        device.on();
        String onString = device.toString();
        
        // Assert
        assertAll(
                "toString() should correctly represent device state",
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=false}", offString,
                        "When off, toString should include off state"),
                () -> assertEquals("StandardDevice{policy=TestPolicy, on=true}", onString,
                        "When on, toString should include on state")
        );
    }
    
    @Nested
    @DisplayName("Integration with concrete FailingPolicy implementations")
    class ConcreteImplementationTests {
        
        @Test
        @DisplayName("Integration with AlwaysFailingPolicy")
        void integrationWithAlwaysFailingPolicy() {
            // Arrange
            FailingPolicy alwaysFailingPolicy = new AlwaysFailingPolicy();
            StandardDevice device = new StandardDevice(alwaysFailingPolicy);
            
            // Act & Assert initial state
            assertThrows(IllegalStateException.class, () -> device.on(),
                    "Should throw IllegalStateException when using AlwaysFailingPolicy");
            
            // Reset and try again
            device.reset();
            
            assertAll(
                    "Device should be off and still fail to turn on after reset",
                    () -> assertFalse(device.isOn(), "Device should be off"),
                    () -> assertThrows(IllegalStateException.class, () -> device.on(),
                            "Should still throw exception after reset")
            );
        }
        
        @Test
        @DisplayName("Integration with NeverFailingPolicy")
        void integrationWithNeverFailingPolicy() {
            // Arrange
            FailingPolicy neverFailingPolicy = new NeverFailingPolicy();
            StandardDevice device = new StandardDevice(neverFailingPolicy);
            
            // Act
            device.on();
            
            // First set of assertions
            assertTrue(device.isOn(), "Device should turn on with NeverFailingPolicy");
            
            // Reset and try again
            device.reset();
            device.on();
            
            // Assert after reset
            assertAll(
                    "Device should turn on after reset",
                    () -> assertTrue(device.isOn(), "Device should turn on after reset"),
                    () -> assertEquals("StandardDevice{policy=NeverFailingPolicy, on=true}",
                            device.toString(), "toString should show correct state")
            );
        }
        
        @Test
        @DisplayName("Integration with AlternatingFailingPolicy")
        void integrationWithAlternatingFailingPolicy() {
            // Arrange
            FailingPolicy alternatingPolicy = new AlternatingFailingPolicy();
            StandardDevice device = new StandardDevice(alternatingPolicy);
            
            // First attempt should succeed
            device.on();
            assertTrue(device.isOn(), "First attempt should succeed");
            
            // Turn off and try again - should fail
            device.off();
            
            assertAll(
                    "Second attempt should fail",
                    () -> assertThrows(IllegalStateException.class, () -> device.on(),
                            "Second attempt should throw IllegalStateException"),
                    () -> assertFalse(device.isOn(), "Device should remain off after failed attempt")
            );
            
            // Reset and try again - should succeed again
            device.reset();
            device.on();
            
            assertAll(
                    "After reset, first attempt should succeed again",
                    () -> assertTrue(device.isOn(), "Device should turn on after reset"),
                    () -> assertEquals("StandardDevice{policy=AlternatingFailingPolicy, on=true}",
                            device.toString(), "toString should show correct state")
            );
        }
    }
}
```

Claude gave the best overall answer, refactoring the tests to use the 'assertAll' method in each test where there are
multiple assertions, correctly excluding the ones that did not have multiple assertions, but occasionally did not group
assertions inside the 'assertAll' method when it could have been done, so this code needs some human intervention to
fix it, but definitely speeded up the process of writing tests.

## Conclusions:
Each model completed the task successfully, generating a test class that tests the StandardDevice class with the
FailingPolicy interface. The tests cover all the methods in the StandardDevice class and check if they behave as expected
when the FailingPolicy allows or denies the operation. The tests are well-structured and cover each method.

However, Claude has generated the most complete and accurate test class among all the LLMs, with well-structured tests
grouped into nested classes based on the method they are testing, making it easier to understand and maintain the test
suite. The tests are also refactored to use the 'assertAll' method in each test where there are multiple assertions,
correctly excluding the ones that did not have multiple assertions.
A small human intervention was needed to fix some of the tests that were not grouped inside the 'assertAll' method but
definitely speeded up the process of writing tests (Claude's code was used to generate the final test class).
```
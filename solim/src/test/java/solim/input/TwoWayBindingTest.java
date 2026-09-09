package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class TwoWayBindingTest {

	@Test
	void signalUpdatesWidget() {
		Signal<String> sig = Signal.of("initial");
		String[] widgetVal = new String[] {"initial"};
		AtomicInteger setterCalls = new AtomicInteger(0);

		TwoWayBinding<String> binding = new TwoWayBinding<>(
				sig,
				() -> widgetVal[0],
				val -> {
					widgetVal[0] = val;
					setterCalls.incrementAndGet();
				},
				onChange -> () -> {}
		);

		assertEquals(0, setterCalls.get(), "Initial equal value should not trigger setter");

		sig.set("updated");
		assertEquals("updated", widgetVal[0]);
		assertEquals(1, setterCalls.get());

		binding.dispose();
	}

	@Test
	void widgetUpdatesSignalWithoutFeedbackLoop() {
		Signal<Integer> sig = Signal.of(10);
		int[] widgetVal = new int[] {10};
		AtomicInteger setterCalls = new AtomicInteger(0);
		Runnable[] trigger = new Runnable[1];

		TwoWayBinding<Integer> binding = new TwoWayBinding<>(
				sig,
				() -> widgetVal[0],
				val -> {
					widgetVal[0] = val;
					setterCalls.incrementAndGet();
				},
				onChange -> {
					trigger[0] = onChange;
					return () -> {};
				}
		);

		// Simulate widget user interaction
		widgetVal[0] = 42;
		trigger[0].run();

		assertEquals(42, sig.get());
		// Widget setter should NOT have been called when widget drove the change
		assertEquals(0, setterCalls.get(), "Widget change should not loop back to widget setter");

		binding.dispose();
	}

	@Test
	void disposalCleansUp() {
		Signal<String> sig = Signal.of("A");
		String[] widgetVal = new String[] {"A"};
		AtomicBoolean listenerRemoved = new AtomicBoolean(false);

		TwoWayBinding<String> binding = new TwoWayBinding<>(
				sig,
				() -> widgetVal[0],
				val -> widgetVal[0] = val,
				onChange -> () -> listenerRemoved.set(true)
		);

		assertFalse(binding.isDisposed());
		binding.dispose();
		assertTrue(binding.isDisposed());
		assertTrue(listenerRemoved.get(), "Widget listener must be cleaned up on disposal");

		sig.set("B");
		assertEquals("A", widgetVal[0], "Disposed binding should not propagate signal changes");
	}
}

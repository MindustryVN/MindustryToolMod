package solim.overlay;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.core.Component;
import solim.feedback.Alert;
import solim.feedback.Avatar;
import solim.feedback.Badge;
import solim.feedback.ProgressBar;
import solim.feedback.Spinner;
import solim.signal.Computed;
import solim.signal.Signal;

class OverlayFeedbackTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void dialogShowHideDispose() {
		SolimDialog d = new SolimDialog("Test");
		assertFalse(d.isShown());
		d.show();
		assertTrue(d.isShown());
		d.hide();
		assertFalse(d.isShown());
		d.dispose();
	}

	@Test
	void dialogContentComponentAutomaticallyDisposed() {
		AtomicBoolean disposed = new AtomicBoolean(false);
		Component testComp = new Component() {
			@Override
			public Element element() {
				return new Table();
			}

			@Override
			public void dispose() {
				disposed.set(true);
			}
		};

		SolimDialog d = new SolimDialog("Lifecycle Test");
		d.content(testComp);
		assertFalse(disposed.get());

		d.dispose();
		assertTrue(d.isDisposed());
		assertTrue(disposed.get(), "Attached content component must be disposed when dialog is disposed");
	}

	@Test
	void dialogDefaultsToFullScreen() {
		SolimDialog d = new SolimDialog("Full Screen Test");
		assertTrue(d.isFillParent(), "SolimDialog must default to full screen (fillParent == true)");
		d.fillParent(false);
		assertFalse(d.isFillParent(), "fillParent(false) must allow disabling full screen");
		d.dispose();
	}

	public static class TestDialogEvent {
		public final int code;

		public TestDialogEvent(int code) {
			this.code = code;
		}
	}

	@Test
	void dialogSignalCreationAndEventRecalculation() {
		SolimDialog d = new SolimDialog("Test");
		int[] counter = new int[] {10};
		java.util.List<Runnable> resizeCallbacks = new java.util.ArrayList<>();

		// Test callback-based signal creation (e.g. this.resized(callback))
		Signal<Integer> resizeSignal = d.createSignal(cb -> {
			resizeCallbacks.add(cb);
			return () -> resizeCallbacks.remove(cb);
		}, () -> counter[0]);
		assertEquals(10, resizeSignal.get());

		counter[0] = 25;
		for (Runnable r : resizeCallbacks) r.run();
		assertEquals(25, resizeSignal.get());

		// Test event-based signal creation (Events.on)
		Signal<Integer> eventSignal = d.createSignal(TestDialogEvent.class, () -> counter[0] * 2);
		assertEquals(50, eventSignal.get());

		counter[0] = 30;
		Events.fire(new TestDialogEvent(1));
		assertEquals(60, eventSignal.get());

		// Test event-mapping signal creation
		Signal<Integer> mappedSignal = d.createSignal(TestDialogEvent.class, e -> e.code * 100, 0);
		assertEquals(0, mappedSignal.get());
		Events.fire(new TestDialogEvent(5));
		assertEquals(500, mappedSignal.get());

		// Test disposal cleans up listeners
		d.dispose();
		assertTrue(d.isDisposed());
		assertTrue(resizeCallbacks.isEmpty(), "Callback subscription must be unregistered on dispose");

		counter[0] = 999;
		Events.fire(new TestDialogEvent(99));
		// After disposal, event listener was unregistered so eventSignal should not recalculate
		assertEquals(60, eventSignal.get());
		assertEquals(500, mappedSignal.get());
	}

	@Test
	void progressBarReactive() {
		Signal<Float> progress = Signal.of(0.3f);
		ProgressBar pb = new ProgressBar(progress);
		progress.set(0.7f);
		assertNotNull(pb.bar());
		pb.dispose();
	}

	@Test
	void alertDismiss() {
		Alert a = new Alert("Saved!", Alert.Type.SUCCESS);
		assertTrue(a.isShown());
		a.dismiss();
		assertFalse(a.isShown());
	}

	@Test
	void badgeReactiveCount() {
		Signal<Integer> count = Signal.of(5);
		Computed<String> text = count.map(v -> v > 99 ? "99+" : String.valueOf(v));
		Badge b = Badge.of(text);
		assertNotNull(b.table());
		count.set(50);
		text.get();
		b.dispose();
	}

	@Test
	void spinnerExists() {
		Spinner s = new Spinner();
		assertNotNull(s.label());
	}

	@Test
	void avatarExists() {
		Avatar a = new Avatar();
		assertNotNull(a.table());
	}

	@Test
	void popupExists() {
		Popup p = new Popup();
		assertNotNull(p.table());
	}
}

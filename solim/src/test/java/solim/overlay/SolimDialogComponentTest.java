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
import solim.signal.Signal;

class SolimDialogComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void dialogCreatesWithDefaultName() {
		SolimDialog d = new SolimDialog("Test");
		assertEquals("solim-dialog-dialog", d.name);
		assertSame(d, d.element());
		d.dispose();
	}

	@Test
	void dialogShowHideLifecycle() {
		SolimDialog d = new SolimDialog("Test");
		assertFalse(d.isShown());
		d.show();
		assertTrue(d.isShown());
		d.hide();
		assertFalse(d.isShown());
		d.dispose();
	}

	@Test
	void dialogDisposeMarksDisposed() {
		SolimDialog d = new SolimDialog("Test");
		assertFalse(d.isDisposed());
		d.dispose();
		assertTrue(d.isDisposed());
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
		assertTrue(disposed.get(), "Attached content component must be disposed when dialog is disposed");
	}

	@Test
	void dialogDefaultsToFullScreen() {
		SolimDialog d = new SolimDialog("Full Screen Test");
		assertTrue(d.isFillParent(), "SolimDialog must default to full screen");
		d.fillParent(false);
		assertFalse(d.isFillParent());
		d.dispose();
	}

	@Test
	void dialogSignalCreationAndEventRecalculation() {
		SolimDialog d = new SolimDialog("Test");
		int[] counter = new int[] {10};
		java.util.List<Runnable> resizeCallbacks = new java.util.ArrayList<>();

		solim.signal.Signal<Integer> resizeSignal = d.createSignal(cb -> {
			resizeCallbacks.add(cb);
			return () -> resizeCallbacks.remove(cb);
		}, () -> counter[0]);
		assertEquals(10, resizeSignal.get());

		counter[0] = 25;
		for (Runnable r : resizeCallbacks) r.run();
		assertEquals(25, resizeSignal.get());

		d.dispose();
		assertTrue(d.isDisposed());
	}

	@Test
	void dialogFluentApiReturnsSameInstance() {
		SolimDialog d = new SolimDialog("Test");
		assertSame(d, d.fillParent(false));
		assertTrue(d.isFillParent() == false);
		assertSame(d, d.fillParent(true));
		d.dispose();
	}
}

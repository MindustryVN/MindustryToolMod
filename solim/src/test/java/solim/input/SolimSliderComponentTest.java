package solim.input;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class SolimSliderComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void sliderCreatesElement() {
		SolimSlider sl = new SolimSlider(Signal.of(0f), 0f, 1f, 0.1f);
		assertNotNull(sl.element());
		assertNotNull(sl.slider());
		sl.dispose();
	}

	@Test
	void sliderFloatBinding() {
		Signal<Float> v = Signal.of(0.3f);
		SolimSlider sl = new SolimSlider(v, 0f, 1f, 0.1f);
		sl.slider().setValue(0.7f);
		assertEquals(0.7f, v.get(), 0.0001f);
		v.set(0.2f);
		assertEquals(0.2f, sl.slider().getValue(), 0.0001f);
		sl.dispose();
	}

	@Test
	void sliderIntegerBinding() {
		Signal<Integer> cols = Signal.of(3);
		SolimSlider sl = new SolimSlider(cols, 1, 9, 1);
		sl.slider().setValue(6f);
		assertEquals(6, cols.get().intValue());
		cols.set(4);
		assertEquals(4f, sl.slider().getValue(), 0.0001f);
		sl.dispose();
	}

	@Test
	void sliderNameModifier() {
		SolimSlider sl = new SolimSlider(Signal.of(0f), 0f, 1f, 0.1f).name("sl");
		assertEquals("sl", sl.element().name);
		sl.dispose();
	}

	@Test
	void sliderStaticFactoryFloat() {
		SolimSlider sl = SolimSlider.of(Signal.of(0.5f), 0f, 1f, 0.1f);
		assertNotNull(sl.slider());
		sl.dispose();
	}

	@Test
	void sliderStaticFactoryInteger() {
		SolimSlider sl = SolimSlider.of(Signal.of(5), 1, 10, 1);
		assertNotNull(sl.slider());
		sl.dispose();
	}

	@Test
	void sliderImplementsComponent() {
		SolimSlider sl = new SolimSlider(Signal.of(0f), 0f, 1f, 0.1f);
		assertInstanceOf(solim.core.Component.class, sl);
		sl.dispose();
	}
}

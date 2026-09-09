package solim.display;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.graphics.Color;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import solim.signal.Signal;

class TextComponentTest {

	@BeforeAll
	static void checkArcContext() {
		Assumptions.assumeTrue(Core.scene != null, "Arc Core.scene is null; skipping skin-dependent tests");
	}

	@Test
	void textCreatesLabel() {
		Text t = new Text("Hello");
		assertNotNull(t.element());
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textStaticValue() {
		Text t = new Text("Hello");
		assertEquals("Hello", t.label().getText().toString());
		t.dispose();
	}

	@Test
	void textEmptyDefault() {
		Text t = new Text();
		assertEquals("", t.label().getText().toString());
		t.dispose();
	}

	@Test
	void textStaticFactory() {
		Text t = Text.of("Hello");
		assertEquals("Hello", t.label().getText().toString());
		t.dispose();
	}

	@Test
	void textReactiveBinding() {
		Signal<String> s = Signal.of("a");
		Text t = Text.of(s);
		assertEquals("a", t.label().getText().toString());
		s.set("b");
		assertEquals("b", t.label().getText().toString());
		t.dispose();
	}

	@Test
	void textDisposeStopsBinding() {
		Signal<String> s = Signal.of("a");
		Text t = Text.of(s);
		assertEquals("a", t.label().getText().toString());
		t.dispose();
		s.set("b");
		assertEquals("a", t.label().getText().toString());
	}

	@Test
	void textColorModifier() {
		Text t = new Text("Hello");
		t.color(Color.red);
		assertEquals(Color.red, t.label().color);
		t.dispose();
	}

	@Test
	void textReactiveColor() {
		Signal<Color> colorSig = Signal.of(Color.green);
		Text t = new Text("Hello").color(colorSig);
		assertEquals(Color.green, t.label().color);
		colorSig.set(Color.blue);
		assertEquals(Color.blue, t.label().color);
		t.dispose();
	}

	@Test
	void textWrapModifier() {
		Text t = new Text("Hello");
		t.wrap(true);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textEllipsisModifier() {
		Text t = new Text("Hello");
		t.ellipsis(true);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textAlignment() {
		Text t = new Text("Hello");
		t.left();
		t.center();
		t.right();
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textFontScaleModifier() {
		Text t = new Text("Hello");
		t.fontScale(2f);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textReactiveFontScale() {
		Signal<Float> scale = Signal.of(1f);
		Text t = new Text("Hello").fontScale(scale);
		assertNotNull(t.label());
		scale.set(2f);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textPaddingAndMargin() {
		Text t = new Text("Hello");
		t.padding(8f);
		t.margin(4f);
		t.paddingTop(2f);
		t.paddingBottom(3f);
		t.paddingLeft(4f);
		t.paddingRight(5f);
		t.marginTop(1f);
		t.marginBottom(2f);
		t.marginLeft(3f);
		t.marginRight(4f);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textPaddingFourArgs() {
		Text t = new Text("Hello");
		t.padding(1f, 2f, 3f, 4f);
		t.margin(5f, 6f, 7f, 8f);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textVisibleModifier() {
		Text t = new Text("Hello");
		t.visible(false);
		assertFalse(t.label().visible);
		t.visible(true);
		assertTrue(t.label().visible);
		t.dispose();
	}

	@Test
	void textReactiveVisible() {
		Signal<Boolean> vis = Signal.of(true);
		Text t = new Text("Hello").visible(vis);
		assertTrue(t.label().visible);
		vis.set(false);
		assertFalse(t.label().visible);
		t.dispose();
	}

	@Test
	void textPositionModifiers() {
		Text t = new Text("Hello");
		t.x(10f);
		assertEquals(10f, t.label().x, 0.01f);
		t.y(20f);
		assertEquals(20f, t.label().y, 0.01f);
		t.position(30f, 40f);
		assertEquals(30f, t.label().x, 0.01f);
		assertEquals(40f, t.label().y, 0.01f);
		t.dispose();
	}

	@Test
	void textSizeModifier() {
		Text t = new Text("Hello");
		t.size(100f, 50f);
		assertNotNull(t.label());
		t.dispose();
	}

	@Test
	void textNameModifier() {
		Text t = new Text("Hello").name("my-text");
		assertEquals("my-text", t.element().name);
		t.dispose();
	}

	@Test
	void textImplementsComponent() {
		Text t = new Text("Hello");
		assertInstanceOf(solim.core.Component.class, t);
		t.dispose();
	}

	@Test
	void textGrowModifiers() {
		Text t = new Text("Hello");
		t.growX();
		t.growY();
		t.grow();
		assertNotNull(t.label());
		t.dispose();
	}
}

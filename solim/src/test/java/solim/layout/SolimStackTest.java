package solim.layout;

import static org.junit.jupiter.api.Assertions.*;

import arc.Core;
import arc.mock.MockApplication;
import arc.mock.MockGraphics;
import arc.scene.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SolimStackTest {

	@BeforeAll
	static void initArc() {
		if (Core.app == null) {
			Core.app = new MockApplication();
		}
		if (Core.graphics == null) {
			Core.graphics = new MockGraphics();
		}
	}

	@Test
	void stackCreatesElement() {
		SolimStack s = new SolimStack();
		assertNotNull(s.element());
		assertNotNull(s.stack());
	}

	@Test
	void stackAddsChildren() {
		SolimStack s = new SolimStack();
		Element bg = new Element();
		Element fg = new Element();
		s.add(bg);
		s.add(fg);
		assertEquals(2, s.stack().getChildren().size);
	}

	@Test
	void stackSingleChild() {
		SolimStack s = new SolimStack();
		s.add(new Element());
		assertEquals(1, s.stack().getChildren().size);
	}

	@Test
	void stackNameModifier() {
		SolimStack s = new SolimStack();
		s.name("my-stack");
		assertEquals("my-stack", s.stack().name);
	}

	@Test
	void stackImplementsComponent() {
		SolimStack s = new SolimStack();
		assertInstanceOf(solim.core.Component.class, s);
	}
}

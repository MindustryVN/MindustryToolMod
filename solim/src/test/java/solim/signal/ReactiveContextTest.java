package solim.signal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ReactiveContextTest {
    @AfterEach
    void clear() {
        ReactiveContext.clear();
    }

    @Test
    void pushPopCurrent() {
        ReactiveObserver dummy = new ReactiveObserver() {
            @Override public void addDependency(Object observable) {}
            @Override public void invalidate() {}
        };
        assertNull(ReactiveContext.current());
        ReactiveContext.push(dummy);
        assertEquals(dummy, ReactiveContext.current());
        assertEquals(1, ReactiveContext.size());
        ReactiveContext.push(dummy);
        assertEquals(2, ReactiveContext.size());
        ReactiveContext.pop();
        assertEquals(1, ReactiveContext.size());
        ReactiveContext.pop();
        assertNull(ReactiveContext.current());
        assertEquals(0, ReactiveContext.size());
    }

    @Test
    void nestedEvaluationStack() {
        Signal<Integer> s = Signal.of(1);
        Computed<String> inner = Signal.computed(() -> "inner:" + s.get());
        Computed<String> outer = Signal.computed(() -> "outer:" + inner.get() + s.get());
        assertEquals("outer:inner:11", outer.get());
        // after get, stack should be empty
        assertEquals(0, ReactiveContext.size());
    }

    @Test
    void noThreadLocalImport() throws Exception {
        // try multiple relative locations because test working dir may vary
        Path[] candidates = new Path[]{
            Path.of("solim/src/solim/signal/ReactiveContext.java"),
            Path.of("src/solim/signal/ReactiveContext.java"),
            Path.of(System.getProperty("user.dir"), "solim/src/solim/signal/ReactiveContext.java"),
            Path.of(System.getProperty("user.dir"), "src/solim/signal/ReactiveContext.java")
        };
        String content = null;
        for (Path p : candidates) {
            if (Files.exists(p)) { content = new String(Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8); break; }
        }
        // fallback search from root
        if (content == null) {
            Path root = Path.of(System.getProperty("user.dir"));
            // walk up to find repo root containing solim folder
            Path cur = root;
            for (int i = 0; i < 5; i++) {
                Path candidate = cur.resolve("solim/src/solim/signal/ReactiveContext.java");
                if (Files.exists(candidate)) { content = new String(Files.readAllBytes(candidate), java.nio.charset.StandardCharsets.UTF_8); break; }
                cur = cur.getParent();
                if (cur == null) break;
            }
        }
        assertNotNull(content, "ReactiveContext.java not found");
        assertFalse(content.contains("import java.lang.ThreadLocal") || content.contains("import java.util.concurrent") && content.contains("ThreadLocal"),
            "ReactiveContext must not use ThreadLocal");
        // also ensure no ThreadLocal usage via import line
        long threadLocalImportCount = content.lines().filter(l -> l.trim().startsWith("import") && l.contains("ThreadLocal")).count();
        assertEquals(0, threadLocalImportCount, "ReactiveContext must not import ThreadLocal");
    }

    @Test
    void trackWithoutObserverIsNoOp() {
        ReactiveContext.clear();
        Signal<Integer> s = Signal.of(5);
        // should not throw when no observer
        assertDoesNotThrow(() -> ReactiveContext.track(s));
        assertEquals(5, s.get());
    }
}

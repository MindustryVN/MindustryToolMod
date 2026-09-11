package mindustrytool.features;

import arc.Core;
import arc.Events;
import mindustry.game.EventType.ResizeEvent;
import solim.signal.Readable;
import solim.signal.Signal;

public final class OrientationSignal {
    private static final Signal<Boolean> signal = Signal.of(false);

    private OrientationSignal() {}

    public static void init() {
        signal.set(Core.graphics.isPortrait());
        Events.on(ResizeEvent.class, e -> Core.app.post(() -> signal.set(Core.graphics.isPortrait())));
    }

    public static Readable<Boolean> isPortrait() {
        return signal;
    }
}

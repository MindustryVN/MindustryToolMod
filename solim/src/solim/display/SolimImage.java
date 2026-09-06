package solim.display;

import arc.scene.ui.Image;
import solim.signal.Effect;
import solim.signal.Signal;

/**
 * Display widget for drawable content.
 */
public final class SolimImage {
    private final Image image = new Image();
    private Effect binding;

    public SolimImage() {}

    public SolimImage(arc.scene.style.Drawable d) {
        image.setDrawable(d);
    }

    public static SolimImage of(arc.scene.style.Drawable d) {
        return new SolimImage(d);
    }

    public static SolimImage of(Signal<arc.scene.style.Drawable> s) {
        SolimImage img = new SolimImage();
        Effect e = Effect.of((java.util.function.Consumer<Effect.Cleanup>) cleanup -> {
            img.image.setDrawable(s.get());
        });
        img.binding = e;
        return img;
    }

    public Image image() {
        return image;
    }

    public void dispose() {
        if (binding != null) binding.dispose();
    }
}

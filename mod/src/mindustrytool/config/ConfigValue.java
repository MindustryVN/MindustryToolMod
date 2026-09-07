package mindustrytool.config;

import arc.util.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import solim.signal.Signal;

public class ConfigValue<T> {
    private final String key;
    private final @Nullable T defaultValue;
    private final Consumer<T> setter;
    private final Signal<T> signal;
    private boolean updating = false;

    public ConfigValue(
            String key,
            @Nullable T defaultValue,
            Supplier<T> getter,
            Consumer<T> setter) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.setter = setter;

        T initial = getter.get();
        this.signal = Signal.of(initial != null ? initial : defaultValue);

        this.signal.subscribe(value -> {
            if (updating)
                return;
            updating = true;
            try {
                this.setter.accept(value);
            } finally {
                updating = false;
            }
        });
    }

    public String getKey() {
        return key;
    }

    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    public @Nullable T get() {
        return signal.get();
    }

    public void set(@Nullable T value) {
        if (updating)
            return;
        updating = true;
        try {
            setter.accept(value);
            if (!Objects.equals(signal.get(), value)) {
                signal.set(value);
            }
        } finally {
            updating = false;
        }
    }

    public Signal<T> signal() {
        return signal;
    }

    public void reset() {
        set(defaultValue);
    }

    public boolean isModified() {
        T current = get();
        return !Objects.equals(current, defaultValue);
    }
}

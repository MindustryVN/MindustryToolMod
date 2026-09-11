package mindustrytool.config;

import arc.util.Nullable;
import java.util.Objects;
import java.util.function.Function;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.signal.Subscription;

public class ContextualConfigValue<T, K> {
    private final ConfigGroup group;
    private final String baseName;
    private final Readable<K> discriminant;
    private final Function<K, String> keySuffix;
    private final @Nullable T defaultValue;
    private final Signal<T> signal;
    private final ContextualPersister<T> persister;
    private final Subscription discriminantSub;

    private @Nullable K currentDiscriminant;
    private String currentKey;
    private boolean updating = false;

    public ContextualConfigValue(
            ConfigGroup group,
            String baseName,
            Readable<K> discriminant,
            Function<K, String> keySuffix,
            @Nullable T defaultValue,
            ContextualPersister<T> persister) {
        this.group = group;
        this.baseName = baseName;
        this.discriminant = discriminant;
        this.keySuffix = keySuffix;
        this.defaultValue = defaultValue;
        this.persister = persister;

        this.currentDiscriminant = discriminant.peek();
        this.currentKey = deriveKey(currentDiscriminant);
        T initial = loadFromSettings(currentKey);
        this.signal = Signal.of(initial != null ? initial : defaultValue);

        this.signal.subscribe(value -> {
            if (updating)
                return;
            updating = true;
            try {
                persister.save(currentKey, value);
            } finally {
                updating = false;
            }
        });

        this.discriminantSub = subscribeToDiscriminant();
    }

    private Subscription subscribeToDiscriminant() {
        if (discriminant instanceof Signal) {
            return ((Signal<K>) discriminant).subscribe(this::onDiscriminantChanged);
        } else if (discriminant instanceof Computed) {
            return ((Computed<K>) discriminant).subscribe(this::onDiscriminantChanged);
        }
        return () -> {};
    }

    private void onDiscriminantChanged(K newDisc) {
        if (updating)
            return;
        updating = true;
        try {
            T current = signal.peek();
            persister.save(currentKey, current);
            currentDiscriminant = newDisc;
            currentKey = deriveKey(newDisc);
            T loaded = loadFromSettings(currentKey);
            signal.set(loaded != null ? loaded : defaultValue);
        } finally {
            updating = false;
        }
    }

    private String deriveKey(K disc) {
        return group.resolveKey(baseName + "." + keySuffix.apply(disc));
    }

    private @Nullable T loadFromSettings(String key) {
        return persister.load(key);
    }

    public @Nullable T get() {
        return signal.peek();
    }

    public void set(@Nullable T value) {
        if (updating)
            return;
        updating = true;
        try {
            persister.save(currentKey, value);
            if (!Objects.equals(signal.peek(), value)) {
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
        return !Objects.equals(get(), defaultValue);
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public void dispose() {
        discriminantSub.dispose();
    }

    public interface ContextualPersister<T> {
        @Nullable T load(String key);
        void save(String key, @Nullable T value);
    }
}

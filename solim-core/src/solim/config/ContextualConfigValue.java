package solim.config;

import arc.util.Nullable;
import java.util.function.Function;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;
import solim.signal.Subscription;

public class ContextualConfigValue<T, K> extends ConfigValue<T> {
	private final ConfigGroup group;
	private final String baseName;
	private final Readable<K> discriminant;
	private final Function<K, String> keySuffix;
	private final ConfigPersister<T> persister;
	private final Subscription discriminantSub;
	private @Nullable K currentDiscriminant;

	public ContextualConfigValue(
			ConfigGroup group,
			String baseName,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			@Nullable T defaultValue,
			ConfigPersister<T> persister) {
		super(
				deriveInitialKey(group, baseName, discriminant, keySuffix),
				defaultValue,
				() -> persister != null ? persister.load(deriveInitialKey(group, baseName, discriminant, keySuffix), defaultValue) : null,
				null);
		this.group = group;
		this.baseName = baseName;
		this.discriminant = discriminant;
		this.keySuffix = keySuffix;
		this.persister = persister;
		this.currentDiscriminant = discriminant != null ? discriminant.peek() : null;
		this.discriminantSub = subscribeToDiscriminant();
	}

	public ContextualConfigValue(
			ConfigGroup group,
			String baseName,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			@Nullable T defaultValue,
			ContextualPersister<T> persister) {
		this(group, baseName, discriminant, keySuffix, defaultValue, (ConfigPersister<T>) persister);
	}

	private static <K> String deriveInitialKey(
			ConfigGroup group,
			String baseName,
			@Nullable Readable<K> discriminant,
			Function<K, String> keySuffix) {
		K initialDisc = discriminant != null ? discriminant.peek() : null;
		String suffix = keySuffix != null ? keySuffix.apply(initialDisc) : "";
		return group.resolveKey(baseName + (suffix != null && !suffix.isEmpty() ? "." + suffix : ""));
	}

	@Override
	protected void save(@Nullable T value) {
		if (persister != null) {
			persister.save(this.key, value);
		}
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
		if (updating) {
			return;
		}
		updating = true;
		try {
			T current = signal.peek();
			if (persister != null) {
				persister.save(this.key, current);
			}
			this.currentDiscriminant = newDisc;
			this.key = deriveKey(newDisc);
			T loaded = persister != null ? persister.load(this.key, defaultValue) : null;
			signal.set(loaded != null ? loaded : defaultValue);
		} finally {
			updating = false;
		}
	}

	private String deriveKey(K disc) {
		String suffix = keySuffix != null ? keySuffix.apply(disc) : "";
		return group.resolveKey(baseName + (suffix != null && !suffix.isEmpty() ? "." + suffix : ""));
	}

	public String getCurrentKey() {
		return getKey();
	}

	public @Nullable K getCurrentDiscriminant() {
		return currentDiscriminant;
	}

	@Override
	public void dispose() {
		if (discriminantSub != null) {
			discriminantSub.dispose();
		}
		super.dispose();
	}

	public interface ContextualPersister<T> extends ConfigPersister<T> {
		@Nullable
		T load(String key);

		@Override
		default @Nullable T load(String key, @Nullable T defaultValue) {
			T val = load(key);
			return val != null ? val : defaultValue;
		}
	}
}

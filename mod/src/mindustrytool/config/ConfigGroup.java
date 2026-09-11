package mindustrytool.config;

import arc.Core;
import arc.util.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import solim.signal.Readable;
import mindustrytool.features.Feature;
import mindustrytool.features.FeatureMetadata;

public class ConfigGroup {

	private static final String ROOT = "mindustrytool";
	private static final String FEATURES = ROOT + ".features";

	private final String namespace;

	private ConfigGroup(String namespace) {
		this.namespace = normalizeNamespace(namespace);
	}

	public static ConfigGroup of(@Nullable Feature feature) {
		if (feature == null) {
			return new ConfigGroup(FEATURES);
		}
		return of(feature.getMetadata());
	}

	public static ConfigGroup of(@Nullable FeatureMetadata metadata) {
		if (metadata == null) {
			return new ConfigGroup(FEATURES);
		}
		return new ConfigGroup(FEATURES + "." + metadata.getId());
	}

	public static ConfigGroup of(String namespace) {
		return new ConfigGroup(namespace);
	}

	public static ConfigGroup global(String name) {
		if (name == null || name.trim().isEmpty()) {
			return new ConfigGroup(ROOT);
		}
		if (name.startsWith(ROOT + ".") || name.equals(ROOT)) {
			return new ConfigGroup(name);
		}
		return new ConfigGroup(ROOT + "." + name);
	}

	public ConfigGroup group(String subGroup) {
		if (subGroup == null || subGroup.trim().isEmpty()) {
			return this;
		}
		return new ConfigGroup(namespace + "." + subGroup);
	}

	public String getNamespace() {
		return namespace;
	}

	public String resolveKey(String name) {
		if (name == null || name.trim().isEmpty()) {
			return namespace;
		}
		String cleanName = name.startsWith(".") ? name.substring(1) : name;
		if (namespace.isEmpty()) {
			return cleanName;
		}
		return namespace + "." + cleanName;
	}

	public ConfigValue<Boolean> boolValue(String name, boolean defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> Core.settings.getBool(key, defaultValue),
				val -> {
					if (val != null) {
						Core.settings.put(key, val);
					}
				});
	}

	public ConfigValue<Integer> intValue(String name, int defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> Core.settings.getInt(key, defaultValue),
				val -> {
					if (val != null) {
						Core.settings.put(key, val);
					}
				});
	}

	public ConfigValue<Long> longValue(String name, long defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> Core.settings.getLong(key, defaultValue),
				val -> {
					if (val != null) {
						Core.settings.put(key, val);
					}
				});
	}

	public ConfigValue<Float> floatValue(String name, float defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> Core.settings.getFloat(key, defaultValue),
				val -> {
					if (val != null) {
						Core.settings.put(key, val);
					}
				});
	}

	public ConfigValue<Double> doubleValue(String name, double defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> (double) Core.settings.getFloat(key, (float) defaultValue),
				val -> {
					if (val != null) {
						Core.settings.put(key, val.floatValue());
					}
				});
	}

	public ConfigValue<String> stringValue(String name, @Nullable String defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> Core.settings.getString(key, defaultValue),
				val -> {
					if (val == null) {
						Core.settings.remove(key);
					} else {
						Core.settings.put(key, val);
					}
				});
	}

	public ConfigValue<Set<String>> setValue(String name, Class<String> elementType, Set<String> defaultValue) {
		String key = resolveKey(name);
		return new ConfigValue<>(
				key,
				defaultValue,
				() -> {
					String str = Core.settings.getString(key, null);
					if (str == null) {
						return defaultValue;
					}
					if (str.trim().isEmpty()) {
						return new HashSet<>();
					}
					return new HashSet<>(Arrays.asList(str.split(",")));
				},
				setVal -> {
					if (setVal == null || setVal.isEmpty()) {
						Core.settings.put(key, "");
					} else {
						Core.settings.put(key, String.join(",", setVal));
					}
				});
	}

	public <K> ContextualConfigValue<Boolean, K> boolValueKeyed(
			String name,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			boolean defaultValue) {
		return new ContextualConfigValue<>(
				this,
				name,
				discriminant,
				keySuffix,
				defaultValue,
				new ContextualConfigValue.ContextualPersister<Boolean>() {
					@Override
					public Boolean load(String key) {
						return Core.settings.getBool(key, defaultValue);
					}

					@Override
					public void save(String key, Boolean value) {
						if (value != null) {
							Core.settings.put(key, value);
						}
					}
				});
	}

	public <K> ContextualConfigValue<Integer, K> intValueKeyed(
			String name,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			int defaultValue) {
		return new ContextualConfigValue<>(
				this,
				name,
				discriminant,
				keySuffix,
				defaultValue,
				new ContextualConfigValue.ContextualPersister<Integer>() {
					@Override
					public Integer load(String key) {
						return Core.settings.getInt(key, defaultValue);
					}

					@Override
					public void save(String key, Integer value) {
						if (value != null) {
							Core.settings.put(key, value);
						}
					}
				});
	}

	public <K> ContextualConfigValue<Float, K> floatValueKeyed(
			String name,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			float defaultValue) {
		return new ContextualConfigValue<>(
				this,
				name,
				discriminant,
				keySuffix,
				defaultValue,
				new ContextualConfigValue.ContextualPersister<Float>() {
					@Override
					public Float load(String key) {
						return Core.settings.getFloat(key, defaultValue);
					}

					@Override
					public void save(String key, Float value) {
						if (value != null) {
							Core.settings.put(key, value);
						}
					}
				});
	}

	public <K> ContextualConfigValue<String, K> stringValueKeyed(
			String name,
			Readable<K> discriminant,
			Function<K, String> keySuffix,
			@Nullable String defaultValue) {
		return new ContextualConfigValue<>(
				this,
				name,
				discriminant,
				keySuffix,
				defaultValue,
				new ContextualConfigValue.ContextualPersister<String>() {
					@Override
					public String load(String key) {
						return Core.settings.getString(key, defaultValue);
					}

					@Override
					public void save(String key, String value) {
						if (value == null) {
							Core.settings.remove(key);
						} else {
							Core.settings.put(key, value);
						}
					}
				});
	}

	private static String normalizeNamespace(String ns) {
		if (ns == null) {
			return "";
		}
		String clean = ns.trim();
		while (clean.startsWith(".")) {
			clean = clean.substring(1);
		}
		while (clean.endsWith(".")) {
			clean = clean.substring(0, clean.length() - 1);
		}
		return clean.replaceAll("\\.{2,}", ".");
	}
}

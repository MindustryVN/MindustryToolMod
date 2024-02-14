package mindytool.data;

public enum TagName {
    schematic("schematic-search-tag"),
    map("map-search-tag");

    final String value;

    TagName(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

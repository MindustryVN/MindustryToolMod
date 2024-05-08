package mindytool.data;

public enum TagName {
    schematic("schematic"),
    map("map");

    final String value;

    TagName(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

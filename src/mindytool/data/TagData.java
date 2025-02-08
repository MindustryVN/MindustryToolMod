package mindytool.data;

import arc.struct.Seq;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class TagData {
    String name;
    Seq<TagValue> values;
    String color;

    @Data
    @Accessors(chain = true, fluent = true)
    public static class TagValue {
        String name;
        String modId;
        String icon;

    }
}

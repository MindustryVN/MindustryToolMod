package mindytool.data;

import arc.struct.Seq;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class SchematicDetailData {
    String id;
    String name;
    String userId;
    String itemId;
    int width;
    int height;
    String description;
    SchematicMetadata metadata;
    Seq<TagData> tags;
    Long likes;
    Long dislikes;
    String verifierId;
    Boolean isVerified;
    Long downloadCount;

    @Data
    @Accessors(chain = true, fluent = true)
    public static class SchematicMetadata {
        Seq<SchematicRequirement> requirements;
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class SchematicRequirement {
        String name;
        String color;
        Integer amount;
    }
}

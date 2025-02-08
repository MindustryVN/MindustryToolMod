package mindytool.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class SchematicData {
    String id;
    String itemId;
    String userId;
    String name;
    Long likes;
    Long dislikes;
    Boolean isCurated;
    Boolean isVerified;
    Long downloadCount;
}

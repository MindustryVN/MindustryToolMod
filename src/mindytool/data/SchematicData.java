package mindytool.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class SchematicData {
    String id;
    String itemId;
    String name;
    Long likes;
    Long dislikes;
    Boolean isCurated;
    Boolean isVerified;
    Integer downloadCount;
}

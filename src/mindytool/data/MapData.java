package mindytool.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class MapData {
    String id;
    String userId;
    String itemId;
    String name;
    Long likes;
    Long dislikes;
    Boolean isCurated;
    Boolean isVerified;
    Long downloadCount;
    Boolean isPrivate;
}

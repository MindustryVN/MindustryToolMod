package mindustrytool.data;

import arc.struct.Seq;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class MapDetailData {
    String id;
    String name;
    String userId;
    String itemId;
    int width;
    int height;
    String description;
    Seq<TagData> tags;
    Long likes;
    Long dislikes;
    String verifierId;
    Boolean isVerified;
    Long downloadCount;
    Boolean isPrivate;
}

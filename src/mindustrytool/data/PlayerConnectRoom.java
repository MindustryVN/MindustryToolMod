package mindustrytool.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class PlayerConnectRoom {
    private String roomId;
    private String address;
    private String data;
}

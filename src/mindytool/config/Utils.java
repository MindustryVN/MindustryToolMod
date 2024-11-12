package mindytool.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import arc.struct.ObjectMap;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public class Utils {

    public static ObjectMap<String, Schematic> schematicData = new ObjectMap<>();

    public static synchronized Schematic readSchematic(String data) {
        return schematicData.get(data, () -> Schematics.readBase64(data));
    }

    public static byte[] webpToPng(InputStream webpStream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            var image = ImageIO.read(webpStream);
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Unable to convert image");
        }
    }
}

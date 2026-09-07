package mindustrytool.components;

import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import mindustry.gen.Icon;
import mindustrytool.Main;

public class FileIcon {

	private static ConcurrentHashMap<String, TextureRegionDrawable> iconCache = new ConcurrentHashMap<>();

	public static TextureRegionDrawable of(String name) {
		if (iconCache.containsKey(name)) {
			return iconCache.get(name);
		}

		try {
			var texture =
					new TextureRegion(new Texture(Main.self.root.child("icons").child(name)));
			var drawable = new TextureRegionDrawable(texture);
			iconCache.put(name, drawable);
			return drawable;
		} catch (Exception e) {
			Log.err(e.getMessage());
			iconCache.put(name, Icon.book);
			return Icon.book;
		}
	}
}

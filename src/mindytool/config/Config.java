package mindytool.config;

import mindytool.data.Sort;
import java.util.Arrays;
import java.util.List;

public class Config {

    private static final String DEV_URL = "https://api.mindustry-tool.app/api/v3/";
    // private static final String DEV_URL = "http://localhost:8080/api/v3/";
    private static final String PROD_URL = "https://api.mindustry-tool.app/api/v3/";
    // private static final String PROD_URL =
    // "https://api.mindustry-tool.app/api/v3/";
    private static final String ENV = System.getenv("ENV");
    public static final boolean DEV = (ENV != null && ENV.equals("DEV"));
    public static final String API_URL = DEV ? DEV_URL : PROD_URL;
    public static final String IMAGE_URL = "https://res.cloudinary.com/dyx7yui8u/image/upload/v1703328847/";

    public static final String REPO_URL = "https://api.github.com/repos/sharrlotte/MindustryToolMod/releases/latest";
    public static final String CACERT_URL = "https://api.github.com/repos/sharrlotte/MindustryToolMod/contents/assets/security/certificate.crt";

    public static final List<Sort> sorts = Arrays.asList(//
            new Sort("newest", "time_1"), //
            new Sort("oldest", "time_-1"),
            new Sort("most-like", "like_1"));
}

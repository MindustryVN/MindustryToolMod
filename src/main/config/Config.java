package main.config;

import java.util.Arrays;
import java.util.List;

import main.data.Sort;

public class Config {

    private static final String DEV_URL = "http://localhost:8080/api/v2/";
    private static final String PROD_URL = "https://mindustry-tool-backend.onrender.com/api/v2/";
    public static final String API_URL = System.getenv("ENV").equals("DEV") ? DEV_URL
            : PROD_URL;
    public static final List<Sort> sorts = Arrays.asList(new Sort("newest", "time_1"), new Sort("oldest", "time_-1"),
            new Sort("most liked", "like_1"));
}

package mindustrytool.models;

import lombok.Data;

@Data
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
}

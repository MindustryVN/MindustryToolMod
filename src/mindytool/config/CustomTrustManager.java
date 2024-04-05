package mindytool.config;

import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import arc.util.Http;
import arc.util.Log;

public class CustomTrustManager {

    public static void init() {
        Http.get(Config.CACERTS_URL).block((res) -> {
            var cert = (X509Certificate) CertificateFactory.getInstance("X509")
                    .generateCertificate(res.getResultAsStream());

            KeyStore.getInstance(KeyStore.getDefaultType())
                    .setCertificateEntry("mindustry-tool", cert);
        });
    }
}

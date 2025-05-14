package org.eea.utils;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import software.amazon.awssdk.http.TlsTrustManagersProvider;

public class TrustAllManagersProvider implements TlsTrustManagersProvider {
  private static final TrustManager[] TRUST_ALL = new TrustManager[] {
      new X509TrustManager() {
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
      }
  };

  public TrustManager[] trustManagers() {
    return TRUST_ALL;
  }
}

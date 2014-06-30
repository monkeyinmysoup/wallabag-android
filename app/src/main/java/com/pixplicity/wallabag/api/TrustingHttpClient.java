package com.pixplicity.wallabag.api;

import android.content.Context;

import com.pixplicity.wallabag.Constants;
import com.squareup.okhttp.OkHttpClient;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * HttpClient extension that keeps a keystore for trusted, self-signed SSL certificates.
 */
public class TrustingHttpClient {

    private final OkHttpClient okclient;
    private final Context mContext;

    public TrustingHttpClient(Context context) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        this.mContext = context;
        okclient = new OkHttpClient();
        KeyStore store = getKeyStore(mContext);
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(store);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(store, null);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        okclient.setSslSocketFactory(sslContext.getSocketFactory());

//        HttpClient client = new DefaultHttpClient(
//                createClienConnectionManager(), new BasicHttpParams());
    }
//    @Override
//    protected ClientConnectionManager createClientConnectionManager() {
//        SchemeRegistry registry = new SchemeRegistry();
//        registry.register(
//                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", newSslSocketFactory(), 443));
//        return new SingleClientConnManager(getParams(), registry);
//    }

//    private SSLSocketFactory newSslSocketFactory() {
//        try {
//            KeyStore store = getKeyStore();
//            return new SSLSocketFactory(store);
//        } catch (Exception e) {
//            throw new AssertionError(e);
//        }
//    }

    public static KeyStore getKeyStore(Context context) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType()); // == BKS
        File storeFile = new File(context.getFilesDir(), Constants.SSL_KEYSTORE_FILENAME);
        if (storeFile.exists()) {
            InputStream in = new FileInputStream(storeFile);
            try {
                store.load(in, null);
            } finally {
                in.close();
            }
        } else {
            store.load(null); // init empty keystore
        }
        return store;
    }

    private void acceptCertificate(KeyStore store, String url) {
        URLConnection uc = null;
        try {
            uc = new URL(url).openConnection();
            uc.connect();
            Certificate[] certs = ((HttpsURLConnection) uc).getServerCertificates();
            for (int i = 0; i < certs.length; i++) {
                store.setCertificateEntry("cert_" + i, certs[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (uc != null && uc instanceof HttpURLConnection) {
                ((HttpsURLConnection) uc).disconnect();
            }
        }
    }
}
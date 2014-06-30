package com.pixplicity.wallabag.api;

import android.content.Context;

import com.pixplicity.wallabag.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SslTrustManager implements X509TrustManager {

    private X509TrustManager defaultTrustManager;
    private List<Certificate> tempCertList = new ArrayList<>();
    private Context mContext;

    public SslTrustManager(Context context) throws Exception {
        mContext = context;
//        String alg = TrustManagerFactory.getDefaultAlgorithm();
//        try {
//            defaultTrustManager = (X509TrustManager) TrustManagerFactory.getInstance(alg).getTrustManagers()[0];
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(store);
//            localTrustManager = (X509TrustManager) TrustManagerFactory.getInstance(alg).getTrustManagers()[0];
//        } catch (NoSuchAlgorithmException | KeyStoreException e) {
//            e.printStackTrace();
//        }
        reloadTrustManager();
    }

    private void reloadTrustManager() throws Exception {
        KeyStore store = getKeyStore(mContext);

        // add all temporary certs to KeyStore (ts)
        for (Certificate cert : tempCertList) {
            store.setCertificateEntry(UUID.randomUUID().toString(), cert);
        }

        // initialize a new TMF with the ts we just loaded
        TrustManagerFactory tmf
                = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(store);

        // acquire X509 trust manager from factory
        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                defaultTrustManager = (X509TrustManager) tms[i];
                return;
            }
        }

        throw new NoSuchAlgorithmException(
                "No X509TrustManager in TrustManagerFactory");
    }

    private KeyStore getKeyStore(Context context) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
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


    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException cx) {
            addServerCertAndReload(chain[0], true);
            defaultTrustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] issuers
                = defaultTrustManager.getAcceptedIssuers();
        return issuers;
    }

    private void addServerCertAndReload(Certificate cert,
                                        boolean permanent) {
        try {
            // TODO only add if the user say's it's ok
            if (permanent) {
                // import the cert into file trust store
                KeyStore store = getKeyStore(mContext);
                store.setCertificateEntry(UUID.randomUUID().toString(), cert);
                OutputStream out = new FileOutputStream(new File(mContext.getFilesDir(), Constants.SSL_KEYSTORE_FILENAME));
                store.store(out, null);
            }
            tempCertList.add(cert);
            reloadTrustManager();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
        /*
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException ignored) {
        }

        try {
            localTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }
    */
}
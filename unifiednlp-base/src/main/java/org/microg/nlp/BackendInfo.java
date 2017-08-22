package org.microg.nlp;

import android.content.pm.ServiceInfo;

public class BackendInfo {
    public final ServiceInfo serviceInfo;
    public final String simpleName;
    public final String signatureDigest;
    public boolean enabled = false;

    public BackendInfo(ServiceInfo serviceInfo, String simpleName, String signatureDigest) {
        this.serviceInfo = serviceInfo;
        this.simpleName = simpleName;
        this.signatureDigest = signatureDigest;
    }

    public String getMeta(String metaName) {
        return serviceInfo.metaData != null ? serviceInfo.metaData.getString(metaName) : null;
    }

    @Override
    public String toString() {
        return simpleName;
    }

}
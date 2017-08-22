/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.nlp.location;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.microg.nlp.AbstractProviderService;
import org.microg.nlp.BackendInfo;
import org.microg.nlp.Preferences;
import org.microg.nlp.api.LocationBackend;

import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static org.microg.nlp.api.Constants.ACTION_LOCATION_BACKEND;
import static org.microg.nlp.api.Constants.METADATA_BACKEND_INIT_ACTIVITY;

public abstract class AbstractLocationService extends AbstractProviderService<LocationProvider> {
    private static final String TAG = "KarooNlp";

    /**
     * Creates an LocationService.  Invoked by your subclass's constructor.
     *
     * @param tag Used for debugging.
     */
    public AbstractLocationService(String tag) {
        super(tag);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "LocationService: onBind");
        enableBackends();
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "LocationService: onHandleIntent");
        LocationProvider provider = getProvider();
        enableBackends();
        provider.reload();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "LocationService: onUnbind");
        LocationProvider provider = getProvider();
        if (provider != null) {
            provider.onDisable();
        }
        return super.onUnbind(intent);
    }

    protected void enableBackends() {
        Log.v(TAG, "LocationService: enableBackends");
        List<BackendInfo> backends = new Preferences(this).getInstalledBackends();
        for (BackendInfo locationBackend : backends) {
            Log.v(TAG, "LocationService: enabling backend :" + locationBackend.simpleName);
            enableLocationBackend(locationBackend);
        }
    }

    protected void enableLocationBackend(BackendInfo backendInfo) {
        try {
            if (backendInfo.getMeta(METADATA_BACKEND_INIT_ACTIVITY) != null) {
                Log.v(TAG, "enableLocationBackend: METADATA_BACKEND_INIT_ACTIVITY not null");
                startActivity(createExternalIntent(backendInfo, METADATA_BACKEND_INIT_ACTIVITY));
            } else {
                Log.v(TAG, "enableLocationBackend: METADATA_BACKEND_INIT_ACTIVITY null");
                Intent intent = new Intent(ACTION_LOCATION_BACKEND);
                intent.setPackage(backendInfo.serviceInfo.packageName);
                intent.setClassName(backendInfo.serviceInfo.packageName, backendInfo.serviceInfo.name);
                bindService(intent, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.v(TAG, "enableLocationBackend: service connected");
                        Intent i = getBackendInitIntent(service);
                        if (i != null) {
                            Log.v(TAG, "enableLocationBackend: service connected: backendInitIntent not null, starting activity");
                            startActivity(i);
                        }
                        Log.v(TAG, "enableLocationBackend: service connected: unbinding service");
                        unbindService(this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.v(TAG, "enableLocationBackend: service disconnected");
                    }
                }, BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            backendInfo.enabled = false;
            Log.e(TAG, "enableLocationBackend: Error initializing backend");
        }
    }

    protected Intent getBackendInitIntent(IBinder service) {
        LocationBackend backend = LocationBackend.Stub.asInterface(service);
        try {
            return backend.getInitIntent();
        } catch (RemoteException e) {
            return null;
        }
    }

    private Intent createExternalIntent(BackendInfo backendInfo, String metaName) {
        Intent intent = new Intent(ACTION_VIEW);
        intent.setPackage(backendInfo.serviceInfo.packageName);
        intent.setClassName(backendInfo.serviceInfo.packageName, backendInfo.getMeta(metaName));
        return intent;
    }
}

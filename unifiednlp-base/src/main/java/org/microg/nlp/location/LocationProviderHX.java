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

import android.content.Context;
import android.location.Criteria;
import android.os.Bundle;
import android.os.WorkSource;
import android.util.Log;

import com.android.location.provider.LocationProviderBase;
import com.android.location.provider.ProviderPropertiesUnbundled;
import com.android.location.provider.ProviderRequestUnbundled;

import static android.location.LocationProvider.AVAILABLE;

class LocationProviderHX extends LocationProviderBase implements LocationProvider {
    private static final String TAG = "KarooNlp";
    private static final ProviderPropertiesUnbundled props = ProviderPropertiesUnbundled.create(
            false, // requiresNetwork
            false, // requiresSatellite
            false, // requiresCell
            false, // hasMonetaryCost
            true, // supportsAltitude
            true, // supportsSpeed
            true, // supportsBearing
            Criteria.POWER_LOW, // powerRequirement
            Criteria.ACCURACY_COARSE); // accuracy
    private final ThreadHelper helper;

    public LocationProviderHX(Context context) {
        super(TAG, props);
        this.helper = new ThreadHelper(context, this);
    }

    @Override
    public void onDisable() {
        Log.v(TAG, "LocationProviderHX: onDisable");
        helper.disable();
    }

    @Override
    public void reload() {
        Log.v(TAG, "LocationProviderHX: reload");
        helper.reload();
    }

    @Override
    public void destroy() {
        Log.v(TAG, "LocationProviderHX: destroy");
        helper.destroy();
    }

    @Override
    public void onEnable() {
        Log.v(TAG, "LocationProviderHX: onEnable");
    }

    @Override
    public int onGetStatus(Bundle extras) {
        Log.v(TAG, "LocationProviderHX: onGetStatus");
        return AVAILABLE;
    }

    @Override
    public long onGetStatusUpdateTime() {
        return 0;
    }

    @Override
    public void onSetRequest(ProviderRequestUnbundled requests, WorkSource source) {
        Log.v(TAG, "LocationProviderHX: onSetRequest" + requests + " by " + source);
        long autoTime = Math.max(requests.getInterval(), FASTEST_REFRESH_INTERVAL);
        boolean autoUpdate = requests.getReportLocation();

        Log.v(TAG, "using autoUpdate=" + autoUpdate + " autoTime=" + autoTime);
        if (autoUpdate) {
            helper.setTime(autoTime);
            helper.enable();
        } else {
            helper.disable();
        }
    }

}

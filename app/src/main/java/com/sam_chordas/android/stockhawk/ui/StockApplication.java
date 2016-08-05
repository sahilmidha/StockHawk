package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.CalendarContract;

import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.facebook.stetho.inspector.database.ContentProviderDatabaseDriver;
import com.facebook.stetho.inspector.database.ContentProviderSchema;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

/**
 * Created by sahilmidha on 05/08/16.
 */
public class StockApplication extends Application
{
    public void onCreate() {
        super.onCreate();
        initializeStetho(getApplicationContext());
    }

    private void initializeStetho(final Context context)
    {
        // See also: Stetho.initializeWithDefaults(Context)
        Stetho.initialize(Stetho.newInitializerBuilder(context)
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                .build());
    }

}

package com.onspota.demo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onspota.sdk.model.Event;
import com.onspota.sdk.model.PlacesListItem;
import com.onspota.sdk.model.SearchResponse;
import com.onspota.sdk.model.SpotSearchResponse;
import com.crashlytics.android.Crashlytics;
import com.greysonparrelli.permiso.Permiso;
import com.onspota.sdk.OnspotaApi;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class MainActivity extends AppCompatActivity implements FlexibleAdapter.OnItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MAX_LAST_EVENTS = 10;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.version)
    TextView tvVersion;

    @BindString(R.string.places_header) String placesHeader;
    @BindString(R.string.spots_header) String spotsHeader;
    @BindString(R.string.events_header) String eventsHeader;

    private FlexibleAdapter mAdapter;
    private ViewGroup mRootView;
    private List<Event> mLastEvents = new ArrayList<>();

    private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SearchResponse searchResponse = (SearchResponse) intent.getSerializableExtra(getString(com.onspota.sdk.R.string.intent_search_response));
            String searchReason = intent.getStringExtra(getString(com.onspota.sdk.R.string.intent_search_reason));
            Log.i(TAG, ".onReceive: searchResponse = " + searchResponse + ", searchReason = " + searchReason);

            mLastEvents.addAll(0, searchResponse.getEvents());
            while (mLastEvents.size() > MAX_LAST_EVENTS)
                mLastEvents.remove(mLastEvents.size() - 1);

            loadData(searchResponse.getPlaces(), searchResponse.getSpots(), searchResponse.getEvents());
        }
    };

    private void loadData(List<PlacesListItem> places, List<SpotSearchResponse> spots, List<Event> events) {
        // init headers
        List<AbstractFlexibleItem> items = new ArrayList<>();
        items.add(createPlacesHeaderItem(places));
        items.add(createSpotsHeaderItem(spots));
        items.add(createEventsHeaderItem(events));
        mAdapter.clear();
        mAdapter.addItems(0, items);
        mAdapter.expandAll(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);
        mRootView = (ViewGroup) getWindow().getDecorView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        String[] permissionsRequired = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        Permiso.getInstance().setActivity(this);
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                boolean accessFineLocationGranted = resultSet.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);

                if (accessFineLocationGranted) {
                    // Replace 'MyUserId' with the user's user-id allocated by YOUR service.
                    OnspotaApi.SdkResult sdkResult = new OnspotaApi(MainActivity.this).start("MyUserID");

                    if (sdkResult == OnspotaApi.SdkResult.Ok) {
                        // SDK was started.
                    }
                    else if (sdkResult == OnspotaApi.SdkResult.AndroidVersionNotSupported) {
                        Log.w(TAG,"OnSpota SDK doesn't support current android os");
                    }
                    else if (sdkResult == OnspotaApi.SdkResult.FailedToFind3rdPartyLib) {
                        Log.e(TAG,"Critical error: OnSpota SDK Failed to find at least one of its required dependencies.");
                    }
                    else if (sdkResult == OnspotaApi.SdkResult.Failed) {
                        Log.e(TAG,"OnSpota SDK Failed to start.");
                    }

               /*     Intent intent = StartWalkerService.createIntent(MainActivity.this,"MyUserID");
                    startService(intent);*/
                } else if (resultSet.isPermissionPermanentlyDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showSimpleError(R.string.permission_access_fine_location_permanently_denied);
                } else {
                    showSimpleError(R.string.permission_access_fine_location_denied);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                callback.onRationaleProvided();
            }
        }, permissionsRequired);

        // initialize the adapter
        mAdapter = new FlexibleAdapter(null, this, true);
        mAdapter.setAutoCollapseOnExpand(false)
                .setMinCollapsibleLevel(2) //Auto-collapse only mItems with level >= 1 (avoid to collapse also sections!)
                .setAutoScrollOnExpand(true)
                .setStickyHeaders(true);

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        // retrieve version name
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(String.format(getResources().getString(R.string.app_version), packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private void showSimpleError(int textResId) {
        final Snackbar snack = Snackbar.make(mRootView, textResId, Snackbar.LENGTH_INDEFINITE);

        snack.setAction(R.string.snackbar_action_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snack.dismiss();
                finish();
            }
        });

        snack.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mEventReceiver, new IntentFilter(getString(com.onspota.sdk.R.string.intent_search)));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mEventReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private HeaderItem createPlacesHeaderItem(List<PlacesListItem> places) {
        HeaderItem headerItem = new HeaderItem(placesHeader);
        headerItem.setTitle(placesHeader);

        for(PlacesListItem place : places) {
            headerItem.addSubItem(new ContentItem(place.getId(), place));
        }

        return headerItem;
    }

    private HeaderItem createSpotsHeaderItem(List<SpotSearchResponse> spots) {
        HeaderItem headerItem = new HeaderItem(spotsHeader);
        headerItem.setTitle(spotsHeader);
        for(SpotSearchResponse spot : spots) {
            headerItem.addSubItem(new ContentItem(spot.getId(), spot));
        }
        return headerItem;
    }

    private HeaderItem createEventsHeaderItem(List<Event> events) {
        HeaderItem headerItem = new HeaderItem(eventsHeader);
        headerItem.setTitle(eventsHeader);
        for(Event event : events) {
            headerItem.addSubItem(new ContentItem(event.getObjectId(), event));
        }
        return headerItem;
    }

    @Override
    public boolean onItemClick(int position) {
        Log.d(TAG, "onItemClick");
        return false;
    }
    // =========================
// helper classes
// ===========================
    private class HeaderItem extends AbstractHeaderItem<AbstractFlexibleItem> {
        public HeaderItem(String id) {
            super(id);
        }

        @Override
        public int getLayoutRes() {
            return R.layout.header_item;
        }
    }

//    private void setAlarm(long delayMs, long intervalMs) {
//        Log.i(TAG, ".setAlarm: delayMs = " + delayMs + ", intervalMs = " + intervalMs);
//
//        Intent startIntent = new Intent(this, WalkerService.class);
//        startIntent.putExtra(WalkerService.HEARTBEAT_RESTART, Boolean.TRUE);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 56, startIntent, 0);
//        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMs, intervalMs, pendingIntent);
//    }

}
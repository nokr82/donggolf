package donggolf.android.base;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.location.DetectedActivity;

import java.util.concurrent.CountDownLatch;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;

public class SimpleLocator implements LocationBasedOnActivityProvider.LocationBasedOnActivityListener {
    private static final String TAG = SimpleLocator.class.getSimpleName();
    private static final int MAX_CONNECT_TIME = 5 * 1000;
    private static LocationManager locationManager;

    private Context context;
    private OnLocationUpdatedListener listener;

    private Handler stopLocUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // System.out.println("SmartLocation.with(context) : " + SmartLocation.with(context));
            // System.out.println(" SmartLocation.with(context).location() : " +  SmartLocation.with(context).location());

            SmartLocation.with(context).location().stop();

        }
    };

    private Handler locUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // stopLocUpdateHandler.sendEmptyMessage(0);

            SmartLocation smartLocation = new SmartLocation.Builder(context).logging(true).build();
            // smartLocation.location(new LocationBasedOnActivityProvider(SimpleLocator.this)).oneFix().start(listener);
            smartLocation.location(new LocationManagerProvider()).oneFix().start(listener);
        }
    };

    public SimpleLocator(Context context, OnLocationUpdatedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public static Location getLastLocation(final Context context) {

        // System.out.println("Start com.wecoms.core.device.gps.SimpleLocator.getLastLocation");

        final Location[] fetchedLocation = new Location[1];

        final CountDownLatch latch = new CountDownLatch(5);
        new Thread(new Runnable(){
            @Override
            public void run(){

                /*
                locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);

                String provider = locationManager.getBestProvider(criteria, true);

                // System.out.println("provider : " + provider);

                locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        fetchedLocation[0] = location;

                        // System.out.println("location at com.wecoms.core.device.gps.SimpleLocator.getLastLocation : " + location);

                        latch.countDown();
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
                */

                if(!SmartLocation.with(context).location(new LocationManagerProvider()).state().locationServicesEnabled()
                        && !SmartLocation.with(context).location(new LocationManagerProvider()).state().isAnyProviderAvailable()
                        && !SmartLocation.with(context).location(new LocationManagerProvider()).state().isGpsAvailable()
                        && !SmartLocation.with(context).location(new LocationManagerProvider()).state().isNetworkAvailable()) {

                    Location loc = new Location("dummyprovider");
                    loc.setLatitude(-1);
                    loc.setLongitude(-1);

                    fetchedLocation[0] = loc;

                    latch.countDown();
                } else {

                    SmartLocation smartLocation = new SmartLocation.Builder(context).logging(true).build();
                    SmartLocation.LocationControl locationControl = smartLocation.location(new LocationManagerProvider()).oneFix();

                    if(SmartLocation.with(context).location(new LocationManagerProvider()).state().isGpsAvailable()) {
                        LocationParams locationParams = (new LocationParams.Builder()).setAccuracy(LocationAccuracy.MEDIUM).build();
                        locationControl.config(locationParams);
                    } else if(SmartLocation.with(context).location(new LocationManagerProvider()).state().isNetworkAvailable()) {
                        LocationParams locationParams = (new LocationParams.Builder()).setAccuracy(LocationAccuracy.LOW).build();
                        locationControl.config(locationParams);
                    }

                    locationControl.start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            fetchedLocation[0] = location;

                            latch.countDown();

                        }
                    });
                }
            }
        }).start();

        try {

            latch.await();

            return fetchedLocation[0];

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // return dummy
        Location loc = new Location("dummyprovider");
        loc.setLatitude(-1);
        loc.setLongitude(-1);

        return loc;

        // startActivity(intent);

        // locUpdateHandler.sendEmptyMessage(0);

        // new LocationTask(context, listener).execute();

        /*
        Location lastLocation = null;
        SimpleLocator simpleLocator = new SimpleLocator(context);
        if (simpleLocator.connect() && simpleLocator.isAvaliable()) {
            lastLocation = simpleLocator.getLastLocation();
            simpleLocator.disconnet();
        }
        return lastLocation;
        */
    }

    @Override
    public LocationParams locationParamsForActivity(DetectedActivity detectedActivity) {

        LocationParams.Builder builder = new LocationParams.Builder();
        builder.setAccuracy(LocationAccuracy.HIGH);
        builder.setDistance(100);

        return builder.build();
    }

    public void stop() {
        // SmartLocation.with(context).location().stop();
    }

    static class LocationTask extends AsyncTask<String, Void, Void> implements LocationBasedOnActivityProvider.LocationBasedOnActivityListener {

        private Exception exception;
        private Context context;
        private  OnLocationUpdatedListener listener;

        public LocationTask(Context context, OnLocationUpdatedListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... strings) {
            // SmartLocation.with(context).location().stop();

            SmartLocation smartLocation = new SmartLocation.Builder(context).logging(true).build();
            smartLocation.location(new LocationBasedOnActivityProvider(this)).oneFix().start(listener);

            return null;
        }

        protected void onPostExecute(Void feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }

        @Override
        public LocationParams locationParamsForActivity(DetectedActivity detectedActivity) {
            return null;
        }
    }

















    /*
    public static boolean isAvailable(Context context) {
        boolean avaliable = false;
        SimpleLocator simpleLocator = new SimpleLocator(context);
        if (simpleLocator.connect()) {
            avaliable = simpleLocator.isAvaliable();
            simpleLocator.disconnet();
        }
        return avaliable;
    }

    private GoogleApiClient mGoogleApiClient;

    public SimpleLocator(Context context) {
        this.mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).build();
    }

    public boolean connect() {
        return connect(MAX_CONNECT_TIME);
    }

    public boolean connect(int blockingTime) {
        Log.e(TAG, "connect...");
        if (isConnected()) {
            Log.e(TAG, "already connected!!!");
        }

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(blockingTime, TimeUnit.MILLISECONDS);
        boolean success = connectionResult.isSuccess();
        if (success) {
            Log.e(TAG, "success connected!!!");
        } else {
            Log.e(TAG, "fail connected!!!(" + connectionResult + ")");
        }
        return success;
    }

    public void disconnet() {
        Log.e(TAG, "disconnect...");
        mGoogleApiClient.disconnect();
        Log.e(TAG, "success disconnect!!!");
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public Location getLastLocationOld() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public boolean isAvaliable() {
        Log.e(TAG, "check is Availability...");
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (locationAvailability == null) {
            Log.e(TAG, "locationAvailability is null!!!");
            return false;
        } else if (!locationAvailability.isLocationAvailable()) {
            Log.e(TAG, "location is not available!!!");
        } else {
            Log.e(TAG, "location is available!!!");
        }
        return true;
    }
    */
}
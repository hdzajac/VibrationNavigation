package com.navigation.vibration;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.navigation.vibration.models.VibrationConstants;
import com.navigation.vibration.models.VibrationPattern;
import com.navigation.vibration.service.BluetoothService;
import com.here.android.mpa.mapping.MapScreenMarker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class encapsulates the properties and functionality of the Map view.It also triggers a
 * turn-by-turn navigation from HERE Burnaby office to Langley BC.There is a sample voice skin
 * bundled within the SDK package to be used out-of-box, please refer to the Developer's guide for
 * the usage.
 */
public class MapFragmentView implements PositioningManager.OnPositionChangedListener, Map.OnTransformListener {
    private String TAG = "MapFragment";

    private MapFragment m_mapFragment;
    private MapsActivity m_activity;
    private Button m_naviControlButton;
    private Map m_map;
    private NavigationManager m_navigationManager;
    private GeoBoundingBox m_geoBoundingBox;
    private Route m_route;
    private GeoCoordinate m_current_location;
    private boolean paused;
    private boolean m_foregroundServiceStarted;
    // positioning manager instance
    // HERE location data source instance
    private LocationDataSourceHERE mHereLocation;
    // flag that indicates whether maps is being transformed
    private boolean mTransforming;
    private PositioningManager mPositioningManager;

    // callback that is called when transforming ends
    private Runnable mPendingUpdate;

    private Maneuver m_currentManeuver = null;

    private int noDevices;
    VibrationPattern chosenVibrationPattern;
    private List<MapMarker> markerList = new ArrayList<>();


    public MapFragmentView(MapsActivity activity, int vibrationId, int devices) {
        m_activity = activity;
        noDevices = devices;
        chosenVibrationPattern = VibrationConstants.getVibrationPattern(vibrationId);
        initMapFragment();
        initNaviControlButton();

    }
    // Resume positioning gestureListener on wake up

    private void initMapFragment() {
        /* Locate the mapFragment UI element */
        m_mapFragment = (MapFragment) m_activity.getFragmentManager()
                .findFragmentById(R.id.mapfragment);

        // Set path of isolated disk cache
        String diskCacheRoot = Environment.getExternalStorageDirectory().getPath()
                + File.separator + ".isolated-here-maps";
        // Retrieve intent name from manifest
        String intentName = "";
        try {
            ApplicationInfo ai = m_activity.getPackageManager().getApplicationInfo(m_activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            intentName = bundle.getString("INTENT_NAME");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.getClass().toString(), "Failed to find intent name, NameNotFound: " + e.getMessage());
        }

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(diskCacheRoot, intentName);
        if (!success) {
            // Setting the isolated disk cache was not successful, please check if the path is valid and
            // ensure that it does not match the default location
            // (getExternalStorageDirectory()/.here-maps).
            // Also, ensure the provided intent name does not match the default intent name.
        } else {
            if (m_mapFragment != null) {
                /* Initialize the MapFragment, results will be given via the called back. */
                m_mapFragment.init(new OnEngineInitListener() {
                    @Override
                    public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                        if (error == Error.NONE) {

                            //added gestureListener
                            m_mapFragment.getMapGesture().addOnGestureListener(new MyOnGestureListener());

                            m_map = m_mapFragment.getMap();

                            m_map.setCenter(m_current_location,
                                    Map.Animation.NONE);
                            //Put this call in Map.onTransformListener if the animation(Linear/Bow)
                            //is used in setCenter()
                            m_map.setZoomLevel(13.2);
                            m_map.setMapScheme(Map.Scheme.CARNAV_DAY);
                            /*
                             * Get the NavigationManager instance.It is responsible for providing voice
                             * and visual instructions while driving and walking
                             */
                            m_navigationManager = NavigationManager.getInstance();
                            m_map.addTransformListener(MapFragmentView.this);
                            mPositioningManager = PositioningManager.getInstance();
                            mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(
                                    MapFragmentView.this));
                            // start position updates, accepting GPS, network or indoor positions
                            if (mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK)) {
                                m_map.getPositionIndicator().setVisible(true);
                            } else {
                                Log.e("Map", "Unable to start position manager");
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "ERROR: Cannot initialize Map with error " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    public void createRoute(LatLng destinationLitLng) {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption.HERE SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);
        /* Disable highway in this route. */
        routeOptions.setHighwaysAllowed(false);
        /* Calculate the shortest route available. */

        //WHAT DOES BALANCED MEAN
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        RouteWaypoint startPoint = new RouteWaypoint(m_current_location);
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(destinationLitLng.latitude, destinationLitLng.longitude));

        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);

        //Add coordinates from markers
        for (MapMarker marker : markerList) {
            routePlan.addWaypoint(new RouteWaypoint(marker.getCoordinate()));
        }

        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the gestureListener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {

                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                         RoutingError routingError) {
                        /* Calculation is done.Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            if (routeResults.get(0).getRoute() != null) {
                                RouteResult routeResult = routeResults.get(0);


                                m_route = routeResult.getRoute();
                                /* Create a MapRoute so that it can be placed on the map */
                                MapRoute mapRoute = new MapRoute(routeResult.getRoute());
                                mapRoute.setColor(Color.BLUE);

                                /* Show the maneuver number on top of the route */
                                mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                m_map.addMapObject(mapRoute);

                                m_currentManeuver = m_route.getFirstManeuver();
                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                m_geoBoundingBox = routeResult.getRoute().getBoundingBox();
                                m_map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);

                                startNavigation();

                            } else {
                                Toast.makeText(m_activity,
                                        "Error:route results returned is not valid",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void initNaviControlButton() {
        Button upButton = (Button) m_activity.findViewById(R.id.naviUp);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate(VibrationConstants.AHEAD);
            }
        });
        Button downButton = (Button) m_activity.findViewById(R.id.naviDown);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate(VibrationConstants.BACK);
            }
        });
        Button leftButton = (Button) m_activity.findViewById(R.id.naviLeft);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate(VibrationConstants.LEFT);
            }
        });
        Button rightButton = (Button) m_activity.findViewById(R.id.naviRight);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate(VibrationConstants.RIGHT);
            }
        });
        m_naviControlButton = (Button) m_activity.findViewById(R.id.naviCtrlButton);
        m_naviControlButton.setText(R.string.start_navi);
        m_naviControlButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                /*
                 * To start a turn-by-turn navigation, a concrete route object is required.We use
                 * the same steps from Routing sample app to create a route from 4350 Still Creek Dr
                 * to Langley BC without going on HWY.
                 *
                 * The route calculation requires local map data.Unless there is pre-downloaded map
                 * data on device by utilizing MapLoader APIs,it's not recommended to trigger the
                 * route calculation immediately after the MapEngine is initialized.The
                 * INSUFFICIENT_MAP_DATA error code may be returned by CoreRouter in this case.
                 *
                 */

                //m_navigationManager.stop();
                /*
                 * Restore the map orientation to show entire route on screen
                 */
                if (m_route == null) {
                    //if destination missing choose last added marker as destination
                    GeoCoordinate geo = markerList.get(markerList.size() - 1).getCoordinate();
                    createRoute(new LatLng(geo.getLatitude(), geo.getLongitude()));
                } else {
                    m_navigationManager.stop();
                    /*
                     * Restore the map orientation to show entire route on screen
                     */
                    m_map.zoomTo(m_geoBoundingBox, Map.Animation.NONE, 0f);
                    m_naviControlButton.setText(R.string.start_navi);
                    m_route = null;
                }

            }
        });
    }

    private void startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true;
            Intent startIntent = new Intent(m_activity, ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            m_activity.getApplicationContext().startService(startIntent);
        }
    }

    private void stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false;
            Intent stopIntent = new Intent(m_activity, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
            m_activity.getApplicationContext().startService(stopIntent);
        }
    }

    private void startNavigation() {
        m_naviControlButton.setText(R.string.stop_navi);
        /* Display the position indicator on map */
        m_map.getPositionIndicator().setVisible(true);
        /* Configure Navigation manager to launch navigation on current map */
        m_navigationManager.setMap(m_map);

        /*
         * Start the turn-by-turn navigation.Please note if the transport mode of the passed-in
         * route is pedestrian, the NavigationManager automatically triggers the guidance which is
         * suitable for walking. Simulation and tracking modes can also be launched at this moment
         * by calling either simulate() or startTracking()
         */

        /* Choose navigation modes between real time navigation and simulation */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_activity);
        alertDialogBuilder.setTitle("Navigation");
        alertDialogBuilder.setMessage("Choose Mode");
        alertDialogBuilder.setNegativeButton("Navigation", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                m_navigationManager.startNavigation(m_route);
                m_map.setTilt(60);
                startForegroundService();
            }

            ;
        });
        alertDialogBuilder.setPositiveButton("Simulation", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                m_navigationManager.simulate(m_route, 20);//Simualtion speed is set to 500 m/s
                m_map.setTilt(60);
                startForegroundService();
            }

            ;
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        /*
         * Set the map update mode to ROADVIEW.This will enable the automatic map movement based on
         * the current location.If user gestures are expected during the navigation, it's
         * recommended to set the map update mode to NONE first. Other supported update mode can be
         * found in HERE Android SDK API doc
         */
        m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
        /*
         * NavigationManager contains a number of listeners which we can use to monitor the
         * navigation status and getting relevant instructions.In this example, we will add 2
         * listeners for demo purpose,please refer to HERE Android SDK API documentation for details
         */
        addNavigationListeners();
    }

    private class MyOnGestureListener implements MapGesture.OnGestureListener {

        @Override
        public void onPanStart() {
        }

        @Override
        public void onPanEnd() {
        }

        @Override
        public void onMultiFingerManipulationStart() {
        }

        @Override
        public void onMultiFingerManipulationEnd() {
        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> objects) {
            for (ViewObject viewObj : objects) {
                if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                    if (((MapObject) viewObj).getType() == MapObject.Type.ROUTE) {
                        // At this point we have the originally added
                        // map marker, so we can do something with it
                        // (like change the visibility, or more
                        // marker-specific actions)
                        //((MapRoute) viewObj).setVisible(false);

                        //Here if you want to add a marker and it is to close to another one it will remove it and add the new one
                        m_map.removeMapObject((MapObject) viewObj);

                    } else if (((MapObject) viewObj).getType() == MapObject.Type.MARKER) {
                        MapMarker marker = (MapMarker) viewObj;
                        markerList.remove(marker);
                        m_map.removeMapObject(marker);
                    }
                }
            }
//            // return false to allow the map to handle this callback also
            return false;
        }

        @Override
        public boolean onTapEvent(PointF p) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF p) {
            return false;
        }

        @Override
        public void onPinchLocked() {
        }

        @Override
        public boolean onPinchZoomEvent(float scaleFactor, PointF p) {
            return false;
        }

        @Override
        public void onRotateLocked() {
        }

        @Override
        public boolean onRotateEvent(float rotateAngle) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float angle) {
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF p) {
            MapMarker marker;
            GeoCoordinate position = m_map.pixelToGeo(p);
            Image m_marker_image = new Image();
            try {
                m_marker_image.setImageResource(R.drawable.marker);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error image marker.");
            }

            //marker.setCoordinate(new GeoCoordinate(p.x,p.y));
            marker = new MapMarker(position, m_marker_image);
            //marker.setAnchorPoint(p);

            markerList.add(marker);
            m_map.addMapObject(marker);
            // m_tap_marker.setScreenCoordinate(pointF);
            Log.v(TAG, "Added map marker.");
            return false;
        }

        @Override
        public void onLongPressRelease() {
        }

        @Override
        public boolean onTwoFingerTapEvent(PointF p) {
            return false;
        }
    }

    private void addNavigationListeners() {

        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager.addNavigationManagerEventListener(
                new WeakReference<NavigationManager.NavigationManagerEventListener>(
                        m_navigationManagerEventListener));

        /* Register a PositionListener to monitor the position updates */
        m_navigationManager.addPositionListener(
                new WeakReference<NavigationManager.PositionListener>(m_positionListener));


        m_navigationManager.addNewInstructionEventListener(
                new WeakReference<NavigationManager.NewInstructionEventListener>(m_instructionEventListener));
    }

    private NavigationManager.PositionListener m_positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            /* Current position information can be retrieved in this callback */
            //m_map.setCenter(geoPosition.getCoordinate(),
            //        Map.Animation.NONE);
        }
    };
    private NavigationManager.NewInstructionEventListener m_instructionEventListener = new NavigationManager.NewInstructionEventListener() {
        @Override
        public void onNewInstructionEvent() {
            super.onNewInstructionEvent();
            final Maneuver maneuver = m_navigationManager.getNextManeuver();

            int turn = -1;
            if (maneuver != null) {
                if (maneuver.getAction() == Maneuver.Action.END) {
                    //notify the user that the route is complete
                }
                //switch (m_currentManeuver.getTurn()) {
                switch (m_currentManeuver.getTurn()) {
                    case QUITE_RIGHT: //A turn that indicates making a normal right turn.
                        turn = VibrationConstants.RIGHT;
                        Log.v(TAG, "QUITE_RIGHT");
                        break;
                    case LIGHT_RIGHT: //A turn that indicates making a light right turn.
                        turn = VibrationConstants.RIGHT;
                        Log.v(TAG, "LIGHT_RIGHT");
                        break;
                    case KEEP_RIGHT: //A turn that indicates keeping to the right when a road forks.
                        turn = VibrationConstants.RIGHT;
                        Log.v(TAG, "KEEP_RIGHT");
                        break;
                    case HEAVY_RIGHT: //A turn that indicates making a heavy right turn.
                        Log.v(TAG, "HEAVY_RIGHT");
                        turn = VibrationConstants.RIGHT;
                        break;
                    case QUITE_LEFT: //A turn that indicates making a normal left turn.
                        Log.v(TAG, "QUITE_LEFT");
                        turn = VibrationConstants.LEFT;
                        break;
                    case LIGHT_LEFT: //A turn that indicates making a light left turn.
                        Log.v(TAG, "LIGHT_LEFT");
                        turn = VibrationConstants.LEFT;
                        break;
                    case KEEP_LEFT: //A turn that indicates keeping to the left when a road forks.
                        Log.v(TAG, "KEEP_LEFT");
                        turn = VibrationConstants.LEFT;
                        break;
                    case HEAVY_LEFT: //A turn that indicates making a heavy left turn.
                        Log.v(TAG, "HEAVY_LEFT");
                        turn = VibrationConstants.LEFT;
                        break;
                    case RETURN: //A turn that indicates turning around or making a U-turn.
                        Log.v(TAG, "RETURN");
                        turn = VibrationConstants.BACK;
                        break;
                    case KEEP_MIDDLE: //A turn that indicates keeping to the middle when a road forks.
                        Log.v(TAG, "KEEP_MIDDLE");
                        turn = VibrationConstants.AHEAD;
                        break;
                    case NO_TURN: //Indicates that no turn is necessary.
                        Log.v(TAG, "NO_TURN");
                        turn = VibrationConstants.AHEAD;
                        break;
                    default:
                        Log.v(TAG, maneuver.getTurn().toString());
                        break;
                }
                Log.v(TAG,"action "+m_currentManeuver.getAction().toString());
                vibrate(turn);
                m_currentManeuver = maneuver;
            }
        }
    };

    private NavigationManager.NavigationManagerEventListener m_navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onRunningStateChanged() {
            //Toast.makeText(m_activity, "Running state changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNavigationModeChanged() {
            //Toast.makeText(m_activity, "Navigation mode changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
            //Toast.makeText(m_activity, navigationMode + " was ended", Toast.LENGTH_SHORT).show();
            //stopForegroundService();
        }

        @Override
        public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
            //Toast.makeText(m_activity, "Map update mode is changed to " + mapUpdateMode,
            //        Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRouteUpdated(Route route) {
            //Toast.makeText(m_activity, "Route updated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCountryInfo(String s, String s1) {
            Toast.makeText(m_activity, "Country info updated from " + s + " to " + s1,
                    Toast.LENGTH_SHORT).show();
        }
    };

    public void onDestroy() {
        /* Stop the navigation when app is destroyed */
        if (m_navigationManager != null) {
            stopForegroundService();
            m_navigationManager.stop();
        }
    }

    @Override
    public void onPositionUpdated(final PositioningManager.LocationMethod locationMethod, final GeoPosition geoPosition, final boolean mapMatched) {
        final GeoCoordinate coordinate = geoPosition.getCoordinate();

        m_current_location = coordinate;
        if (mTransforming) {
            mPendingUpdate = new Runnable() {
                @Override
                public void run() {
                    onPositionUpdated(locationMethod, geoPosition, mapMatched);
                }
            };
        } else {
            //m_map.setCenter(coordinate, Map.Animation.BOW);
            //updateLocationInfo(locationMethod, geoPosition);
        }
    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
        //
    }

    @Override
    public void onMapTransformStart() {
        mTransforming = true;

    }


    @Override
    public void onMapTransformEnd(MapState mapState) {
        mTransforming = false;
        if (mPendingUpdate != null) {
            mPendingUpdate.run();
            mPendingUpdate = null;
        }
    }

    //BACK and ahead always sent to both devices
    private void vibrate(int position) {
        byte tag = 0;
        byte[] msg = new byte[1];
        String log_msg = "";

        switch (position) {
            case VibrationConstants.AHEAD: //-1 means repeats once
                tag = VibrationConstants.pickVibrationTag(chosenVibrationPattern.getPatternAhead());
                Log.d(TAG, "vibration tag: " + tag);
                msg[0] = tag;

                if (noDevices == 1) {
                    log_msg = "Sending to right vibration ahead "  + msg[0];
                    Log.d(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);
                } else {
                    log_msg = "Sending to both vibration ahead";
                    Log.d(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.LEFT, msg);
                    BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);
                }
                break;
            case VibrationConstants.RIGHT:
                tag = VibrationConstants.pickVibrationTag(chosenVibrationPattern.getPatternRight());

                msg[0] = tag;
                log_msg = "Sending to right vibration right";
                Log.v(TAG, log_msg);

                //do not check for number of devices since default is right
                Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);

                break;
            case VibrationConstants.LEFT:
                tag = VibrationConstants.pickVibrationTag(chosenVibrationPattern.getPatternLeft());
                msg[0] = tag;

                if (noDevices == 1) {
                    log_msg = "Sending to right vibration left";
                    Log.v(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);
                } else {
                    log_msg = "Sending to left vibration left";
                    Log.v(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.LEFT, msg);
                }
                break;
            case VibrationConstants.BACK:
                tag = VibrationConstants.pickVibrationTag(chosenVibrationPattern.getPatternBack());
                msg[0] = tag;

                if (noDevices == 1) {
                    log_msg = "Sending to right vibration back";
                    Log.v(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);
                } else {
                    log_msg = "Sending to both vibration back";
                    Log.v(TAG, log_msg);
                    Toast.makeText(m_activity, log_msg, Toast.LENGTH_SHORT).show();
                    BluetoothService.getInstance().write(VibrationConstants.LEFT, msg);
                    BluetoothService.getInstance().write(VibrationConstants.RIGHT, msg);
                }
                break;
            default:
                log_msg = "Unknown turn value " + tag;
                //Toast.makeText(m_activity,log_msg,Toast.LENGTH_SHORT);
                Log.i(TAG, log_msg);
                break;
        }
    }


}
package nl.noik.cellinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {
    private String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private static final String TAG = "CellInfo";

    private static final String APP_NAME = "SignalLevelSample";
    private static final int EXCELLENT_LEVEL = 75;
    private static final int GOOD_LEVEL = 50;
    private static final int MODERATE_LEVEL = 25;
    private static final int WEAK_LEVEL = 0;

    private static final int INFO_SERVICE_STATE_INDEX = 0;
    private static final int INFO_CELL_LOCATION_INDEX = 1;
    private static final int INFO_CALL_STATE_INDEX = 2;
    private static final int INFO_CONNECTION_STATE_INDEX = 3;
    private static final int INFO_SIGNAL_LEVEL_INDEX = 4;
    private static final int INFO_SIGNAL_LEVEL_INFO_INDEX = 5;
    private static final int INFO_DATA_DIRECTION_INDEX = 6;
    private static final int INFO_DEVICE_INFO_INDEX = 7;

    private static final int[] info_ids= {
            R.id.serviceState_info,
            R.id.cellLocation_info,
            R.id.callState_info,
            R.id.connectionState_info,
            R.id.signalLevel,
            R.id.signalLevelInfo,
            R.id.dataDirection,
            R.id.device_info
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitles = getResources().getStringArray(R.array.menu_array);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                Log.d(TAG, "onDrawerClosed");
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                Log.d(TAG, "onDrawerOpened");
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        startSignalLevelListener();
        displayTelephonyInfo();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        startSignalLevelListener();
    }

    @Override
    protected void onDestroy()
    {
        stopListening();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate");
        super.onCreate(savedInstanceState);
        //sync the toggle state after onRestoreInstanceState has occured
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick/DrawerItemClickListener");
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        Log.d(TAG, "selectItem");
        // Create a new fragment and specify the planet to show based on position
        //Fragment fragment = new PlanetFragment();
        //Bundle args = new Bundle();
        //args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        //fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        getFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment())
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        Log.d(TAG, "setTitle");
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView");
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    //Telephony things.....

    private void setTextViewText(int id,String text) {
        ((TextView)findViewById(id)).setText(text);
    }
    private void setSignalLevel(int id,int infoid,int level){
        int progress = (int) ((((float)level)/31.0) * 100);
        String signalLevelString =getSignalLevelString(progress);
        ((ProgressBar)findViewById(id)).setProgress(progress);
        ((TextView)findViewById(infoid)).setText(signalLevelString);
        Log.i("signalLevel ","" + progress);
    }

    private String getSignalLevelString(int level) {
        String signalLevelString = "Weak";
        if(level > EXCELLENT_LEVEL)     signalLevelString = "Excellent";
        else if(level > GOOD_LEVEL)     signalLevelString = "Good";
        else if(level > MODERATE_LEVEL) signalLevelString = "Moderate";
        else if(level > WEAK_LEVEL)     signalLevelString= "Weak";
        return signalLevelString;
    }

    private void stopListening(){
        TelephonyManager tm = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);
        tm.listen(phoneStateListener,
                PhoneStateListener.LISTEN_NONE);
    }

    private void setDataDirection(int id, int direction){
        int resid = getDataDirectionRes(direction);
        ((ImageView)findViewById(id)).setImageResource(resid);
    }
    private int getDataDirectionRes(int direction){
        /*int resid = R.drawable.data_none;

        switch(direction)
        {
            case TelephonyManager.DATA_ACTIVITY_IN:
                resid = R.drawable.data_in; break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                resid = R.drawable.data_out; break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                resid = R.drawable.data_both; break;
            case TelephonyManager.DATA_ACTIVITY_NONE:
                resid = R.drawable.data_none; break;
            default: resid = R.drawable.data_none; break;
        }
        return resid;*/
        return 2;
    }
    private void startSignalLevelListener() {
        TelephonyManager tm = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);
        int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTH | PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_CELL_LOCATION|PhoneStateListener.LISTEN_CALL_STATE |
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                PhoneStateListener.LISTEN_SERVICE_STATE;
        tm.listen(phoneStateListener, events);
    }
    private void displayTelephonyInfo(){
        TelephonyManager tm = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);
        GsmCellLocation loc = (GsmCellLocation)tm.getCellLocation();
        int cellid = loc.getCid();
        int lac = loc.getLac();
        String deviceid = tm.getDeviceId();
        String phonenumber = tm.getLine1Number();
        String softwareversion = tm.getDeviceSoftwareVersion();
        String operatorname = tm.getNetworkOperatorName();
        String simcountrycode = tm.getSimCountryIso();
        String simoperator = tm.getSimOperatorName();
        String simserialno = tm.getSimSerialNumber();
        String subscriberid = tm.getSubscriberId();
        String networktype = getNetworkTypeString(tm.getNetworkType());
        String phonetype = getPhoneTypeString(tm.getPhoneType());
        logString("CellID: " + cellid);
        logString("LAC: " + lac);
        logString("Device ID: " + deviceid);
        logString("Phone Number: " + phonenumber);
        logString("Software Version: " + softwareversion);
        logString("Operator Name: " + operatorname);
        logString("SIM Country Code: " + simcountrycode);
        logString("SIM Operator: " + simoperator);
        logString("SIM Serial No.: " + simserialno);
        logString("Sibscriber ID: " + subscriberid);
        String deviceinfo = "";
        deviceinfo += ("CellID: " + cellid + "\n");
        deviceinfo += ("LAC: " + lac + "\n");
        deviceinfo += ("Device ID: " + deviceid + "\n");
        deviceinfo += ("Phone Number: " + phonenumber + "\n");
        deviceinfo += ("Software Version: " + softwareversion + "\n");
        deviceinfo += ("Operator Name: " + operatorname + "\n");
        deviceinfo += ("SIM Country Code: " + simcountrycode + "\n");
        deviceinfo += ("SIM Operator: " + simoperator + "\n");
        deviceinfo += ("SIM Serial No.: " + simserialno + "\n");
        deviceinfo += ("Subscriber ID: " + subscriberid + "\n");
        deviceinfo += ("Network Type: " + networktype + "\n");
        deviceinfo += ("Phone Type: " + phonetype + "\n");
        List<NeighboringCellInfo> cellinfo =tm.getNeighboringCellInfo();
        if(null != cellinfo){
            for(NeighboringCellInfo info: cellinfo){
                deviceinfo += ("\tCellID: " + info.getCid() +", RSSI: " + info.getRssi() + "\n");
            }
        }
        setTextViewText(info_ids[INFO_DEVICE_INFO_INDEX],deviceinfo);
    }
    private String getNetworkTypeString(int type){
        String typeString;
        switch(type)
        {
            case TelephonyManager.NETWORK_TYPE_EDGE:typeString = "EDGE"; break;
            case TelephonyManager.NETWORK_TYPE_GPRS:typeString = "GPRS"; break;
            case TelephonyManager.NETWORK_TYPE_UMTS:typeString = "UMTS"; break;
            default:
                typeString = "UNKNOWN"; break;
        }
        return typeString;
    }
    private String getPhoneTypeString(int type){
        String typeString;
        switch(type)
        {
            case TelephonyManager.PHONE_TYPE_GSM: typeString = "GSM"; break;
            case TelephonyManager.PHONE_TYPE_NONE: typeString = "UNKNOWN"; break;
            default:typeString = "UNKNOWN"; break;
        }
        return typeString;
    }
    private int logString(String message) {
        return Log.i(APP_NAME,message);
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener(){

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi)
        {
            Log.i(APP_NAME, "onCallForwardingIndicatorChanged " +cfi);
            super.onCallForwardingIndicatorChanged(cfi);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            String callState = "UNKNOWN";
            switch(state)
            {
                case TelephonyManager.CALL_STATE_IDLE:callState = "IDLE"; break;
                case TelephonyManager.CALL_STATE_RINGING:callState = "Ringing (" + incomingNumber + ")"; break;
                case TelephonyManager.CALL_STATE_OFFHOOK:       callState = "Offhook"; break;
            }
            setTextViewText(info_ids[INFO_CALL_STATE_INDEX],callState);
            Log.i(APP_NAME, "onCallStateChanged " + callState);
            super.onCallStateChanged(state, incomingNumber);
        }
        @Override
        public void onCellLocationChanged(CellLocation location)
        {
            String locationString = location.toString();
            setTextViewText(info_ids[INFO_CELL_LOCATION_INDEX],locationString);

            Log.i(APP_NAME, "onCellLocationChanged " +
                    locationString);
            super.onCellLocationChanged(location);
        }

        @Override
        public void onDataActivity(int direction)
        {
            String directionString = "none";
            switch(direction)
            {
                case TelephonyManager.DATA_ACTIVITY_IN:
                    directionString = "IN"; break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    directionString = "OUT"; break;
                case
                        TelephonyManager.DATA_ACTIVITY_INOUT:      directionString = "INOUT";
                    break;
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    directionString = "NONE"; break;
                default: directionString = "UNKNOWN: " +
                        direction; break;
            }

            setDataDirection(info_ids[INFO_DATA_DIRECTION_INDEX],direction);
            Log.i(APP_NAME, "onDataActivity " +
                    directionString);
            super.onDataActivity(direction);
        }

        @Override
        public void onDataConnectionStateChanged(int state)
        {
            String connectionState = "Unknown";
            switch(state)
            {
                case TelephonyManager.DATA_CONNECTED:
                    connectionState = "Connected"; break;
                case TelephonyManager.DATA_CONNECTING:
                    connectionState = "Connecting"; break;
                case TelephonyManager.DATA_DISCONNECTED:
                    connectionState = "Disconnected"; break;
                case TelephonyManager.DATA_SUSPENDED:
                    connectionState = "Suspended"; break;
                default:

                    connectionState = "Unknown: " +
                            state; break;
            }



            setTextViewText(info_ids[INFO_CONNECTION_STATE_INDEX],connectionState);

            Log.i(APP_NAME, "onDataConnectionStateChanged " +
                    connectionState);

            super.onDataConnectionStateChanged(state);
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi)
        {
            Log.i(APP_NAME, "onMessageWaitingIndicatorChanged" + mwi);
            super.onMessageWaitingIndicatorChanged(mwi);
        }

        @Override
        public void onServiceStateChanged(ServiceState
                                                  serviceState)
        {
            String serviceStateString = "UNKNOWN";
            switch(serviceState.getState())
            {
                case ServiceState.STATE_IN_SERVICE:
                    serviceStateString = "IN SERVICE"; break;
                case ServiceState.STATE_EMERGENCY_ONLY:
                    serviceStateString = "EMERGENCY ONLY"; break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                    serviceStateString = "OUT OF SERVICE"; break;
                case ServiceState.STATE_POWER_OFF:
                    serviceStateString = "POWER OFF"; break;
                default:

                    serviceStateString = "UNKNOWN";
                    break;
            }



            setTextViewText(info_ids[INFO_SERVICE_STATE_INDEX],serviceStateString);

            Log.i(APP_NAME, "onServiceStateChanged " +
                    serviceStateString);

            super.onServiceStateChanged(serviceState);
        }

        @Override
        public void onSignalStrengthChanged(int asu)
        {
            Log.i(APP_NAME, "onSignalStrengthChanged " +
                    asu);
            setSignalLevel(info_ids[INFO_SIGNAL_LEVEL_INDEX],info_ids[INFO_SIGNAL_LEVEL_INFO_INDEX],asu);
            super.onSignalStrengthChanged(asu);
        }
    };
}

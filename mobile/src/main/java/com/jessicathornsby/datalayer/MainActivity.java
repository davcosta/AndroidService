package com.jessicathornsby.datalayer;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.IBinder;
/*import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ServiceCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;*/
import android.content.BroadcastReceiver;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.actions.ItemListIntents;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

public class MainActivity extends AccessibilityService {

    private String hostName = "192.168.1.4"; //192.168.43.87 - IP FCUL //192.168.1.7 - IP DESKTOP //192.168.1.3 - IP PORTATIL
    private int portNumber = 5555;
    private Socket kkSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String gestureFromWatch = "";
    private ArrayList<Gesture> gestureBuffer = new ArrayList<Gesture>();
    private Context context;
    private String fromServer;

    private boolean isVirtualMenu = false;
    com.jessicathornsby.datalayer.Menu menu;
    Button talkbutton;
    TextView textview;
    protected Handler myHandler;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;



    private static final String TAG = "BackgroundService";
    private static final int DROP_NOTIFICATIONS = 1000;
    private static final int SCROLL_RIGHT = 1001;
    private static final int SCROLL_LEFT = 1002;
    private static final int BACK_BUTTON = 1003;
    private static final int HOME_BUTTON = 1004;
    private static final int BACKGROUND_BUTTON = 1004;

    private Toast mToast;
    private ArrayList<AccessibilityNodeInfo> ItemsList = new ArrayList<AccessibilityNodeInfo>();
    private ArrayList<AccessibilityNodeInfo> ItemsListRoot = new ArrayList<AccessibilityNodeInfo>();
    private ArrayList<AccessibilityNodeInfo> ItemsListRootNotVisible = new ArrayList<AccessibilityNodeInfo>();
    private ArrayList<AccessibilityNodeInfo> ItemsListRootNoFilter = new ArrayList<AccessibilityNodeInfo>();
    private List<AccessibilityWindowInfo> WindowList = new ArrayList<AccessibilityWindowInfo>();
    private static final String TASK_LIST_VIEW_CLASS_NAME =
            "com.example.android.apis.accessibility.TaskListView";

    private float roll = 0;
    private float yaw = 0;
    private float pitch = 0;

    private int roll_w = 0;
    private int yaw_w = 0;
    private int pitch_w = 0;

    private Pose _pose = Pose.REST;
    private int currentWin = 0;
    private AccessibilityNodeInfo tempInfo;
    private AccessibilityNodeInfo LastNode;
    private int previousroll = 0;
    private int normalizationPitch = 0;
    private int counter = 0;
    private AccessibilityNodeInfo firstNotVisible;
    private int counterFists = 0;


    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    private EventInput ei;
    private boolean cameFromKeyboard = false;
    private boolean isConnected = false;

    private Menu mNavigationViewMenu;
    private List<Node> mNodes;
    //private NavigationView mNavigationView;

    ArrayList<AccessibilityNodeInfo> lastUI = new ArrayList<AccessibilityNodeInfo>();
    private boolean gestureActivated = false;
    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    /*private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast(getString(R.string.connected));
            WindowList = getWindows();
            currentWin = 0;
            Log.i("INICIO", "numero de janelas: "+WindowList.size());
            if(WindowList.size() != 0) {
                logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                Log.i("LOG_TAG", "ENDLOG");
                if(ItemsListRoot.size() != 0) {
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                }else {
                    while(ItemsListRoot.size() == 0)
                    {
                        Log.d("ON CONNECT", "ON CONNECT window size: "+WindowList.size()+ "current window: "+currentWin);
                        if(currentWin < WindowList.size() -1)
                            currentWin++;
                        else
                            currentWin = 0;
                        logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                    }
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                }
                LastNode = ItemsListRoot.get(0);
            }
            isConnected = true;

            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);

        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            showToast(getString(R.string.disconnected));
            isConnected = false;
        }
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {

            counter++;

            boolean scroll = false;
            roll = (float) Math.atan2( 2.0f * (rotation.w() * rotation.x() + rotation.y() * rotation.z()),
                    1.0f - 2.0f * (rotation.x() * rotation.x() + rotation.y() * rotation.y()));
            pitch = (float)Math.asin(Math.max(-1.0f, Math.min(1.0f, 2.0f * (rotation.w() * rotation.y() - rotation.z() * rotation.x()))));
            yaw = (float )Math.atan2(2.0f * (rotation.w() * rotation.z() + rotation.x() * rotation.y()),
                    1.0f - 2.0f * (rotation.y() * rotation.y() + rotation.z() * rotation.z()));


            roll_w = (int)((roll + (float)Math.PI)/(Math.PI * 2.0f) * 18);
            pitch_w = (int)((pitch + (float)Math.PI/2.0f)/Math.PI * 18);
            yaw_w = (int)((yaw + (float)Math.PI)/(Math.PI * 2.0f) * 18);
            int diff = (pitch_w - normalizationPitch);
            if(_pose == Pose.FINGERS_SPREAD && counter >= 60 && tempInfo != null) {

                showToast("Entrou no if: roll- " + roll_w + " yaw- " + yaw_w + " pitch- " + pitch_w);
                Log.i("scroll","conta: "+diff);
                if (diff > 0)
                    tempInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                else if (diff < 0 )
                    tempInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                counter =0;
                tempInfo = ItemsList.get(1).getParent();
                boolean isScrollable = false;
                while(!isScrollable)
                {
                    if(tempInfo != null && tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") != 0 && tempInfo.getClassName().toString().compareTo("android.widget.ListView") != 0) {
                        Log.i("Description", " "+tempInfo.getClassName()+" "+tempInfo.toString()+""+tempInfo);
                        tempInfo = tempInfo.getParent();
                    }

                    else if(tempInfo != null && (tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") == 0 || tempInfo.getClassName().toString().compareTo("android.widget.ListView") == 0))
                        isScrollable = true;
                    else if(tempInfo != null && tempInfo.equals(tempInfo.getWindow().getRoot()))
                        break;
                    else if(tempInfo == null)
                        break;
                }

            }else if(!scroll && _pose == Pose.FINGERS_SPREAD && currentWin == 0)
                performGlobalActions(DROP_NOTIFICATIONS);
            else if( counter >= 60 && _pose == Pose.FINGERS_SPREAD) {
                counter = 0;
                if (diff > 0){


                    Log.i("SCROLLRIGHT", "currentWin: "+currentWin+" ItemList size: "+ItemsList.size()+" ");

                    AccessibilityNodeInfo lastSibling = ItemsList.get(1).getParent().getChild(ItemsList.get(1).getParent().getChildCount() - 1);
                    Log.i("Last", "Last info: " + lastSibling.getText() + " " + lastSibling.getContentDescription());

                    int i = ItemsListRootNotVisible.indexOf(lastSibling)+1;


                    while(i < ItemsListRootNotVisible.size() && (ItemsListRootNotVisible.get(i).getContentDescription() == null && ItemsListRootNotVisible.get(i).getText() == null || ItemsListRootNotVisible.get(i).getClassName().toString().compareTo("android.widget.ImageView")  == 0 ))
                    {
                        i++;
                    }
                    if(i >= ItemsListRootNotVisible.size()){
                        lastSibling.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        lastSibling.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        //lastSibling.performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                        scroll = true;
                    }else{
                        ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        //ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                        scroll = true;
                        Log.i("Last", "NotVisible info: " + ItemsListRootNotVisible.get(i).getText() + " " + ItemsListRootNotVisible.get(i).getContentDescription());
                    }

                }else if(diff < 0)
                {
                    AccessibilityNodeInfo firstSibling = ItemsList.get(1).getParent().getChild(0);
                    Log.i("Last", "Last info: " + firstSibling.getText() + " " + firstSibling.getContentDescription());
                    int i = ItemsListRootNotVisible.indexOf(firstSibling) - 1;

                    while(i > 0 && (ItemsListRootNotVisible.get(i).getContentDescription() == null && ItemsListRootNotVisible.get(i).getText() == null || ItemsListRootNotVisible.get(i).getClassName().toString().compareTo("android.widget.ImageView")  == 0) )
                    {
                        i--;
                    }
                    if(i < 0){
                        firstSibling.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        //firstSibling.performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                        scroll = true;
                    }else{
                        ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        //ItemsListRootNotVisible.get(i).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                        scroll = true;
                        Log.i("Last", "NotVisible info: " + ItemsListRootNotVisible.get(i).getText() + " " + ItemsListRootNotVisible.get(i).getContentDescription());
                    }

                }

            }
        }*/
        // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
        // making a fist, or not making a fist anymore.
       /* @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Show the name of the pose in a toast.
            _pose = pose;
            Log.i("AccessibiltiServiceApp"," POSE: "+pose.name()+"ItemList: "+ItemsList.size()+" ItemsListRoot: "+ItemsListRoot.size()+" WindowsList: "+WindowList.size()+" currentWindow: "+currentWin);
           /* if(ItemsList.isEmpty() && ItemsListRoot.isEmpty()) //NAO DESCOMENTAR ESTA PARTE --
            {
                Log.i("AccessibiltiServiceApp","ItemsList and Root Null");
                logNodeHeirarchy(getWindows().get(currentWin).getRoot(),0);
                if(!ItemsListRoot.isEmpty())
                {
                    Log.i("AccessibiltiServiceApp","ItemsListRoot Not Null");
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                }
            }*/ //--NAO DESCOMENTAR ESTA PARTE
           /* try {

                if(myo.getArm() == Arm.LEFT)
                {
                    if(pose == Pose.WAVE_IN)
                        pose = Pose.WAVE_OUT;
                    else if(pose == Pose.WAVE_OUT)
                        pose = Pose.WAVE_IN;
                }

                switch (pose) {
                    case WAVE_IN:
                        showToast("Navegar para trás");
                        if(!isVirtualMenu) {
                            LastNode = ItemsList.get(1);
                            if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") == 0) {
                                WindowList = getWindows();
                                if (WindowList.size() > 3)
                                    currentWin = 3;
                            }
                            Log.i("WAVE IN", "elemento anterior: " + ItemsList.get(0).getText() + " " + ItemsList.get(0).getContentDescription() + " elemento actual: " + ItemsList.get(1).getText() + " " + ItemsList.get(1).getContentDescription() + " elementos seguinte: " + ItemsList.get(2).getText() + " " + ItemsList.get(2).getContentDescription());
                            //ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                            ItemsList.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                            //ItemsList.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            //ItemsList.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                            cameFromKeyboard = false;
                        }else
                        {
                            //unfocus current element and focus the previous menu item
                            menu.unfocusElement(menu.getCurrentFocused());
                            if(menu.getCurrentFocused() != 0) {
                                menu.focusElement(menu.getCurrentFocused() - 1);
                                showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" botão.");
                                Log.d("MENU VIRTUAL", "Menu "+menu.getMenuName()+" focused.");
                                Log.d("MENU VIRTUAL", "Botao "+menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" focado");
                            }else {
                                menu.focusElement(menu.getMenuButtons().size()-1);
                                showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" botão.");
                                Log.d("MENU VIRTUAL", "Menu "+menu.getMenuName()+" focused.");
                                Log.d("MENU VIRTUAL", "Botao "+menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" focado");
                            }
                        }
                        myo.notifyUserAction();
                        break;
                    case WAVE_OUT:
                        showToast("Navegar para a frente");
                        if(!isVirtualMenu) {
                            Log.i("WAVE OUT", "elemento anterior: " + ItemsList.get(0).getText() + " " + ItemsList.get(0).getContentDescription() + " elemento actual: " + ItemsList.get(1).getText() + " " + ItemsList.get(1).getContentDescription() + " elementos seguinte: " + ItemsList.get(2).getText() + " " + ItemsList.get(2).getContentDescription());
                            LastNode = ItemsList.get(1);
                            if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") == 0) {
                                WindowList = getWindows();
                                if (WindowList.size() > 3)
                                    currentWin = 3;
                            }
                            // ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                            ItemsList.get(2).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                            //ItemsList.get(2).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            //ItemsList.get(2).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                            cameFromKeyboard = false;
                        }else
                        {
                            //unfocus current element and focus the next menu item
                            menu.unfocusElement(menu.getCurrentFocused());
                            if(menu.getCurrentFocused() +1 != menu.getMenuButtons().size()) {
                                menu.focusElement(menu.getCurrentFocused() + 1);
                                showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" botão.");
                                Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focused.");
                                Log.d("MENU VIRTUAL", "Botao " + menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " focado");
                            }else {
                                menu.focusElement(0);
                                showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" botão.");
                                Log.d("MENU VIRTUAL", "Menu "+menu.getMenuName()+" focused.");
                                Log.d("MENU VIRTUAL", "Botao "+menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" focado");
                            }
                        }
                        myo.notifyUserAction();
                        break;
                    case DOUBLE_TAP:
                        showToast("Clicar");
                        if(!isVirtualMenu) {
                            LastNode = ItemsList.get(1);
                            lastUI.clear();
                            lastUI.addAll(ItemsListRoot);
                            Log.i("LOG_CLICK", "size lastui: " + lastUI);
                            ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                            ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                            //ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                        }else{
                            performGlobalActions(menu.getMenuButtons().get(menu.getCurrentFocused()).onClick());

                        }
                        myo.notifyUserAction();



                        break;
                    case FIST:
                        showToast("Mudar foco");
                        cameFromKeyboard = false;
                        if(WindowList.size() > 3) {
                            performGlobalActions(BACK_BUTTON);
                            ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                            WindowList = getWindows();
                            currentWin =-1;

                        }

                        if( currentWin >= WindowList.size()-1 && !isVirtualMenu){
                            isVirtualMenu = true;
                        }
                        else if(currentWin >= WindowList.size()-1 && isVirtualMenu) {
                            isVirtualMenu = false;
                            currentWin = 0;
                        }else
                            currentWin++;
                        if(!isVirtualMenu) {
                            LastNode = ItemsList.get(1);
                            ItemsListRoot.clear();
                            ItemsListRootNotVisible.clear();
                            ItemsListRootNoFilter.clear();
                            ItemsList.clear();
                            logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                            Log.i("LOG_TAG", "ENDLOG");
                            if (ItemsListRoot.size() == 0) {
                                while (ItemsListRoot.size() == 0) {
                                    Log.d("FIST", "FIST window size: " + WindowList.size() + "current window: " + currentWin);
                                    if (currentWin >= WindowList.size() - 1)
                                        currentWin = 0;
                                    else
                                        currentWin++;
                                    logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                                }


                                Log.i("LOG_TAG", "ENDLOG");
                            }
                            ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            WindowList = getWindows();
                            myo.notifyUserAction();
                        }else{
                            //get first element of the virtual menu and focus it
                            menu.focusElement(0);
                            Log.d("MENU VIRTUAL", "Menu "+menu.getMenuName()+" focado.");
                            Log.d("MENU VIRTUAL", menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel()+" botão.");
                        }
                        break;
                    case FINGERS_SPREAD:
                        //showToast("Navegar para trás"); //IMPLEMEMNTAR PREVIOUS GESTURE PARA NAO REPETIR A FRASE MULTIPLAS VEZES
                        if(!isVirtualMenu) {
                            normalizationPitch = pitch_w;
                            boolean isScrollable = false;
                            tempInfo = ItemsList.get(1).getParent();


                            while (!isScrollable) {
                                if (tempInfo != null && tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") != 0 && tempInfo.getClassName().toString().compareTo("android.widget.ListView") != 0) {
                                    Log.i("Description", " " + tempInfo.getClassName() + " " + tempInfo.toString() + "" + tempInfo);
                                    tempInfo = tempInfo.getParent();
                                } else if (tempInfo != null && (tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") == 0 || tempInfo.getClassName().toString().compareTo("android.widget.ListView") == 0))
                                    isScrollable = true;
                                else if (tempInfo != null && tempInfo.equals(tempInfo.getWindow().getRoot()))
                                    break;
                                else if (tempInfo == null)
                                    break;
                            }


                            Log.i("teste", ItemsListRootNotVisible.size() + " size");
                            //ItemsListRootNotVisible.get(0).performAction(AccessibilityNodeInfo.FOCUS_INPUT);


                        }
                        myo.notifyUserAction();
                        break;
                    case REST: break;
                    case UNKNOWN: break;
                    default:
                        break;
                }
            }catch(IndexOutOfBoundsException e) {
                Log.i("AccessibiltiServiceApp", "ERROR: No tree");
            }
        }
    };*/

    public void onConnect()
    {
        WindowList = getWindows();
        currentWin = 0;
        Log.i("INICIO", "numero de janelas: "+WindowList.size());
        if(WindowList.size() != 0) {
            logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
            //Log.i("LOG_TAG", "ENDLOG");
            if(ItemsListRoot.size() != 0) {
                ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }else {
                while(ItemsListRoot.size() == 0)
                {
                    Log.d("ON CONNECT", "ON CONNECT window size: "+WindowList.size()+ "current window: "+currentWin);
                    if(currentWin < WindowList.size() -1)
                        currentWin++;
                    else
                        currentWin = 0;
                    logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                }
                ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            LastNode = ItemsListRoot.get(0);
        }
        isConnected = true;
    }

    public void onDisconnect() {
        //oast(getString(R.string.disconnected));
        isConnected = false;
    }

    public void onPose(String myoGesture, String smartwatchGesture) {
        // Show the name of the pose in a toast.

        if(myoGesture.compareTo("Fist") == 0 && counterFists == 0) {
            myoGesture = "NoGesture";
            counterFists++;
        } else if(myoGesture.compareTo("Fist") == 0 && counterFists > 0)
        {
            myoGesture = "Hold";
            counterFists = 0;
        } else if (myoGesture.compareTo("Fist") != 0 && counterFists > 0) {
            counterFists = 0;
            myoGesture = "Fist";
        }

        if( gestureActivated) {
            Log.i("AccessibiltiServiceApp", " POSE: " + myoGesture + " smartGesture" + smartwatchGesture + "ItemList: " + ItemsList.size() + " ItemsListRoot: " + ItemsListRoot.size() + " WindowsList: " + WindowList.size() + " currentWindow: " + currentWin);

            try {

                switch (myoGesture) {
                    case "Wave_In":
                        if (smartwatchGesture.compareTo("Palm_left") == 0) {
                            showToast("Navegar para trás");
                            if (!isVirtualMenu) {
                                LastNode = ItemsList.get(1);
                                if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") == 0) {
                                    WindowList = getWindows();
                                    if (WindowList.size() > 3)
                                        currentWin = 3;
                                }
                                Log.i("WAVE IN", "elemento anterior: " + ItemsList.get(0).getText() + " " + ItemsList.get(0).getContentDescription() + " elemento actual: " + ItemsList.get(1).getText() + " " + ItemsList.get(1).getContentDescription() + " elementos seguinte: " + ItemsList.get(2).getText() + " " + ItemsList.get(2).getContentDescription());
                                //ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                                ItemsList.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                                //ItemsList.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                                //ItemsList.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                                cameFromKeyboard = false;
                            } else {
                                //unfocus current element and focus the previous menu item
                                menu.unfocusElement(menu.getCurrentFocused());
                                if (menu.getCurrentFocused() != 0) {
                                    menu.focusElement(menu.getCurrentFocused() - 1);
                                    showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " botão.");
                                    Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focused.");
                                    Log.d("MENU VIRTUAL", "Botao " + menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " focado");
                                } else {
                                    menu.focusElement(menu.getMenuButtons().size() - 1);
                                    showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " botão.");
                                    Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focused.");
                                    Log.d("MENU VIRTUAL", "Botao " + menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " focado");
                                }
                            }
                        } else if (smartwatchGesture.compareTo("Palm_down") == 0) {
                            //TODO - Incompleto, olhar par ao codigo do onOrientation

                            if (!isVirtualMenu) {
                                normalizationPitch = pitch_w;
                                boolean isScrollable = false;
                                Log.d("TESTING SCROLL WAVE_IN", ItemsList.get(1).toString());
                                tempInfo = ItemsList.get(1).getParent();

                                while (!isScrollable) {
                                    if (tempInfo != null && tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") != 0 && tempInfo.getClassName().toString().compareTo("android.widget.ListView") != 0 && tempInfo.getClassName().toString().compareTo("android.support.v7.widget.RecyclerView") != 0) {
                                        Log.i("TESTING SCROLL WAVE_IN", "1º IF" + tempInfo.getClassName() + " " + tempInfo.toString() + "" + tempInfo);
                                        tempInfo = tempInfo.getParent();
                                    } else if (tempInfo != null && (tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") == 0 || tempInfo.getClassName().toString().compareTo("android.widget.ListView") == 0 || tempInfo.getClassName().toString().compareTo("android.support.v7.widget.RecyclerView") == 0)) {
                                        Log.i("TESTING SCROLL WAVE_IN", "2º IF" + tempInfo.getClassName() + " " + tempInfo.toString() + "" + tempInfo);
                                        isScrollable = true;
                                    }else if (tempInfo != null && tempInfo.equals(tempInfo.getWindow().getRoot()))
                                        break;
                                    else if (tempInfo == null)
                                        break;
                                }


                                Log.i("TESTING SCROLL WAVE_IN", ItemsListRootNotVisible.size() + " size");
                                //ItemsListRootNotVisible.get(0).performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                                if(tempInfo != null)
                                    tempInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                else
                                    Log.i("SCROLL BACKWORD","CANT SCROLL FORWARD" );

                            }

                        }
                        //myo.notifyUserAction();
                        break;
                    case "Wave_Out":
                        Log.i("WAVE OUT", "WAVE_OUT");
                        if (smartwatchGesture.compareTo("Palm_left") == 0) {
                            showToast("Navegar para a frente");
                            Log.i("WAVE OUT", "WAVE_OUT+PALM_LEFT");
                            if (!isVirtualMenu) {
                                Log.i("WAVE OUT", "elemento anterior: " + ItemsList.get(0).getText() + " " + ItemsList.get(0).getContentDescription() + " elemento actual: " + ItemsList.get(1).getText() + " " + ItemsList.get(1).getContentDescription() + " elementos seguinte: " + ItemsList.get(2).getText() + " " + ItemsList.get(2).getContentDescription());
                                LastNode = ItemsList.get(1);
                                if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") == 0) {
                                    WindowList = getWindows();
                                    if (WindowList.size() > 3)
                                        currentWin = 3;
                                }
                                // ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                                ItemsList.get(2).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                                //ItemsList.get(2).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                                //ItemsList.get(2).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                                cameFromKeyboard = false;
                            } else {
                                //unfocus current element and focus the next menu item
                                menu.unfocusElement(menu.getCurrentFocused());
                                if (menu.getCurrentFocused() + 1 != menu.getMenuButtons().size()) {
                                    menu.focusElement(menu.getCurrentFocused() + 1);
                                   showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " botão.");
                                    Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focused.");
                                    Log.d("MENU VIRTUAL", "Botao " + menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " focado");
                                } else {
                                    menu.focusElement(0);
                                    showToast(menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " botão.");
                                    Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focused.");
                                    Log.d("MENU VIRTUAL", "Botao " + menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " focado");
                                }
                            }
                        } else if (smartwatchGesture.compareTo("Palm_down") == 0) {
                            //TODO - Incompleto, olhar par ao codigo do onOrientation
                            if (!isVirtualMenu) {
                                normalizationPitch = pitch_w;
                                boolean isScrollable = false;
                                tempInfo = ItemsList.get(1).getParent();


                                while (!isScrollable) {
                                    if (tempInfo != null && tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") != 0 && tempInfo.getClassName().toString().compareTo("android.widget.ListView") != 0 && tempInfo.getClassName().toString().compareTo("android.support.v7.widget.RecyclerView") != 0) {
                                        Log.i("Description", " " + tempInfo.getClassName() + " " + tempInfo.toString() + "" + tempInfo);
                                        tempInfo = tempInfo.getParent();
                                    } else if (tempInfo != null && (tempInfo.getClassName().toString().compareTo("android.widget.ScrollView") == 0 || tempInfo.getClassName().toString().compareTo("android.widget.ListView") == 0 || tempInfo.getClassName().toString().compareTo("android.support.v7.widget.RecyclerView") == 0))
                                        isScrollable = true;
                                    else if (tempInfo != null && tempInfo.equals(tempInfo.getWindow().getRoot()))
                                        break;
                                    else if (tempInfo == null)
                                        break;
                                }


                                Log.i("teste", ItemsListRootNotVisible.size() + " size");
                                //ItemsListRootNotVisible.get(0).performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                                if(tempInfo != null)
                                    tempInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                                else
                                    Log.i("SCROLL BACKWORD","CANT SCROLL BACKWARDS" );

                            }


                        }
                        //myo.notifyUserAction();
                        break;
                    case "Fist":
                            showToast("Clicar");
                            if (!isVirtualMenu) {
                                LastNode = ItemsList.get(1);
                                lastUI.clear();
                                lastUI.addAll(ItemsListRoot);
                                Log.i("LOG_CLICK", "size lastui: " + lastUI);
                                ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                                ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                                //ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                            } else {
                                performGlobalActions(menu.getMenuButtons().get(menu.getCurrentFocused()).onClick());

                            }
                            //myo.notifyUserAction();


                        break;

                    case "Hold":

                        showToast("Mudar foco");
                        cameFromKeyboard = false;
                        if (WindowList.size() > 3) {
                            performGlobalActions(BACK_BUTTON);
                            ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                            WindowList = getWindows();
                            currentWin = -1;

                        }

                        if (currentWin >= WindowList.size() - 1 && !isVirtualMenu) {
                            isVirtualMenu = true;
                        } else if (currentWin >= WindowList.size() - 1 && isVirtualMenu) {
                            isVirtualMenu = false;
                            currentWin = 0;
                        } else
                            currentWin++;
                        if (!isVirtualMenu) {
                            LastNode = ItemsList.get(1);
                            ItemsListRoot.clear();
                            ItemsListRootNotVisible.clear();
                            ItemsListRootNoFilter.clear();
                            ItemsList.clear();
                            logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                            Log.i("LOG_TAG", "ENDLOG");
                            if (ItemsListRoot.size() == 0) {
                                while (ItemsListRoot.size() == 0) {
                                    Log.d("FIST", "FIST window size: " + WindowList.size() + "current window: " + currentWin);
                                    if (currentWin >= WindowList.size() - 1)
                                        currentWin = 0;
                                    else
                                        currentWin++;
                                    logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                                }


                                Log.i("LOG_TAG", "ENDLOG");
                            }
                            ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                            //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            WindowList = getWindows();
                            //myo.notifyUserAction();
                        } else {
                            //get first element of the virtual menu and focus it
                            menu.focusElement(0);
                            Log.d("MENU VIRTUAL", "Menu " + menu.getMenuName() + " focado.");
                            Log.d("MENU VIRTUAL", menu.getMenuButtons().get(menu.getCurrentFocused()).getLabel() + " botão.");
                        }
                        break;
                    default:
                        break;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.i("AccessibiltiServiceApp", "ERROR: No tree");
            }
        }
        }




    @Override
    public void onServiceConnected()
    {
        Log.v(TAG, "***** onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;

        info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        //info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);


    }

    private void performGlobalActions(int type)
    {
        Log.i("SCROLL", "TYPE "+type+" currentWin: "+currentWin);
        if(type == DROP_NOTIFICATIONS)
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
        else if(type == SCROLL_RIGHT)
            this.performGlobalAction(AccessibilityService.GESTURE_SWIPE_RIGHT);
        else if(type == SCROLL_LEFT)
            this.performGlobalAction(AccessibilityService.GESTURE_SWIPE_LEFT);
        else if(type == BACK_BUTTON)
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        else if(type == HOME_BUTTON)
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        else if(type == BACKGROUND_BUTTON)
            this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);

    }


    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(isConnected) {
            //Log.i("EVENT", "Event: " + event.getEventType() + " source: " + event.getSource());

            if (event.getSource() != null && event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED && !cameFromKeyboard) { //32768 TYPE_VIEW_ACCESSIBILITY_FOCUSED
               // Log.i("EVENT", "element focused: " + event.getSource().getContentDescription()+" element text: "+event.getSource().getText());
                ItemsListRoot.clear();
                ItemsListRootNoFilter.clear();
                ItemsListRootNotVisible.clear();
                ItemsList.clear();
                WindowList = getWindows();
                if(currentWin >= WindowList.size())
                    currentWin = WindowList.size()-1;
                logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                //Log.i("LOG_TAG", "ENDLOG");
                for (int i = 0; i < ItemsListRoot.size(); i++) {
                    if (ItemsListRoot.get(i).equals(event.getSource())) {
                        //add previous element
                        if (i == 0) {
                            ItemsList.add(ItemsListRoot.get(ItemsListRoot.size() - 1));

                        } else
                            ItemsList.add(ItemsListRoot.get(i - 1));
                        //add current element
                        ItemsList.add(ItemsListRoot.get(i));

                        //add next element
                        if (i == ItemsListRoot.size() - 1) {
                            ItemsList.add(ItemsListRoot.get(0));

                        } else
                            ItemsList.add(ItemsListRoot.get(i + 1));

                        break;
                    }
                }
                if (ItemsList.size() == 0 && ItemsListRoot.size() > 0) {
                    if (ItemsListRoot.size() >= 2) {
                        ItemsList.add(ItemsListRoot.get(ItemsListRoot.size() - 1));
                        ItemsList.add(ItemsListRoot.get(0));
                        ItemsList.add((ItemsListRoot.get(1)));
                    } else {
                        ItemsList.add(ItemsListRoot.get(0));
                        ItemsList.add(ItemsListRoot.get(0));
                        ItemsList.add(ItemsListRoot.get(0));
                    }
                }else if(ItemsList.size() == 0 && ItemsListRoot.size() == 0)
                {
                    ItemsListRoot.clear();
                    ItemsListRootNoFilter.clear();
                    ItemsListRootNotVisible.clear();
                    ItemsList.clear();
                    WindowList = getWindows();
                    logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                    int counter = 0;
                    if(ItemsListRoot.size() == 0) {
                        while(ItemsListRoot.size() == 0 && counter < WindowList.size())
                        {
                  //          Log.d("TYPE_VIEW_ACCESSIBILITY_FOCUSED", "TYPE_VIEW_ACCESSIBILITY_FOCUSED window size: "+WindowList.size()+ "current window: "+currentWin);
                            if( currentWin >= WindowList.size()-1)
                                currentWin = 0;
                            else
                                currentWin++;
                            logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                            counter++;
                        }


                    //    Log.i("LOG_TAG", "ENDLOG");
                    }
                    try {
                        ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    }catch (Exception e) {
                      //  Log.d("TYPE_VIEW_ACCESSIBILITY_FOCUSED", "cannot find any elemento to focus in window " + currentWin);
                    }
                }
                if(ItemsList.size() != 0)
                    Log.i("EVENT FOCUSED", "elemento anterior: "+ItemsList.get(0).getText()+" "+ItemsList.get(0).getContentDescription()+" elemento actual: "+ItemsList.get(1)+" "+ItemsList.get(1).getContentDescription()+" elementos seguinte: "+ItemsList.get(2)+" "+ItemsList.get(2).getContentDescription());
                else
                    Log.i("EVENT FOCUSED", "NAO CONSEGIU IMPRIMIR");

            } else if (/*event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||*/ LastNode != null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && (LastNode.getClassName().toString().compareTo("android.widget.EditText") !=0 || (LastNode.getClassName().toString().compareTo("android.widget.EditText") ==0 && event.getSource() != null && event.getPackageName().toString().compareTo("com.google.android.inputmethod.latin") != 0)) ) //TYPE_WINDOW_STATE_CHANGED 32
            {

                if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") != 0) {
                    ItemsListRoot.clear();
                    ItemsListRootNotVisible.clear();
                    ItemsListRootNoFilter.clear();
                    WindowList = getWindows();

                    if (currentWin >= WindowList.size())
                        currentWin = WindowList.size()-1;

                    logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                    //Log.i("LOG_TAG", "ENDLOG");

                    if(ItemsListRoot.size() == 0) {
                        while(ItemsListRoot.size() == 0)
                        {
                         //   Log.d("TYPE_WINDOW_CONTENT_CHANGED", "TYPE_WINDOW_CONTENT_CHANGED window size: "+WindowList.size()+ "current window: "+currentWin);
                            if( currentWin >= WindowList.size()-1)
                                currentWin = 0;
                            else
                                currentWin++;
                            logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                        }


                        //Log.i("LOG_TAG", "ENDLOG");
                    }
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                }


                //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                //ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            /*if(ItemsList.isEmpty() && ItemsListRoot.isEmpty())
            {
                Log.i("AccessibiltiServiceApp","ItemsList and Root Null");
                logNodeHeirarchy(getWindows().get(currentWin).getRoot(),0);
                if(!ItemsListRoot.isEmpty())
                {
                    Log.i("AccessibiltiServiceApp","ItemsListRoot Not Null");
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                }
            }else
            {
                ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            }*/

            } else if (event.getEventType() ==  AccessibilityEvent.TYPE_VIEW_CLICKED/*ID =1  nao me lembro o que era esta condiçao*/ && event.getSource() != null && event.getPackageName().toString().compareTo("com.google.android.inputmethod.latin") != 0) {

                //Log.i("CLICKS_antes", "elemento anterior: "+ItemsList.get(0).getText()+" "+ItemsList.get(0).getContentDescription()+" elemento actual: "+ItemsList.get(1).getText()+" "+ItemsList.get(1).getContentDescription()+" elementos seguinte: "+ItemsList.get(2).getText()+" "+ItemsList.get(2).getContentDescription());
                if (ItemsList.get(1).getClassName().toString().compareTo("android.widget.EditText") == 0) {
                    cameFromKeyboard = true;
                    WindowList = getWindows();
                    if (WindowList.size() > 2) {
                        //ItemsList.get(1).performAction(AccessibilityNodeInfo.FOCUS_INPUT);
                        ItemsListRoot.clear();
                        ItemsListRootNotVisible.clear();
                        ItemsListRootNoFilter.clear();


                        logNodeHeirarchy(WindowList.get(2).getRoot(), 0);
                  //      Log.i("LOG_TAG", "ENDLOG");
                        int i = 0;
                        while (i <= ItemsListRootNoFilter.size() - 1) {
                            if (ItemsListRootNoFilter.get(i).getContentDescription() != null && ItemsListRootNoFilter.get(i).getContentDescription().toString().compareTo("Entrada de texto por voz") == 0) {

                                ItemsListRootNoFilter.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                currentWin = 3;
                                break;
                            } else if (ItemsListRootNoFilter.get(i).getText() != null && ItemsListRootNoFilter.get(i).getText().toString().compareTo("A ouvir…") == 0){
                                performGlobalActions(BACK_BUTTON);
                                ItemsList.get(1).performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
                                WindowList = getWindows();
                                currentWin = 2;

                            }
                            i++;
                        }

                        ItemsListRoot.clear();
                        ItemsListRootNotVisible.clear();
                        ItemsListRootNoFilter.clear();
                        logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                    //    Log.i("LOG_TAG", "ENDLOG");
                      //  Log.i("CLICKS_depois", "elemento anterior: "+ItemsList.get(0).getText()+" "+ItemsList.get(0).getContentDescription()+" elemento actual: "+ItemsList.get(1).getText()+" "+ItemsList.get(1).getContentDescription()+" elementos seguinte: "+ItemsList.get(2).getText()+" "+ItemsList.get(2).getContentDescription());
                    }

                }
            }else if(event.getSource() != null && WindowList.size() > 0 &&  event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.getSource().getWindow().getId() == WindowList.get(currentWin).getId())
            {
                //Log.i("LOG_EVENT 2048", "ENTROU NO 2048");

                ItemsListRoot.clear();
                ItemsListRootNotVisible.clear();
                ItemsListRootNoFilter.clear();
                logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                boolean hasFound = false;
                //Log.i("LOG_EVENT 2048", "last: "+lastUI.size()+" current: "+ ItemsListRoot.size());
                if(ItemsListRoot.size() != lastUI.size()) {
                    for (int i = ItemsListRoot.size() - 1; i >= 0; i--) {
                        for (int p = lastUI.size() - 1; p >= 0; p--) {
                            if (ItemsListRoot.get(i).equals(lastUI.get(p))) {
                                ItemsListRoot.get(i).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                                hasFound = true;
                                break;
                            }

                        }
                        if (hasFound)
                            break;
                    }
                    if (!hasFound){
                        if(ItemsListRoot.size() == 0) {
                            while(ItemsListRoot.size() == 0)
                            {
                  //              Log.d("TYPE_WINDOW_CONTENT_CHANGED", "TYPE_WINDOW_CONTENT_CHANGED window size: "+WindowList.size()+ "current window: "+currentWin);
                                if( currentWin >= WindowList.size()-1)
                                    currentWin = 0;
                                else
                                    currentWin++;
                                logNodeHeirarchy(WindowList.get(currentWin).getRoot(), 0);
                            }


                    //        Log.i("LOG_TAG", "ENDLOG");
                        }
                        ItemsListRoot.get(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    }

                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void logNodeHeirarchy(AccessibilityNodeInfo nodeInfo, int depth) {

        if (nodeInfo == null) return;
        String logStringNotVisible = "";
        String logString = "";

        for (int i = 0; i < depth; ++i) {
            logString += " ";
            logStringNotVisible += " ";
        }

        logString += "Text: " + nodeInfo.getText() + " " + " Content-Description: " + nodeInfo.getContentDescription() + "class: "+nodeInfo.getClassName()+" visisble? : "+nodeInfo.isVisibleToUser()+" clickable? : "+nodeInfo.isClickable();

        //Log.i("LOG_TAG", logString);
        ItemsListRootNoFilter.add(nodeInfo);
        if (nodeInfo.isClickable())
            ItemsListRootNotVisible.add(nodeInfo);
        if ((nodeInfo.isVisibleToUser() && (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null)) || nodeInfo.isClickable() ) {
           // Log.i("NODE_HEIRARCHY", "Adicionou elemento " + nodeInfo.getClassName());
            ItemsListRoot.add(nodeInfo);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logNodeHeirarchy(nodeInfo.getChild(i), depth + 1);
        }


    }

    private void showToast(final String text) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(text);
                }
                mToast.show();

            }
        });


        //Log.w(TAG, text);

    }


    private AccessibilityNodeInfo getListItemNodeInfo(AccessibilityNodeInfo source) {
        AccessibilityNodeInfo current = source;
        while (true) {
            AccessibilityNodeInfo parent = current.getParent();
            if (parent == null) {
                return null;
            }
            if (TASK_LIST_VIEW_CLASS_NAME.equals(parent.getClassName())) {
                return current;
            }
            // NOTE: Recycle the infos.
            AccessibilityNodeInfo oldCurrent = current;
            current = parent;
            oldCurrent.recycle();
        }
    }

     /*@Override
     public IBinder onBind(Intent intent) {
         return null;
     }*/


    @Override
    public void onCreate() {
        super.onCreate();
        //setContentView(R.layout.activity_main);

        //talkbutton = findViewById(R.id.talkButton);
        //textview = findViewById(R.id.textView);

        createSocketClientMobile();

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });


        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        Log.d("Mobile", "Accessibility Service activo");

        /* CODIGO PARA MYO STANDARD
        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            showToast("Couldn't initialize Hub");
            stopSelf();
            return;
        }

        // Disable standard Myo locking policy. All poses will be delivered.
        hub.setLockingPolicy(Hub.LockingPolicy.NONE);

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        // Finally, scan for Myo devices and connect to the first one found that is very near.
        hub.attachToAdjacentMyo();
        */

        //Codigo para inicializar o comportamento do serviço de acessibilidade
        onConnect();

        try {
            ei = new EventInput();
        } catch (Exception e) {
            e.printStackTrace();
        }

        menu = new com.jessicathornsby.datalayer.Menu("Menu de navegação");
        menu.addButton(new BackButton("Voltar Atrás"));
        menu.addButton(new HomeButton("Inicio"));
        menu.addButton(new BackgroundButton("Aplicações ligadas"));

        context = this;
    }


    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            textview.append("\n" + newinfo);
        }
    }

    public boolean makeConnection()
    {
        boolean response = false;
        try {
            kkSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream(), "UTF-8"));

            response =  true;
        } catch (UnknownHostException e) {
            System.err.println("Make Connection: Don't know about host " + hostName);


        } catch (IOException e) {
            System.err.println("Make Connection: Couldn't get I/O for the connection to " +
                    hostName);

        }
        return response;
    }

    public void createSocketClientMobile() {
        new Thread(new Runnable() {
            public void run() {

                boolean res = makeConnection();
                if (res) {

                    try {
                        while ((fromServer = in.readLine()) != null) {


                            gestureBuffer.add(new Gesture(fromServer, getGestureFromWatch()));
                            boolean isActivationGesture = true;
                            //ACTIVATION CODE DISABLED FOR DEBUGGING PROCESS
                            /*if (gestureBuffer.size() >= 4) {
                                for(Gesture g : gestureBuffer)
                                    Log.d("DesktopMobile", "BUFFER: Gesture "+g.getGesture()+" Orientation: "+g.getOrientation()+ " Timestamp: "+g.getTimeStamp());
                                Log.d("DesktopMobile", "TimeStamp: "+ (gestureBuffer.get(gestureBuffer.size() - 1).getTimeStamp() - gestureBuffer.get(0).getTimeStamp())+" buffer size: "+gestureBuffer.size());

                                if (gestureBuffer.get(gestureBuffer.size() - 1).getTimeStamp() - gestureBuffer.get(0).getTimeStamp() < 5000) {
                                    Log.d("DesktopMobile", "INSIDE WINDOW TIME OF 4 SECONDS");
                                    for (int i = 0; i < gestureBuffer.size() && isActivationGesture; i++) {
                                        if((i == 0 || i == 2)) {
                                            if (!(gestureBuffer.get(i).getOrientation().compareTo("Palm_down") == 0 && gestureBuffer.get(i).getGesture().compareTo("Fist") == 0))
                                            {
                                                isActivationGesture = false;
                                            }
                                        }else{
                                            if (!(gestureBuffer.get(i).getOrientation().compareTo("Palm_left") == 0 && gestureBuffer.get(i).getGesture().compareTo("Fist") == 0))
                                            {
                                                isActivationGesture = false;
                                            }
                                        }

                                    }


                                //gestureBuffer.remove(0);


                                    if (isActivationGesture) {
                                        if (gestureActivated) {
                                            gestureActivated = false;
                                            Log.d("Mobile", "GESTURE RECOGNITION DEACTIVATED....");
                                        } else {
                                            gestureActivated = true;
                                            Log.d("Mobile", "GESTURE RECOGNITION ACTIVATED....");
                                        }
                                    }
                                }
                                gestureBuffer.remove(0);
                            }*/

                            gestureActivated = true;
                            if(gestureActivated)
                                onPose(fromServer, getGestureFromWatch());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public String getGestureFromWatch() {
        return gestureFromWatch;
    }

    public void setGestureFromWatch(String gestureFromWatch) {
        this.gestureFromWatch = gestureFromWatch;
        //Log.d("Mobile", "GESTURE SET:"+ gestureFromWatch);
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = "I just received a message from the wearable " + receivedMessageNumber++;;
            //textview.setText(message+" "+intent.getStringExtra("message"));
            //Log.d("Mobile", message+" "+intent.getStringExtra("message"));
            setGestureFromWatch(intent.getStringExtra("message"));


        }
    }


    /*public void talkClick(View v) {
        String message = "Sending message.... ";
        textview.setText(message);
        new NewThread("/my_path", message).start();

    }*/


    public void sendmessage(String messageText) {
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);

    }




    class NewThread extends Thread {
        String path;
        String message;

        NewThread(String p, String m) {
            path = p;
            message = m;
        }


        public void run() {

            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {

                        Integer result = Tasks.await(sendMessageTask);
                        sendmessage("I just sent the wearable a message " + sentMessageNumber++);

                    } catch (ExecutionException exception) {

                        //TO DO: Handle the exception//


                    } catch (InterruptedException exception) {

                    }

                }

            } catch (ExecutionException exception) {

                //TO DO: Handle the exception//

            } catch (InterruptedException exception) {

                //TO DO: Handle the exception//
            }

        }
    }
}

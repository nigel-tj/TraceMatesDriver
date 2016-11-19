package za.co.tracemates;

import com.codename1.components.InteractionDialog;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.ui.*;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose
 * of building native mobile applications using Java.
 */
public class TraceMatesDriver {

    private Form current;
    private Resources theme;

    private Form home;
    ConnectionRequest request = null;
    Location  position = null;

    private String apiURL = "http://tracemates.foodmates.co.za/services/rest/v1/tracemates.php";
    private Boolean tracking = false;
    private String trackingId = "";
    private  HashMap<String, String> driverDetails = null;

    public void init(Object context) {
        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature, uncomment if you have a pro subscription
        // Log.bindCrashProtection(true);

        //Create Form1 and Form2 and set a Back Command to navigate back to the home Form
        //Form listParcels = listParcels();
        //setBackCommand(listParcels);
        //Form form2 = new Form("Form2");
        //setBackCommand(form2);

        //Add navigation commands to the home Form
        //NavigationCommand homeCommand = new NavigationCommand("Home");
        //homeCommand.setNextForm(home);
        //home.getToolbar().addCommandToSideMenu(homeCommand);

        //NavigationCommand cmd1 = new NavigationCommand("Form1");
        //cmd1.setNextForm(form1);
        //home.getToolbar().addCommandToSideMenu(cmd1);

//        NavigationCommand cmd2 = new NavigationCommand("Form2");
        //      cmd2.setNextForm(form2);
        //    home.getToolbar().addCommandToSideMenu(cmd2);

    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        login();
    }

    protected void setBackCommand(Form f) {
        Command back = new Command("") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                home.showBack();
            }
        };
        Image img = FontImage.createMaterial(FontImage.MATERIAL_ARROW_BACK, UIManager.getInstance().getComponentStyle("TitleCommand"));
        back.setIcon(img);
        f.getToolbar().addCommandToLeftBar(back);
        f.getToolbar().setTitleCentered(true);
        f.setBackCommand(back);
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {
    }

    public void trackParcel(Map parcel) {
        //create and build the home Form
        trackingId = (String)parcel.get("trackingNumber");
        home = new Form("TraceMates");
        home.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Container parcelInfo = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        Label parcelName = new Label((String) parcel.get("name"));
        Label parcelDetails = new Label((String) parcel.get("parcelDetails"));
        Label creationTime = new Label((String) parcel.get("creationTime"));
        Label trackingNumber = new Label((String) parcel.get("trackingNumber"));
        Label address = new Label((String) parcel.get("address"));
        Label city = new Label((String) parcel.get("city"));
        Label province = new Label((String) parcel.get("province"));
        Label postalCode = new Label((String) parcel.get("postalCode"));
        Label deliveredFlag = new Label();
        Label customerDetails = new Label("Test customer");
        parcelInfo.addComponent(customerDetails);
        parcelInfo.addComponent(creationTime);
        parcelInfo.addComponent(parcelName);
        parcelInfo.addComponent(parcelDetails);
        parcelInfo.addComponent(trackingNumber);
        parcelInfo.addComponent(address);
        parcelInfo.addComponent(city);
        parcelInfo.addComponent(province);
        parcelInfo.addComponent(postalCode);
        if(parcel.get("deliveredFlag").equals("t")){
            deliveredFlag.setText("delivered");
        }else{
            deliveredFlag.setText("pending");
        }
        parcelInfo.addComponent(deliveredFlag);
        home.addComponent(parcelInfo);
        Button start = new Button("Start Tracking");

        InteractionDialog dialog = new InteractionDialog("Start Tracking");
        dialog.setLayout(new BorderLayout());
        dialog.add(BorderLayout.CENTER, new Label("Starting tracking for parcel xyz"));
        Button ok = new Button("Ok");
        ok.addActionListener((ee) -> dialog.dispose());
        Button close = new Button("Cancel");
        close.addActionListener((ee) -> dialog.dispose());
        dialog.addComponent(BorderLayout.EAST, close);
        dialog.addComponent(BorderLayout.WEST, ok);
        Dimension pre = dialog.getContentPane().getPreferredSize();

        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

                Dialog.show("Start Tracking", "Tracking parcel xyz", "Ok", "Cancel");
                //dialog.show(0, 0, Display.getInstance().getDisplayWidth() - (pre.getWidth() + pre.getWidth() / 6), 0);
                //trackingId = trackingIdTextFld.getText();
                tracking = true;
                tracker();

            }
        });

        home.addComponent(start);
        Button stop = new Button("Stop Tracking");
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Dialog.show("Stop Tracking", "Parcel xyz", "Ok", "Cancel");
                tracking = false;
                tracker();
            }
        });
        home.addComponent(stop);
        current = home;
        start();
    }

    private void tracker(){
        while (tracking) {
            try {
                request = new ConnectionRequest();
                position = LocationManager.getLocationManager().getCurrentLocation();
                String longitude = Double.toString(position.getLongitude());
                String latitude = Double.toString(position.getLatitude());

                request.setUrl(apiURL);
                request.setPost(false);
                request.setHttpMethod("POST");
                request.setContentType("application/x-www-form-urlencoded");
                request.addRequestHeader("Connection", "Keep-Alive");
                request.addArgument("function", "add_tracker_location");
                request.addArgument("tracker_id", trackingId);
                request.addArgument("longitude", longitude);
                request.addArgument("latitude", latitude);

                Log.p(request.getUrl());

                NetworkManager.getInstance().addToQueueAndWait(request);
                JSONParser p = new JSONParser();
                Map<String, Object> result = p.parseJSON(new InputStreamReader(new ByteArrayInputStream(request.getResponseData())));
                Thread.sleep(15000);
                Log.p("tracking code= " + trackingId);
                Log.p("longitude= " + longitude);
                Log.p("latitude= " + latitude);
                Log.p(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void login(){
        home = new Form("");
        home.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        TextField email = new TextField();
        email.setHint("email");
        TextField password = new TextField();
        password.setHint("password");
        Button submit = new Button("Login");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //Dialog.show("Dialog Title", "Dialog Body", "Ok", "Cancel");
                request = new ConnectionRequest();


                //Dialog.show("Foodmates Error", myrole, "Proceed", null);
                request.setUrl(apiURL);
                request.setPost(true);
                request.setHttpMethod("POST");
                request.setContentType("application/x-www-form-urlencoded");
                request.addRequestHeader("Connection","Keep-Alive");
                request.addArgument("function", "authenticate_driver");
                request.addArgument("email", email.getText());

                Map<String, Object> response = traceMatesAPI(request);
                Log.p((String) response.toString());
                if(response.get("success").equals("false")){
                    Dialog.show("Tracemates Error", "wrong email/password combination", "Ok", null);
                }
                if(response != null && response.get("success").equals("true")){
                    driverDetails = (HashMap<String, String>) response.get("data");
                    Log.p((String) driverDetails.toString());
                    trackingId = driverDetails.get("driverId");
                    listParcels();
                }
            }
        });
        home.addComponent(email);
        home.addComponent(password);
        home.addComponent(submit);
        current = home;
        start();
    }

    private void listParcels(){
        ArrayList parcels = allParcelsAssignedToDriver();
        home = new Form("Parcels");
        home.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Container list = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        list.setScrollableY(true);
        for(Map parcel : (ArrayList<Map>) parcels) {

            String name = (String)parcel.get("name");
            Button b = new Button(name);
            list.add(b);
            b.addActionListener(e -> trackParcel(parcel));
        }
        home.addComponent(list);
        current = home;
        start();
    }

    private  Map<String,Object> traceMatesAPI(ConnectionRequest request){

        Log.p(request.getUrl());
        Map<String, Object> result = null;

        try {
            NetworkManager.getInstance().addToQueueAndWait(request);
            JSONParser p = new JSONParser();
            result = p.parseJSON(new InputStreamReader(new ByteArrayInputStream(request.getResponseData())));
            Log.p("after parsing");
            Log.p((String) result.get("success"));

            if (result.get("success").toString().equalsIgnoreCase("true")) {
                Log.p("response: " + result.toString());
                return result;

            }
        }catch (IOException ioExeption){
                Dialog.show("Tracemates Error", String.valueOf(ioExeption), "Proceed", null);
        }

        return result;
    }

    private ArrayList allParcelsAssignedToDriver(){
        ArrayList parcels = null;
        request = new ConnectionRequest();
        request.setUrl(apiURL);
        request.setPost(true);
        request.setHttpMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.addRequestHeader("Connection","Keep-Alive");
        request.addArgument("function", "list_driver_parcels");
        request.addArgument("tracker_id", trackingId);

        Map<String, Object> response = traceMatesAPI(request);
        if(response != null){
            parcels = (ArrayList) response.get("data");
            //List<Object> parcelHashes = Arrays.asList(parcels);
            return parcels;
            //Log.p((String) parcels.toString());
            //trackingId = parcels.get("driverId");
            //trackParcel();
        }
        return parcels;
    }

}
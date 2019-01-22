package me.sorby.googlehome.network;

import me.sorby.googlehome.applications.*;
import me.sorby.googlehome.devices.CastDevice;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DeviceConnection implements ReceiverMessageListener, MediaMessageListener {
    private int msgId = 0;
    private TransportConnection tc;
    private Receiver receiver;
    private Media media;
    private String lastTransportId = "";

    public DeviceConnection(CastDevice device) {
        tc = new TransportConnection(device.getIp(), device.getPort());
        receiver = new Receiver(tc);
        addReceiverMessageListener("RECEIVER_STATUS", this); //To obtain media transportId
        receiver.connect();
        receiver.requestStatus();
    }

    @Override
    public void messageReceived(String application, String type, JSONObject payload) {
        if (application.equals("Receiver") && type.equals("RECEIVER_STATUS") && payload.containsKey("status")) {
            JSONObject statusJSON = (JSONObject) payload.get("status");
            if (statusJSON.containsKey("applications")) { //If there is a running app...
                JSONArray applicationsJSON = (JSONArray) statusJSON.get("applications");
                JSONObject appJSON = (JSONObject) applicationsJSON.get(0);
                if (appJSON.containsKey("transportId")) { //...and receiver status contains his transportId
                    if (media == null) { //If there isn't an already open media session
                        media = new Media(tc, appJSON.get("transportId").toString()); //Open a new session
                        //addMediaMessageListener("MEDIA_STATUS", this); //Not used at the moment
                        media.connect();
                        media.requestStatus();
                    } else if (!appJSON.get("transportId").equals(lastTransportId)) { //If there is an already open media session but the transportId has changed...
                        //We use changeTrasportId method in order to preserve all the previous events binding
                        lastTransportId = appJSON.get("transportId").toString();
                        media.changeTransportId(lastTransportId); //...change media transportId
                        media.connect(); //Connect to the new virtual channel
                        media.requestStatus();
                    }
                }
            }
        }
    }

    //Conveninence method for adding Receiver message listener (L3)
    public void addReceiverMessageListener(String type, ReceiverMessageListener listener) {
        ReceiverMessageDispatcher.getInstance(receiver).addMessageListener(type, listener);
    }

    //Conveninence method for adding Media message listener (L3)
    public void addMediaMessageListener(String type, MediaMessageListener listener) {
        MediaMessageDispatcher.getInstance(media).addMessageListener(type, listener);
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Media getMedia() {
        return media;
    }

    public void closeConnection() {
        if (media != null)
            media.close();

        if (receiver != null)
            receiver.close();

        tc.close();
    }
}

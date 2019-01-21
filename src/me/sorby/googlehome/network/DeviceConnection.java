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
        addReceiverMessageListener("RECEIVER_STATUS", this);
        receiver.connect();
        receiver.requestStatus();
    }

    @Override
    public void messageReceived(String application, String type, JSONObject payload) {
        if(application.equals("Receiver") && type.equals("RECEIVER_STATUS") && payload.containsKey("status")){
            JSONObject statusJSON = (JSONObject) payload.get("status");
            if(statusJSON.containsKey("applications")){
                JSONArray applicationsJSON = (JSONArray) statusJSON.get("applications");
                JSONObject appJSON = (JSONObject) applicationsJSON.get(0);
                if(appJSON.containsKey("transportId")){
                    if(media == null){
                        media = new Media(tc, appJSON.get("transportId").toString());
                        addMediaMessageListener("MEDIA_STATUS", this);
                        media.connect();
                        media.requestStatus();
                    }else if(!appJSON.get("transportId").equals(lastTransportId)){
                        lastTransportId = appJSON.get("transportId").toString();
                        media.changeTransportId(lastTransportId);
                        media.connect();
                        media.requestStatus();
                    }
                }
            }
        }
    }


    public void addReceiverMessageListener(String type, ReceiverMessageListener listener){
        ReceiverMessageDispatcher.getInstance(receiver).addMessageListener(type, listener);
    }

    public void addMediaMessageListener(String type, MediaMessageListener listener){
        MediaMessageDispatcher.getInstance(media).addMessageListener(type, listener);
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Media getMedia() {
        return media;
    }

    public void closeConnection() {
        if(media != null)
            media.close();

        if(receiver != null)
            receiver.close();

        tc.close();
    }
}

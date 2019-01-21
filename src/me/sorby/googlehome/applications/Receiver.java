package me.sorby.googlehome.applications;

import me.sorby.googlehome.network.CastChannel;
import me.sorby.googlehome.network.ContextualChannelMessageListener;
import me.sorby.googlehome.network.TransportConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Receiver implements ContextualChannelMessageListener {
    private CastChannel channel;
    private int msgId=0;
    public final String namespace = "urn:x-cast:com.google.cast.receiver";
    private float volumeLevel;
    private boolean muted;
    private String statusText = "";
    private String appIconUrl = "";
    private boolean appRunning = false;

    public Receiver(TransportConnection tc){
        channel = new CastChannel(tc);
        channel.setName("Receiver");
        channel.addMessageListener(namespace, this);
    }

    public void connect(){
        channel.openVirtualConnection();
        channel.keepAlive();
    }

    public void requestStatus(){
        JSONObject msg = new JSONObject();
        msg.put("type", "GET_STATUS");
        msg.put("requestId", ++msgId);
        channel.send(namespace, msg);
    }

    public void close(){
        channel.closeVirtualConnection();
    }


    public float getVolumeLevel() {
        return volumeLevel;
    }

    public boolean isMuted() {
        return muted;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getAppIconUrl() {
        return appIconUrl;
    }

    public boolean isAppRunning() {
        return appRunning;
    }

    @Override
    public void messageReceived(CastChannel channel, String namespace, JSONObject payload) {
        ReceiverMessageDispatcher.getInstance(this).dispatch(this.getClass().getSimpleName(), payload.get("type").toString(), payload);
        if(payload.get("type").toString().equals("RECEIVER_STATUS") && payload.containsKey("status")){
            JSONObject statusJSON = (JSONObject) payload.get("status");
            JSONObject volumeJSON = (JSONObject) statusJSON.get("volume");
            volumeLevel = new Float(volumeJSON.get("level").toString());
            muted = volumeJSON.get("muted").toString().equals("true");
            appRunning = statusJSON.containsKey("applications");
            if(appRunning){
                JSONArray applicationsJSON = (JSONArray) statusJSON.get("applications");
                JSONObject appJSON = (JSONObject) applicationsJSON.get(0);
                statusText = (appJSON.containsKey("statusText") ? appJSON.get("statusText").toString() : "");
                appIconUrl = (appJSON.containsKey("iconUrl") ? appJSON.get("iconUrl").toString() : "https://lh3.googleusercontent.com/LB5CRdhftEGo2emsHOyHz6NWSfLVD5NC45y6auOqYoyrv7BC5mdDm66vPDCEAJjcDA=w360");
            }else{
                statusText = "Ready to cast";
                appIconUrl = "https://lh3.googleusercontent.com/LB5CRdhftEGo2emsHOyHz6NWSfLVD5NC45y6auOqYoyrv7BC5mdDm66vPDCEAJjcDA=w360";
            }
            ReceiverMessageDispatcher.getInstance(this).dispatch(this.getClass().getSimpleName(), "INFO_READY", new JSONObject());
        }
    }
}

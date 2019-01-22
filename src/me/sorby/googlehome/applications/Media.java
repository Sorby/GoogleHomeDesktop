package me.sorby.googlehome.applications;

import me.sorby.googlehome.network.CastChannel;
import me.sorby.googlehome.network.ContextualChannelMessageListener;
import me.sorby.googlehome.network.TransportConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Media implements ContextualChannelMessageListener {
    private final String namespace = "urn:x-cast:com.google.cast.media";
    private CastChannel channel;
    private TransportConnection tc;
    private int msgId = 0;
    private String title = "";
    private String artist = "";
    private String albumName = "";
    private String imageURL = "";

    public Media(TransportConnection tc, String transportId) {
        channel = new CastChannel(tc, transportId);
        this.tc = tc;
        channel.setName("Media");
        channel.addMessageListener(namespace, this);
    }

    //Convenience method to allow changes of media session (eg by changing the cast application) without the need to rebind all the events
    public void changeTransportId(String transportId) {
        channel.closeVirtualConnection();
        channel = new CastChannel(tc, transportId);
        channel.setName("Media");
        channel.addMessageListener(namespace, this);
    }

    public void connect() {
        channel.openVirtualConnection();
        //We don't need to start the keep-alive pinger here, because we only need one of these for each transport connection
    }

    public void requestStatus() {
        JSONObject msg = new JSONObject();
        msg.put("type", "GET_STATUS");
        msg.put("requestId", ++msgId);
        channel.send(namespace, msg);
    }

    public void close() {
        channel.closeVirtualConnection();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getImageURL() {
        return imageURL;
    }

    @Override
    public void messageReceived(CastChannel channel, String namespace, JSONObject payload) {
        //Send every Contextual (L2) (namespace+channel based) event to the Media dispatcher (L3)
        MediaMessageDispatcher.getInstance(this).dispatch(this.getClass().getSimpleName(), payload.get("type").toString(), payload);

        //Update instance fields when receiving MEDIA_STATUS
        if (payload.get("type").toString().equals("MEDIA_STATUS") && payload.containsKey("status")) {
            JSONArray statusJSONarr = (JSONArray) payload.get("status");
            if (!statusJSONarr.isEmpty()) {
                JSONObject statusJSON = (JSONObject) statusJSONarr.get(0);
                if (statusJSON.containsKey("media")) { //Not all MEDIA_STATUS events contains media info
                    JSONObject mediaJSON = (JSONObject) statusJSON.get("media");
                    JSONObject metadataJSON = (JSONObject) mediaJSON.get("metadata");
                    title = (metadataJSON.containsKey("title") ? metadataJSON.get("title").toString() : "");
                    artist = (metadataJSON.containsKey("artist") ? metadataJSON.get("artist").toString() : "");
                    albumName = (metadataJSON.containsKey("albumName") ? metadataJSON.get("albumName").toString() : "");
                    JSONArray imagesJSONarr = (JSONArray) metadataJSON.get("images");
                    JSONObject imagesJSON = (JSONObject) imagesJSONarr.get(0);
                    imageURL = imagesJSON.get("url").toString();
                    imageURL = (imagesJSON.containsKey("url") ? imagesJSON.get("url").toString() : "");

                    //Custom event sent when updating media info fields
                    MediaMessageDispatcher.getInstance(this).dispatch(this.getClass().getSimpleName(), "INFO_READY", new JSONObject());
                }
            }

        }
    }
}

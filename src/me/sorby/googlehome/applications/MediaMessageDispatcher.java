package me.sorby.googlehome.applications;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class MediaMessageDispatcher {
    private static HashMap<Media, MediaMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ArrayList<MediaMessageListener>> listenerHashMap = new HashMap<>();
    private Media media;
    private final static Logger logger = LoggerFactory.getLogger(MediaMessageDispatcher.class);

    private MediaMessageDispatcher(Media media){
        instances.put(media, this);
        this.media = media;

    }

    //Factory pattern
    public static MediaMessageDispatcher getInstance(Media media){
        if(media == null)
            throw new NullPointerException("cannot get dispatcher instance of null");
        if(instances.containsKey(media))
            return instances.get(media);
        return new MediaMessageDispatcher(media);
    }

    public void addMessageListener(String type, MediaMessageListener listener){
        listenerHashMap.putIfAbsent(type, new ArrayList<>());
        if(!listenerHashMap.get(type).contains(listener))
            listenerHashMap.get(type).add(listener);
    }

    //Observer pattern
    public void dispatch(String application, String type, JSONObject payload){
        if(listenerHashMap.containsKey(type))
            for(MediaMessageListener listener : listenerHashMap.get(type)){
                listener.messageReceived(application, type, payload);
            }
        else
            logger.debug("No event listener for namespace "+type);
    }

}

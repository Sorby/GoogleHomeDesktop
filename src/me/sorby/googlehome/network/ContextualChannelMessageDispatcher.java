package me.sorby.googlehome.network;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ContextualChannelMessageDispatcher {
    private static HashMap<CastChannel, ContextualChannelMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ContextualChannelMessageListener> listenerHashMap = new HashMap<>();
    private CastChannel channel;
    private final static Logger logger = LoggerFactory.getLogger(ContextualChannelMessageDispatcher.class);

    private ContextualChannelMessageDispatcher(CastChannel channel){
        instances.put(channel, this);
        this.channel = channel;

    }

    //Factory pattern
    public static ContextualChannelMessageDispatcher getInstance(CastChannel channel){
        if(instances.containsKey(channel))
            return instances.get(channel);
        return new ContextualChannelMessageDispatcher(channel);
    }

    public void addMessageListener(String namespace, ContextualChannelMessageListener listener){
        listenerHashMap.putIfAbsent(namespace, listener);
    }

    //Observer pattern
    public void dispatch(String namespace, JSONObject payload){
        if(listenerHashMap.containsKey(namespace))
            listenerHashMap.get(namespace).messageReceived(channel, namespace, payload);
        else
            logger.debug("No event listener for namespace "+namespace+" on "+channel.getName());
    }

}

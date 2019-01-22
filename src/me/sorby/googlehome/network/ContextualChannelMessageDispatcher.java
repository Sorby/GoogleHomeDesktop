package me.sorby.googlehome.network;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ContextualChannelMessageDispatcher {
    private final static Logger logger = LoggerFactory.getLogger(ContextualChannelMessageDispatcher.class);
    private static HashMap<CastChannel, ContextualChannelMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ContextualChannelMessageListener> listenerHashMap = new HashMap<>();
    private CastChannel channel;

    private ContextualChannelMessageDispatcher(CastChannel channel) {
        instances.put(channel, this);
        this.channel = channel;

    }

    //Factory pattern
    public static ContextualChannelMessageDispatcher getInstance(CastChannel channel) {
        if (instances.containsKey(channel))
            return instances.get(channel);
        return new ContextualChannelMessageDispatcher(channel);
    }

    public void addMessageListener(String namespace, ContextualChannelMessageListener listener) {
        //We use hasmap strcture, without ArrayList as every L1 virtual channel message must be received by ONLY ONE L2 virtual channel context
        //We identify the virtual channel using his senderId (that appear as destionationId in received messages)
        listenerHashMap.putIfAbsent(namespace, listener);
    }

    //Observer pattern
    //Dispatch every L1 (virtual channel message) message to the L2 virtual channel context (namespace @ virtual channel)
    //L2 didpatcher Called by CastChannel that acts as a ChannelMessageListener for L1 virtual channel messages
    public void dispatch(String namespace, JSONObject payload) {
        if (listenerHashMap.containsKey(namespace))
            listenerHashMap.get(namespace).messageReceived(channel, namespace, payload);
        else
            logger.debug("No event listener for namespace " + namespace + " on " + channel.getName());
    }

}

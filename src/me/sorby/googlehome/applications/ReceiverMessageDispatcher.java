package me.sorby.googlehome.applications;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceiverMessageDispatcher {
    private static HashMap<Receiver, ReceiverMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ArrayList<ReceiverMessageListener>> listenerHashMap = new HashMap<>();
    private Receiver receiver;
    private final static Logger logger = LoggerFactory.getLogger(ReceiverMessageDispatcher.class);

    private ReceiverMessageDispatcher(Receiver receiver){
        instances.put(receiver, this);
        this.receiver = receiver;

    }

    //Factory pattern
    public static ReceiverMessageDispatcher getInstance(Receiver receiver){
        if(receiver == null)
            throw new NullPointerException("cannot get dispatcher instance of null");
        if(instances.containsKey(receiver))
            return instances.get(receiver);
        return new ReceiverMessageDispatcher(receiver);
    }

    public void addMessageListener(String type, ReceiverMessageListener listener){
        listenerHashMap.putIfAbsent(type, new ArrayList<>());
        if(!listenerHashMap.get(type).contains(listener))
            listenerHashMap.get(type).add(listener);
    }

    //Observer pattern
    public void dispatch(String application, String type, JSONObject payload){
        if(listenerHashMap.containsKey(type))
            for(ReceiverMessageListener listener : listenerHashMap.get(type))
                listener.messageReceived(application, type, payload);
        else
            logger.debug("No event listener for namespace "+type);
    }

}

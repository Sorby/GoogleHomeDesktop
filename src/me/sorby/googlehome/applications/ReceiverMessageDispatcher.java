package me.sorby.googlehome.applications;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceiverMessageDispatcher {
    private final static Logger logger = LoggerFactory.getLogger(ReceiverMessageDispatcher.class);
    private static HashMap<Receiver, ReceiverMessageDispatcher> instances = new HashMap<>();
    private HashMap<String, ArrayList<ReceiverMessageListener>> listenerHashMap = new HashMap<>();
    private Receiver receiver;

    private ReceiverMessageDispatcher(Receiver receiver) {
        instances.put(receiver, this);
        this.receiver = receiver;

    }

    //Factory pattern
    public static ReceiverMessageDispatcher getInstance(Receiver receiver) {
        if (receiver == null) //Prevent bindings when receiver isn't ready yet (shouldn't never happen)
            throw new NullPointerException("cannot get dispatcher instance of null");
        if (instances.containsKey(receiver))
            return instances.get(receiver);
        return new ReceiverMessageDispatcher(receiver);
    }

    public void addMessageListener(String type, ReceiverMessageListener listener) {
        //Using HashMap + ArrayList structure because we can have multiple listener for one ReceiverMessage Type (eg. RECEIVER_STATUS inside Receuver class and or each class that requires media transportId/sessionId)
        listenerHashMap.putIfAbsent(type, new ArrayList<>());
        if (!listenerHashMap.get(type).contains(listener))
            listenerHashMap.get(type).add(listener);
    }

    //Observer pattern
    //L3 dispatcher called by Receiver class that acts as a Contextual Message Listener (L2)
    public void dispatch(String application, String type, JSONObject payload) {
        if (listenerHashMap.containsKey(type))
            for (ReceiverMessageListener listener : listenerHashMap.get(type))
                listener.messageReceived(application, type, payload);
        else
            logger.debug("No event listener for namespace " + type);
    }

}

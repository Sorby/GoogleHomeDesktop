package me.sorby.googlehome.applications;

import org.json.simple.JSONObject;

public interface ReceiverMessageListener {
    void messageReceived(String application, String type, JSONObject payload);
}

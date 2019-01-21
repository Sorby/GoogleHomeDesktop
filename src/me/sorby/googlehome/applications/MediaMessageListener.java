package me.sorby.googlehome.applications;

import org.json.simple.JSONObject;

public interface MediaMessageListener {
    void messageReceived(String application, String type, JSONObject payload);
}

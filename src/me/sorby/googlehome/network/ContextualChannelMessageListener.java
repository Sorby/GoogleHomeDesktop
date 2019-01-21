package me.sorby.googlehome.network;

import org.json.simple.JSONObject;

import java.util.EventListener;

public interface ContextualChannelMessageListener extends EventListener {
    void messageReceived(CastChannel channel, String namespace, JSONObject payload);
}

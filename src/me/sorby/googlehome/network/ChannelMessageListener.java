package me.sorby.googlehome.network;

import extensions.api.cast_channel.CastChannelProto;

import java.util.EventListener;

public interface ChannelMessageListener extends EventListener {
    void messageReceived(CastChannelProto.CastMessage msg);
}

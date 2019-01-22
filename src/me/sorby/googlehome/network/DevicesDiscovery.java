package me.sorby.googlehome.network;

import me.sorby.googlehome.devices.CastDevice;
import me.sorby.googlehome.exceptions.DeviceNotSupported;
import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.sd.Instance;
import net.straylightlabs.hola.sd.Query;
import net.straylightlabs.hola.sd.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DevicesDiscovery {

    private final static Logger logger = LoggerFactory.getLogger(DevicesDiscovery.class);

    private List<CastDevice> devices = new ArrayList<>();

    public DevicesDiscovery() {
        try {
            Service service = Service.fromName("_googlecast._tcp"); //mDNS query
            Query query = Query.createWithTimeout(service, Domain.LOCAL, 1500);
            Set<Instance> instances = query.runOnce();

            for (Instance i : instances)
                devices.add(CastDevice.registerDevice(i));

        } catch (IOException e) {
            logger.error("IO error: ", e);
        } catch (DeviceNotSupported e) {
            logger.error(e.getMessage());
        }
    }

    public List<CastDevice> getDevices() {
        return devices;
    }
}

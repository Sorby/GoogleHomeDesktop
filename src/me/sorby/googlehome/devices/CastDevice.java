package me.sorby.googlehome.devices;

import me.sorby.googlehome.exceptions.DeviceNotSupported;
import net.straylightlabs.hola.sd.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public abstract class CastDevice {
    String deviceName;
    String ip;
    int port;
    String name;
    private ImageIcon flagIcon;
    private final static Logger logger = LoggerFactory.getLogger(CastDevice.class);

    public CastDevice(String deviceName, String ip, int port) {
        this.deviceName = deviceName;
        this.ip = ip;
        this.port = port;
    }

    // Register device through mDNS response
    public static CastDevice registerDevice(Instance i) throws DeviceNotSupported {
        String IP = i.getAddresses().iterator().next().getHostAddress();
        String name;
        String type;


        CastDevice device = null;

        if (i.lookupAttribute("md") != null)
            //As every device could have different specifications, we create the specific class instance
            switch (type = i.lookupAttribute("md")) {
                case "Chromecast":
                    device = new Chromecast(i.getName(), IP, i.getPort());
                    break;
                case "Google Home":
                    device = new GoogleHome(i.getName(), IP, i.getPort());
                    break;
                case "Google Home Mini":
                    device = new GoogleHomeMini(i.getName(), IP, i.getPort());
                    break;
                case "Google Cast Group":
                    device = new GoogleCastGroup(i.getName(), IP, i.getPort());
                    break;
                default:
                    throw new DeviceNotSupported("Unsupported device type=" + type);
            }

        if ((name = i.lookupAttribute("fn")) != null)
            device.setName(name);

        return device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    //Icons are inside resources folder, named as the device class with png extensions
    public ImageIcon getIcon() {
        if (flagIcon == null) {
            try {
                InputStream imgStream = getClass().getClassLoader().getResourceAsStream("resources/" + getType() + ".png");
                if (imgStream != null) {
                    flagIcon = new ImageIcon(ImageIO.read(imgStream));
                }else {
                    imgStream = getClass().getClassLoader().getResourceAsStream("resources/" + "unknown"+ ".png");
                    flagIcon = new ImageIcon(ImageIO.read(imgStream));
                }
            } catch (IOException e) {
                logger.error("Cannot load resource : "+e);
            }
        }

        return flagIcon;
    }

    @Override
    public String toString() {
        return "CastDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", name='" + name + '\'' +
                ", type='" + getType() + '\'' +
                '}';
    }
}

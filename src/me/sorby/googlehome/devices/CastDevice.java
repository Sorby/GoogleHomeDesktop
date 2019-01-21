package me.sorby.googlehome.devices;

import me.sorby.googlehome.exceptions.DeviceNotSupported;
import net.straylightlabs.hola.sd.Instance;

import javax.swing.*;
import java.io.File;

public abstract class CastDevice {
    String deviceName;
    String ip;
    int port;
    String name;
    private ImageIcon flagIcon;

    public CastDevice(String deviceName, String ip, int port) {
        this.deviceName = deviceName;
        this.ip = ip;
        this.port = port;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getType() {
        return getClass().getSimpleName();
    }

    public ImageIcon getIcon()
    {
        if (flagIcon == null){
            File img = new File(getType()+".png");
            if(img.exists())
                flagIcon = new ImageIcon(img.getPath());
            else
                flagIcon = new ImageIcon("unknown.png");
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

    public static CastDevice registerDevice(Instance i) throws DeviceNotSupported {
        String IP = i.getAddresses().iterator().next().getHostAddress();
        String name;
        String type;


        CastDevice device = null;

        if(i.lookupAttribute("md")!=null)
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
                    throw new DeviceNotSupported("Unsupported device type="+type);
            }

        if((name = i.lookupAttribute("fn"))!=null)
            device.setName(name);

        return device;
    }
}

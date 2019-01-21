package me.sorby.googlehome.gui;

import me.sorby.googlehome.devices.CastDevice;

import javax.swing.*;
import java.awt.*;

public class DevicesCells extends JLabel implements ListCellRenderer<CastDevice> {

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    @Override
    public Component getListCellRendererComponent(
            JList<? extends CastDevice> list,           // the list
            CastDevice device,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // does the cell have focus
    {
        setText(device.getName());
        setIcon(device.getIcon());
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }

}

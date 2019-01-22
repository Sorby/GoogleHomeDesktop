package me.sorby.googlehome.gui;

import me.sorby.googlehome.devices.CastDevice;

import javax.swing.*;
import java.awt.*;

public class DevicesCells extends JLabel implements ListCellRenderer<CastDevice> {

    // This is the only method defined by ListCellRenderer (interface)
    // This method is called for every new label inside our JList, we only just add the device icon and name
    @Override
    public Component getListCellRendererComponent(
            JList<? extends CastDevice> list,           // the list
            CastDevice device,            // label value (as object)
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

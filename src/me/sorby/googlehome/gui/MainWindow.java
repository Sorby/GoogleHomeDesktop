package me.sorby.googlehome.gui;

import com.sun.webkit.dom.RectImpl;
import jdk.nashorn.internal.runtime.events.RecompilationEvent;
import me.sorby.googlehome.applications.Media;
import me.sorby.googlehome.applications.MediaMessageListener;
import me.sorby.googlehome.applications.Receiver;
import me.sorby.googlehome.applications.ReceiverMessageListener;
import me.sorby.googlehome.devices.CastDevice;
import me.sorby.googlehome.network.DeviceConnection;
import me.sorby.googlehome.network.DevicesDiscovery;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainWindow extends JFrame implements ListSelectionListener, ReceiverMessageListener, MediaMessageListener {
    private JPanel mainPanel;
    private JSplitPane mainSplitPane;
    private JList<CastDevice> devicesList;
    private JPanel actionPanel;
    private DeviceConnection deviceConnection;
    private JLabel deviceName;
    private JLabel deviceAddress;
    private JLabel statusText;
    private JLabel appPicLab;
    private JPanel mediaControlPanel;
    private JLabel mediaTitle;
    private JLabel mediaArtist;
    private JLabel mediaAlbum;
    private JLabel mediaImage;

    public MainWindow() {
        initFrame();
    }

    private void initFrame() {
        setTitle("Google Home Desktop");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //termina il programma quando si chiude la finestra
        //centerWindow();
        initMainPanel();
        setVisible(true);
        populateDevicesList();
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private void initMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setDividerSize(0);
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        initDevicesList();
        initActionPanel();
        add(mainPanel);
    }

    private void initDevicesList() {
        devicesList = new JList<>();
        devicesList.setBackground(Color.getHSBColor(1, 0, 0.97f));
        devicesList.setCellRenderer(new DevicesCells());
        devicesList.addListSelectionListener(this);
        mainSplitPane.add(devicesList);
    }

    private void initActionPanel() {
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridBagLayout());
        actionPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridy = 0;
        deviceName = new JLabel("Seleziona un dispositivo");
        deviceName.setFont(getFont("Calibri", Font.BOLD, 24, deviceName.getFont()));
        actionPanel.add(deviceName, gbc);
        deviceAddress = new JLabel("");
        deviceAddress.setFont(getFont("Calibri", Font.PLAIN, 12, deviceAddress.getFont()));
        gbc.gridy++;
        gbc.fill=GridBagConstraints.NORTH;
        gbc.weighty=0.15;
        actionPanel.add(deviceAddress, gbc);
        gbc.weighty=0;
        gbc.fill=GridBagConstraints.NONE;
        try {
            gbc.gridy++;
            BufferedImage appPic = ImageIO.read(new File("unknown.png"));
            appPicLab = new JLabel(getScaledImageIcon(new ImageIcon(appPic), 64, 64));
            appPicLab.setVisible(false);
            actionPanel.add(appPicLab, gbc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusText = new JLabel("");
        statusText.setFont(getFont("Calibri", Font.ITALIC, 14, statusText.getFont()));
        gbc.gridy++;
        gbc.weighty = 1;
        actionPanel.add(statusText, gbc);
        gbc.gridy++;
        initMediaControlPanel();
        actionPanel.add(mediaControlPanel, gbc);
        mainSplitPane.add(actionPanel);
    }

    private void initMediaControlPanel(){
        mediaControlPanel = new JPanel();
        mediaControlPanel.setLayout(new GridBagLayout());
        mediaControlPanel.setBackground(Color.getHSBColor(1, 0, 0.97f));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.ipadx = 10;
        gbc.anchor = GridBagConstraints.LINE_START;
        mediaImage = new JLabel("");
        mediaControlPanel.add(mediaImage, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.ipadx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        mediaTitle = new JLabel("");
        mediaTitle.setFont(getFont("Calibri", Font.BOLD, 16, mediaTitle.getFont()));
        mediaControlPanel.add(mediaTitle, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mediaArtist = new JLabel("");
        mediaArtist.setFont(getFont("Calibri", Font.PLAIN, 13, mediaArtist.getFont()));
        mediaControlPanel.add(mediaArtist, gbc);

        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        mediaAlbum = new JLabel("");
        mediaAlbum.setFont(getFont("Calibri", Font.ITALIC, 13, mediaAlbum.getFont()));
        mediaControlPanel.add(mediaAlbum, gbc);

        mediaControlPanel.setVisible(false);
    }

    private void populateDevicesList() {
        List<CastDevice> devices = new DevicesDiscovery().getDevices();
        DefaultListModel<CastDevice> listModel = new DefaultListModel<>();
        for (CastDevice cd : devices)
            listModel.addElement(cd);
        devicesList.setModel(listModel);
    }


    private Font getFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    private ImageIcon getScaledImageIcon(ImageIcon srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }


    private void castDeviceSelected(CastDevice selectedDevice) {
        if (deviceConnection != null)
            deviceConnection.closeConnection();
        deviceConnection = new DeviceConnection(selectedDevice);
        deviceConnection.addReceiverMessageListener("INFO_READY", this);
        deviceName.setText(selectedDevice.getName());
        deviceAddress.setText(selectedDevice.getIp());
        statusText.setText("");
        appPicLab.setVisible(false);
    }

    public static void main(String[] args) {
        //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new MainWindow();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!devicesList.getValueIsAdjusting()) {
            castDeviceSelected(devicesList.getSelectedValue());
        }
    }

    @Override
    public void messageReceived(String application, String type, JSONObject payload) {
        if(application.equals("Receiver") && type.equals("INFO_READY")){
            Receiver receiver = deviceConnection.getReceiver();
            statusText.setText(receiver.getStatusText());
            try {
                BufferedImage appPic = ImageIO.read(new URL(receiver.getAppIconUrl()));
                appPicLab.setIcon(getScaledImageIcon(new ImageIcon(appPic), 64, 64));
                appPicLab.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!receiver.isAppRunning())
                mediaControlPanel.setVisible(false);
            if(deviceConnection.getMedia() != null)
                deviceConnection.addMediaMessageListener("INFO_READY", this);
        }else if(application.equals("Media") && type.equals("INFO_READY")){
            mediaControlPanel.setVisible(true);
            Media media = deviceConnection.getMedia();
            mediaTitle.setText(media.getTitle());
            mediaArtist.setText(media.getArtist());
            mediaAlbum.setText(media.getAlbumName());
            try {
                if(!media.getImageURL().equals("")){
                    URL imgFile = new URL(media.getImageURL());
                    BufferedImage appPic = ImageIO.read(imgFile);
                    if(appPic == null)
                        throw new IOException("Cannot load Image");
                    ImageIcon originalImg = new ImageIcon(appPic);
                    ImageIcon img = getScaledImageIcon(originalImg, 128, 128);
                    mediaImage.setIcon(img);
                    mediaImage.setVisible(true);
                }else
                    mediaImage.setVisible(false);
            } catch (IOException e) {
                mediaImage.setVisible(false);
                e.printStackTrace();
            }
        }
    }
}

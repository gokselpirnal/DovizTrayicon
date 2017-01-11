import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static final SystemTray tray = SystemTray.getSystemTray();
    static final PopupMenu popup = new PopupMenu();
    static TrayIcon trayIcon = null;

    public static void main(String[] args) throws Exception {
        tray.add(refreshTrayIcon(getData()));
    }

    private static void refresh() throws Exception {
        refreshTrayIcon(getData());
    }

    private static List<Value> getData() throws Exception {
        return getValues(httpGet("http://api.doviz.com/list/C"));
    }

    private static TrayIcon refreshTrayIcon(List<Value> values) {

        if (trayIcon == null) {
            trayIcon = new TrayIcon(createImage(values.get(0), false));
        } else {
            trayIcon.setImage(createImage(values.get(0), false));
        }

        popup.removeAll();
        // dolar, euro, sterlin, kanada doları
        for (int i = 0; i < 4; i++) {
            popup.add(new MenuItem(values.get(i).getKey() + ": " + values.get(i).getAlis()));
        }

        MenuItem refresh = new MenuItem("Yenile");
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    refresh();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        popup.add(refresh);
        trayIcon.setPopupMenu(popup);
        return trayIcon;
    }

    private static BufferedImage createImage(Value value, boolean isMenuOpen) {
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        int taskBarHeight = scrnSize.height - winSize.height;
        int fontSize = (taskBarHeight / 10) * 7;
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        String displayText = value.getKey() + ": " + value.getAlis();
        int trayiconWidth = (int) (font.getStringBounds(displayText, new FontRenderContext(new AffineTransform(), true, true)).getWidth());

        BufferedImage image = new BufferedImage(trayiconWidth, taskBarHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        if (isMenuOpen) {
            g2d.setColor(Color.white);
        } else if (value.getUpDown().equals("1")) {
            g2d.setColor(Color.decode("#16a085"));
        } else {
            g2d.setColor(Color.decode("#c0392b"));
        }

        g2d.drawString(displayText, 0, (taskBarHeight / 2) + (fontSize / 2));
        g2d.dispose();

        return image;
    }

    private static String httpGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private static List<Value> getValues(String str) {

        List<Value> values = new ArrayList<>();

        Pattern keyPattern = Pattern.compile("\"key\":\"([^\"]+)\"");
        Pattern alisPattern = Pattern.compile("\"alis\":\"([^\"]+)\"");
        Pattern upDownPattern = Pattern.compile("\"upDown\":(-?1)");

        Matcher keyMatcher = keyPattern.matcher(str);
        Matcher alisMatcher = alisPattern.matcher(str);
        Matcher upDownMatcher = upDownPattern.matcher(str);

        while (keyMatcher.find() && alisMatcher.find() && upDownMatcher.find()) {
            values.add(new Value(keyMatcher.group(1), alisMatcher.group(1), upDownMatcher.group(1)));
        }

        return values;
    }

    public static class Value {
        private String key;
        private String alis;
        private String upDown;

        public Value(String key, String alis, String upDown) {
            setKey(key);
            setAlis(alis);
            setUpDown(upDown);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAlis() {
            return alis;
        }

        public void setAlis(String alis) {
            this.alis = alis;
        }

        public String getUpDown() {
            return upDown;
        }

        public void setUpDown(String upDown) {
            this.upDown = upDown;
        }
    }

}

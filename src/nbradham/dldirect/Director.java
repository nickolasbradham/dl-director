package nbradham.dldirect;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

final class Director {

	private static final File F_CFG = new File("director.cfg");

	private void start() throws IOException, AWTException {
		TrayIcon ti = new TrayIcon(ImageIO.read(Director.class.getResource("/icon.png")), "Download Director");
		ti.setImageAutoSize(true);

		SystemTray st = SystemTray.getSystemTray();
		MenuItem mi = new MenuItem("Exit");
		mi.addActionListener(e -> st.remove(ti));

		PopupMenu pm = new PopupMenu();
		pm.add(mi);
		ti.setPopupMenu(pm);
		st.add(ti);

		Properties props = new Properties();
		if (F_CFG.exists())
			props.load(new FileInputStream(F_CFG));
		else {
			ti.displayMessage("Download Director",
					"If you ever want to undo an automatic action, just click me in the system tray.",
					MessageType.INFO);
			props.store(new FileOutputStream(F_CFG), "Careful. This file is sensitive.");
		}
	}

	public static void main(String[] args) throws IOException, AWTException {
		new Director().start();
	}
}
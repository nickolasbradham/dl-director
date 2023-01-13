package nbradham.dldirect;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

final class Director {

	static final char A_ASK = 'A', A_IGNORE = 'I', A_MOVE = 'M', A_RUN = 'R';
	private static final File F_CFG = new File("director.cfg"), F_TMP = new File(System.getenv("TMP"), "DownDirector");
	private static final Path P_DOWN = Path.of(System.getProperty("user.home"), "Downloads");
	private static final JFileChooser jfc = new JFileChooser();

	private boolean run = true;

	private void start() throws IOException, AWTException, InterruptedException {
		F_TMP.mkdirs();

		TrayIcon ti = new TrayIcon(ImageIO.read(Director.class.getResource("/icon.png")), "Download Director");
		ti.setImageAutoSize(true);

		SystemTray st = SystemTray.getSystemTray();
		MenuItem mi = new MenuItem("Exit");
		WatchService ws = FileSystems.getDefault().newWatchService();
		mi.addActionListener(e -> {
			run = false;
			try {
				ws.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			st.remove(ti);
		});

		PopupMenu pm = new PopupMenu();
		pm.add(mi);
		ti.setPopupMenu(pm);
		st.add(ti);

		Properties props = new Properties();
		if (F_CFG.exists())
			props.load(new FileInputStream(F_CFG));
		else {
			ti.displayMessage("Download Director",
					"If you ever want to undo or change an automatic action, just click me in the system tray.",
					MessageType.INFO);
			props.store(new FileOutputStream(F_CFG), "Careful. This file is sensitive.");
		}

		P_DOWN.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
		File dlDir = P_DOWN.toFile();
		ActionChooser ac = new ActionChooser();
		while (true) {
			try {
				ws.take();
			} catch (ClosedWatchServiceException e) {
				if (run)
					e.printStackTrace();
				break;
			}
			File[] fs = dlDir.listFiles();
			for (byte i = 0; i < fs.length; i++) {
				String s = fs[i].getName(), ext = s.substring(s.lastIndexOf('.'));
				if (props.containsKey(ext)) {
					String act = props.getProperty(ext);
					switch (act.charAt(0)) {
					case A_ASK:
						moveFile(fs[i], getSaveLoc(s, JFileChooser.FILES_ONLY));
						break;
					case A_IGNORE:
						break;
					case A_MOVE:
						moveFile(fs[i], new File(act.substring(1)));
						break;
					case A_RUN:
						File tmp = new File(F_TMP, System.currentTimeMillis() + fs[i].getName());
						moveFile(fs[i], tmp);
						Desktop.getDesktop().open(tmp);
					}
				} else {
					String resp = ac.getResponseFor(ext);
					if (!resp.equals(String.valueOf(ActionChooser.NO_RESPONSE))) {
						props.put(ext, resp);
						i--;
					}
				}
			}
		}
		ac.dispose();
	}

	private static void moveFile(File src, File dest) throws FileNotFoundException, IOException {
		Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	static File getSaveLoc(String item, int mode) {
		jfc.setDialogTitle("Choose location for " + item);
		jfc.setFileSelectionMode(mode);
		jfc.showSaveDialog(null);
		return jfc.getSelectedFile();
	}

	public static void main(String[] args) throws IOException, AWTException, InterruptedException {
		new Director().start();
	}
}
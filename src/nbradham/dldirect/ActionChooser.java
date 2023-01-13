package nbradham.dldirect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

final class ActionChooser {

	static final char NO_RESPONSE = '\0';

	private final JFrame frame = new JFrame("Choose Action for File Type");
	private final JLabel prompt = new JLabel();
	private final Object lock = new Object();
	private final ActionListener listener = e -> {
		frame.setVisible(false);
		sendResponse(e.getActionCommand().charAt(0));
	};
	private final GridBagConstraints gbc = new GridBagConstraints();

	private char response;

	ActionChooser() {
		SwingUtilities.invokeLater(() -> {
			frame.setLayout(new GridBagLayout());
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					sendResponse(NO_RESPONSE);
				}
			});

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 4;
			frame.add(prompt, gbc);

			gbc.gridwidth = 1;
			gbc.gridy = 1;
			addButton("Ask Save Location", Director.A_ASK, 0);
			addButton("Ignore", Director.A_IGNORE, 1);
			addButton("Move", Director.A_MOVE, 2);
			addButton("Open", Director.A_RUN, 3);
			frame.getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));
			frame.pack();
		});
	}

	private void addButton(String text, char response, int x) {
		JButton button = new JButton(text);
		button.setActionCommand(String.valueOf(response));
		button.addActionListener(listener);
		gbc.gridx = x;
		frame.add(button, gbc);
	}

	private void sendResponse(char resp) {
		response = resp;
		synchronized (lock) {
			lock.notify();
		}
	}

	final String getResponseFor(String type) throws InterruptedException {
		SwingUtilities.invokeLater(() -> {
			prompt.setText("What would you like to do with \"" + type + "\" files?");
			frame.setVisible(true);
			frame.toFront();
			frame.requestFocus();
		});
		synchronized (lock) {
			lock.wait();
		}
		File dir = null;
		if (response == Director.A_MOVE)
			dir = Director.getSaveLoc(type, JFileChooser.DIRECTORIES_ONLY);

		return String.valueOf(response) + (dir == null ? "" : dir.getAbsolutePath());
	}

	final void dispose() {
		SwingUtilities.invokeLater(() -> frame.dispose());
	}
}
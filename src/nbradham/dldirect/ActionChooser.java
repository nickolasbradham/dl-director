package nbradham.dldirect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

final class ActionChooser {

	private final JFrame frame = new JFrame("Choose Action for File Type");
	private final JLabel prompt = new JLabel();
	private final ActionListener listener = e -> {
		response = e.getActionCommand().charAt(0);
		notify();
	};
	private final GridBagConstraints gbc = new GridBagConstraints();

	private char response;

	ActionChooser() {
		SwingUtilities.invokeLater(() -> {
			frame.setLayout(new GridBagLayout());

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
			frame.pack();
		});
	}

	private final void addButton(String text, char response, int x) {
		JButton button = new JButton(text);
		button.setActionCommand(String.valueOf(response));
		button.addActionListener(listener);
		gbc.gridx = x;
		frame.add(button, gbc);
	}

	final String getResponseFor(String type) throws InterruptedException {
		SwingUtilities.invokeLater(() -> {
			prompt.setText("What would you like to do with \"" + type + "\" files?");
			frame.setVisible(true);
		});
		wait();
		//TODO Handle move.
		return String.valueOf(response);
	}
}
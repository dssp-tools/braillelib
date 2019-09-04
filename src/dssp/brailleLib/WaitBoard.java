package dssp.brailleLib;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

import java.awt.Color;

import javax.swing.JPanel;

public class WaitBoard extends JDialog
{
	private JLabel icon;
	private JLabel title;
	private JLabel message;

	/**
	 * Create the dialog.
	 */
	public WaitBoard()
	{
		getContentPane().setBackground(Color.WHITE);
		setType(Type.POPUP);
		setBounds(100, 100, 264, 139);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 20, 5, 20));
		panel.setBackground(Color.WHITE);
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		icon = new JLabel("");
		panel.add(icon, BorderLayout.WEST);
		icon.setHorizontalAlignment(SwingConstants.CENTER);

		title = new JLabel("");
		title.setFont(new Font("MS UI Gothic", Font.PLAIN, 20));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(title, BorderLayout.NORTH);
		
		message = new JLabel("New label");
		message.setFont(new Font("MS UI Gothic", Font.PLAIN, 14));
		message.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(message, BorderLayout.CENTER);

		this.setUndecorated(true);
	}

	public void setMessage(String title, Icon image, String text)
	{
		if (null != title && false == title.isEmpty())
		{
			this.title.setText(title);
		}
		if (null != icon)
		{
			this.icon.setIcon(image);
		}
		this.message.setText(text);
	}
}

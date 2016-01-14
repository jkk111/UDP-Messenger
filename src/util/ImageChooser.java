package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import GUI.GUI;

public class ImageChooser implements ActionListener  {
	GUI gui;
	Logger l;
	public ImageChooser(GUI g) {
		l = new Logger(); 
		gui = g;
	}
	@Override
	public void actionPerformed(ActionEvent e) {JFileChooser c = new JFileChooser();
		int returnValue = c.showOpenDialog(null);
		if(returnValue == c.APPROVE_OPTION) {
			//callback
			l.out("returning: " + c.getSelectedFile());
			gui.sendImage(c.getSelectedFile().toString());
		} else {
			l.out("no file selected");
		}
	}
	
	public static void main(String[] args) {
		new ImageChooser(null);
		
	}
}

package gui;

/*
 * Based heavily on oracle textdemo.java
 * https://docs.oracle.com/javase/tutorial/uiswing/components/textfield.html
 */
 
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

import util.Logger;
 
public class UI extends JPanel implements ActionListener {
    protected JTextField textField;
    protected JTextArea textArea;
    public Logger log;
 
    public UI() {
        super(new GridBagLayout());
        try {
			log = new Logger();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        textField = new JTextField(20);
        textField.addActionListener(this);
 
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
 
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        System.out.println(c.weightx);
 
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
        
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(textField, c);
    }
 
    public void actionPerformed(ActionEvent evt) {
        String text = textField.getText();
        if(text.equals(""))
        	return;
        textField.setText("");
        try {
			log.in(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        textArea.append(text + "\n");
        
        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.addWindowListener(closeHandler());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add contents to the window.
        frame.add(new UI());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static WindowListener closeHandler() {
    	WindowListener l = new WindowAdapter() {
    		@Override
    		public void windowClosing(WindowEvent e) {
    			UI ui = (UI)((JFrame) e.getSource()).getContentPane().getComponents()[0];
    			try {
					ui.log.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    		}
    	};
    	return l;
    }

	public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        System.out.println("hello world");
    }
}
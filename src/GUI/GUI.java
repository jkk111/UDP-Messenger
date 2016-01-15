package GUI;

/*
 * Based heavily on oracle textdemo.java
 * https://docs.oracle.com/javase/tutorial/uiswing/components/textfield.html
 */
 
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.*;

import interfaces.ImageSelect;
import interfaces.MessageReceived;
import interfaces.MessageSend;
import util.ClientNode;
import util.ImageChooser;
import util.Logger;
 
public class GUI extends JPanel implements ActionListener, MessageReceived, ImageSelect {
    protected JTextField textField;
    protected JTextArea textArea;
    DefaultListModel model;
    JList list;
    public Logger log;
    MessageSend host;
    ArrayList<ClientNode> clients;
    int counter = 15;
    public GUI(MessageSend host) {
        super(new GridBagLayout());
    	clients = new ArrayList<ClientNode>();
		log = new Logger();
		this.host = host;
        textField = new JTextField(20);
        textField.addActionListener(this);
        model = new DefaultListModel();
        list = new JList(model);
        list.getSelectedIndex();
        JScrollPane pane = new JScrollPane(list);
        TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {
				ArrayList<ClientNode> tmpClients = host.getClients();
				ArrayList<ClientNode> newClients = new ArrayList<ClientNode>();
				for(int i = 0; i < tmpClients.size(); i++) {
					boolean isUnique = true;
					for(int j = 0 ; j < clients.size(); j++) {
						if(tmpClients.get(i).id.equals(clients.get(j).id)) {
							isUnique = false;
						}
					}
					if(isUnique) {
						newClients.add(tmpClients.get(i));
					}
				}
				for(int i = 0 ; i < newClients.size(); i++) {
					model.addElement(newClients.get(i).id);
				}
				clients.addAll(newClients);
			}
		};
		java.util.Timer timer = new java.util.Timer(true);
		timer.scheduleAtFixedRate(timerTask, 0, 5000);
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
 
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        JFrame frame = new JFrame("TextDemo");
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.7;
        c.gridwidth = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(scrollPane, c);
        c.gridx = 2;
        c.weightx = 0.3;
        c.gridwidth = 1;
        add(pane, c);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0; 
        add(textField, c);
        JButton button = new JButton("Image");
        button.addActionListener(new ImageChooser(this));
        c.gridx = 2;
        c.gridwidth = 1;
        add(button, c);
        
        frame.addWindowListener(closeHandler());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }
    
    public void receivedMessage(String message, String sender) {
    	log.out("Adding message " + message +" to UI");
    	textArea.append(sender + ": " + message +"\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
    public void addImage(byte[] image) {
    	InputStream in = new ByteArrayInputStream(image);
    	try {
			BufferedImage bImage = ImageIO.read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
        JLabel label = new JLabel(new ImageIcon(image));
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(label);
        f.pack();
        f.setLocation(200,200);
        f.setVisible(true);
    }
 
    public void actionPerformed(ActionEvent evt) {
    	if(list.getSelectedIndex() == -1) {
    		JOptionPane.showMessageDialog(null, "Select a client from the list on the right\nbefore sending a message!");
    		return;
    	}
        String text = textField.getText();
        if(text.equals(""))
        	return;
        if(host != null) {
        	ClientNode client = clients.get(list.getSelectedIndex());
        	host.sendMessage(textField.getText(), (InetSocketAddress) client.address, client.id);
        }
        textField.setText("");
		log.in(text);
        textArea.append("YOU: " + text + "\n");
        
        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
    public void sendImage(String path) {
    	byte[] image = null;
    	String ext = path.substring(path.lastIndexOf(".") + 1);
		try {
			BufferedImage originalImage = ImageIO.read(new File(path));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if(originalImage == null)
				return;
			ImageIO.write(originalImage, ext, baos);
			baos.flush();
			image = baos.toByteArray();
			baos.close();
		} catch (IOException e) {}
		log.out("sending image to client to send");
		host.sendImage(image, clients.get(list.getSelectedIndex()).id);
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
        frame.add(new GUI(null));
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static WindowListener closeHandler() {
    	WindowListener l = new WindowAdapter() {
    		@Override
    		public void windowClosing(WindowEvent e) {
    			GUI ui = (GUI)((JFrame) e.getSource()).getContentPane().getComponents()[0];
				ui.log.close();
    		}
    	};
    	return l;
    }

	public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
		GUI gui = new GUI(null);
		byte[] testImage = null;
		String imagePath = "kawaii.jpg";
		try {
			BufferedImage originalImage = ImageIO.read(new File(imagePath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
			testImage = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gui.addImage(testImage);
		gui.addImage(testImage);
		gui.addImage(testImage);
    }
}
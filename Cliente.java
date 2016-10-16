import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JTextArea;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gabriel
 */
public class Cliente implements Runnable{
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;       
    private Thread t;
    
    private JTextArea userList;
    private JTextArea messages;
    private static String PROTOCOL_USER_PREFIX = "$:->usuario";
    private static String PROTOCOL_PUBLIC_MESSAGE_PREFIX = "$:->mensagem";
    private static String PROTOCOL_PRIVATE_MESSAGE_PREFIX = "$:->privado";

    
    private static String PROTOCOL_GET_USER_LIST = "/lista";
    
    public Cliente(JTextArea userList, JTextArea messages){
    	this.userList = userList;
    	this.messages = messages;
    }
    
    public void conectar(String host, int porta){
        try {
            s = new Socket(host, porta);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
            t = new Thread(this);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void enviar(String msg){
    	if(msg.equals(PROTOCOL_GET_USER_LIST)){
    		userList.setText("");
    	}
        out.println(msg);
        out.flush();
    }
    
    public String receber(){
        try {
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {        
        while(true){
        	String recebido = receber();
        	//System.out.println(recebido + "foi");
        	
        	if(isUserInfo(recebido)){
        		appendUser(recebido);
        	}else if(isPublicMesage(recebido)){
        		handleRecivedPublicMesage(recebido);
        	}else if(isPrivateMesage(recebido)){
        		handleRecivedPrivateMesage(recebido);
        	}else {
        		messages.append(recebido + "\n");
        	}
        }
    }
    
    private void handleRecivedPublicMesage(String message){
    	String[] splitedMesage = message.split(" ", 3);
    	if(splitedMesage.length == 3){
    		messages.append(splitedMesage[1] + ": " + splitedMesage[2] + "\n");
    	}
    }
    
    private void handleRecivedPrivateMesage(String message){
    	String[] splitedMesage = message.split(" ", 3);
    	if(splitedMesage.length == 3){
    		messages.append("Privado: " + splitedMesage[1] + ": " + splitedMesage[2] + "\n");
    	}
    }
    
    /**
     * @param message
     * @return
     */
    private boolean isUserInfo(String message){
    	return message != null && message.startsWith(PROTOCOL_USER_PREFIX);
    }
    
    private boolean isPublicMesage(String message){
    	return message != null && message.startsWith(PROTOCOL_PUBLIC_MESSAGE_PREFIX);
    }
    
    private boolean isPrivateMesage(String message){
    	return message != null && message.startsWith(PROTOCOL_PRIVATE_MESSAGE_PREFIX);
    }
    
    /**
     * @param user
     */
    private void appendUser(String user){
    	user = user.substring(user.indexOf(" ") + 1);
    	
    	if(user != null && !user.isEmpty()){
        	String currentText = userList.getText();
        	if(currentText != null && !currentText.isEmpty()){
        		currentText += "\n" + user;
        	}else {
        		currentText = user;
        	}
    		userList.setText(currentText);
    	}
    }
}
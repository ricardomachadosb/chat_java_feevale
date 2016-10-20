import java.io.BufferedReader;
import java.io.IOException;
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
    private static String PROTOCOL_LOGOUT_ACTION = "/sair";
    private static String PROTOCOL_USER_NOT_FOUND_NOTIFICATION_PREFIX = "$:->status";
    private static String PROTOCOL_LOGOUT_NOTIFICATION_PREFIX = "$:->saiu";
    private static String PROTOCOL_PING_RESPONSE_PREFIX = "$:->pong";
    private static String PROTOCOL_LOGIN_NOTIFICATION_PREFIX = "$:->entrou";

    
	boolean shouldRead = true;

    
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
        
        if(isPrivateMesage(msg)){
        	handlePrivateMesageSend(msg);
        }
    	
    	if(msg.equals(PROTOCOL_LOGOUT_ACTION)){
    		handleLogoutAction();
    	}
    }
    
    public String receber() throws IOException{
        return in.readLine();
    }

    @Override
    public void run() {
        while(shouldRead){
        	try {
	        	String recebido = receber();
	        	//System.out.println(recebido + "foi");
	        	
	        	if(recebido == null){
	        		return;
	        	}
	        	
	        	if(isUserInfo(recebido)){
	        		appendUser(recebido);
	        	}else if(isPublicMesage(recebido)){
	        		handleRecivedPublicMesage(recebido);
	        	}else if(isPrivateMesage(recebido)){
	        		handleRecivedPrivateMesage(recebido);
	        	}else if(isUserLogouNotification(recebido)){
	        		handleLogoutNotification(recebido);
	        	}else if(isUserNotFoundNotification(recebido)){
	        		handleUserNotFoundNotification(recebido);
	        	}else if(isPingResponse(recebido)){
	        		handlePingResponse();
	        	}else if(isLoginNotification(recebido)){
	        		handleLoginNotification(recebido);
	        	}else {
	        		messages.append(recebido + "\n");
	        	}
        	}catch(Exception e){
        		e.printStackTrace();
        		shouldRead = false;
        		try{
        			s.close();
        		}catch(Exception ee){}
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
    
    private void handlePrivateMesageSend(String message){
    	String[] splitedMesage = message.split(" ", 3);
    	if(splitedMesage.length == 3){
    		messages.append("Mensagem privada para " + splitedMesage[1] + ": " + splitedMesage[2] + "\n");
    	}
    }
    
    private void handleLogoutNotification(String message){
    	String[] splitedMesage = message.split(" ", 2);
    	messages.append("Usuario Desconectado: " + splitedMesage[1] + "\n");
    }
    
    private void handleLoginNotification(String message){
    	String[] splitedMesage = message.split(" ", 2);
    	messages.append(splitedMesage[1] + "Entrou no chat" + "\n");
    }
    
    private void handleLogoutAction(){
    	messages.append("Você foi desconectado");
		shouldRead = false;
		try {
			s.close();
		} catch (IOException e) {}
    }
    
    private void handlePingResponse(){
    	messages.append("Servidor: Pong" + "\n");
    }
    
    private void handleUserNotFoundNotification(String message){
    	String[] splitedMesage = message.split(" ", 2);
    	messages.append(splitedMesage[1] + "\n");
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
    
    private boolean isUserLogouNotification(String message){
    	return message != null && message.startsWith(PROTOCOL_LOGOUT_NOTIFICATION_PREFIX);
    }
    
    private boolean isUserNotFoundNotification(String message){
    	return message != null && message.startsWith(PROTOCOL_USER_NOT_FOUND_NOTIFICATION_PREFIX);
    }
    
    private boolean isPingResponse(String message){
    	return message != null && message.startsWith(PROTOCOL_PING_RESPONSE_PREFIX);
    }
    
    private boolean isLoginNotification(String message){
    	return message != null && message.startsWith(PROTOCOL_LOGIN_NOTIFICATION_PREFIX);
    }
    
    /**
     * @param user
     */
    private void appendUser(String user){
    	if(user.contains(" ")){
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
    
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gabriel
 */
public class ClienteConectado implements Runnable{
    Socket s;
    PrintWriter out;
    BufferedReader in;
    Thread t;
    String name;
    
    private static String PROTOCOL_GET_USER_LIST = "/lista";
    private static String PROTOCOL_PUBLIC_MESSAGE = "/mensagem";
    private static String PROTOCOL_PRIVATE_MESSAGE = "/privado";
    private static String PROTOCOL_LOGOUT_ACTION = "/sair";
    private static String PROTOCOL_PING_ACTION = "/ping";


    
    private static String PROTOCOL_SEND_PUBLIC_MESSAGE_PREFIX = "$:->mensagem";
    private static String PROTOCOL_SEND_PRIVATE_MESSAGE_PREFIX = "$:->privado";
    private static String PROTOCOL_SEND_USER_NOT_FOUND_NOTIFICATION_PREFIX = "$:->status";
    private static String PROTOCOL_SEND_LOGOUT_NOTIFICATION_PREFIX = "$:->saiu";
    private static String PROTOCOL_SEND_PONG_PREFIX = "$:->pong";
    private static String PROTOCOL_SEND_LOGIN_NOTIFICATION_PREFIX = "$:->entrou";




    public ClienteConectado(Socket s, String name) {
        this.s = s;
        this.name = name;
        setup();
        start();
    }
    
    public void enviarMensagem(String msg){
        out.println(msg);
        out.flush();
    }
    
    public void receberMensagem() throws IOException{
        String msg = in.readLine();
        
        if(msg.equals(PROTOCOL_GET_USER_LIST)){
        	Servidor.enviaUsuariosConectados(out);
        }else if(msg.startsWith(PROTOCOL_PUBLIC_MESSAGE)){
        	sendPublicMessage(msg.split(" ", 2)[1]);
        }else if(msg.startsWith(PROTOCOL_PRIVATE_MESSAGE)){
        	sendPrivateMesage(msg);
        }else if(msg.equals(PROTOCOL_LOGOUT_ACTION)){
        	logoutUser();
        }else if(msg.equals(PROTOCOL_PING_ACTION)){
        	handlePingAction();
        }
    }
    
    private void sendPublicMessage(String msg){
    	msg = PROTOCOL_SEND_PUBLIC_MESSAGE_PREFIX + " " + name + " " + msg;
        for(ClienteConectado c:Servidor.clientMaps.values()){
            c.enviarMensagem(msg);
        }
    }
    
    private void sendPrivateMesage(String msg){
    	String[] splitedMessage = msg.split(" ", 3);
    	if(splitedMessage.length < 3){
    		enviarMensagem("Formato de mensagem privada invalido");
    	}else {
    		ClienteConectado destino = Servidor.clientMaps.get(splitedMessage[1]);
    		if(destino == null){
    			enviarMensagem(PROTOCOL_SEND_USER_NOT_FOUND_NOTIFICATION_PREFIX + " " + "Usuario Não conectado");
    		}else {
    			destino.enviarMensagem(PROTOCOL_SEND_PRIVATE_MESSAGE_PREFIX + " " + name + " " + splitedMessage[2]);
    		}
    	}
    }
    
    private void logoutUser(){
    	try {
			s.close();
			Servidor.clientMaps.remove(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        for(ClienteConectado c:Servidor.clientMaps.values()){
            c.enviarMensagem(PROTOCOL_SEND_LOGOUT_NOTIFICATION_PREFIX + " " + name);
        }
    }
    
    private void handlePingAction(){
    	enviarMensagem(PROTOCOL_SEND_PONG_PREFIX);
    }
    
    private void setup(){
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void start(){
        t = new Thread(this);
        t.start();
    }
    
    public void sendLoginNotificationForAll(){
    	String msg = PROTOCOL_SEND_LOGIN_NOTIFICATION_PREFIX + " " + name + " ";
        for(ClienteConectado c:Servidor.clientMaps.values()){
            c.enviarMensagem(msg);
        }
    }

    @Override
    public void run() {
    	Boolean shouldRead = true;
        while(shouldRead){
        	try{
        		receberMensagem();
        	}catch(Exception e){
        		shouldRead = false;
        		System.out.println();
        	}
        }
    }
}

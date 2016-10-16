
import java.io.BufferedReader;
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
    
    private static String PROTOCOL_SEND_PUBLIC_MESSAGE_PREFIX = "$:->mensagem";
    private static String PROTOCOL_SEND_PRIVATE_MESSAGE_PREFIX = "$:->privado";



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
    
    public String receberMensagem(){
        String msg;
        try {
            msg = in.readLine();
            
            if(msg.equals(PROTOCOL_GET_USER_LIST)){
            	Servidor.enviaUsuariosConectados(out);
            }else if(msg.startsWith(PROTOCOL_PUBLIC_MESSAGE)){
            	sendPublicMessage(msg.split(" ", 2)[1]);
            }else if(msg.startsWith(PROTOCOL_PRIVATE_MESSAGE)){
            	sendPrivateMesage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return null;
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
    			enviarMensagem("Usuario Não conectado");
    		}else {
    			destino.enviarMensagem(PROTOCOL_SEND_PRIVATE_MESSAGE_PREFIX + " " + name + " " + splitedMessage[2]);
    		}
    	}
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

    @Override
    public void run() {
        while(true){
            receberMensagem();
        }
    }
}

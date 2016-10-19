
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gabriel
 */
public class Servidor {
    public static Map<String, ClienteConectado> clientMaps;
    private int port;
    ServerSocket ss;
    private static String PROTOCOL_USER_PREFIX = "$:->usuario";
    
    public static void enviaUsuariosConectados(PrintWriter out){
		StringBuilder builder = new StringBuilder();
		
		for(Map.Entry<String, ClienteConectado> entry: clientMaps.entrySet()){
			builder.append(PROTOCOL_USER_PREFIX + " " + entry.getKey() + "\n");
		}
		out.println(builder.toString());
		out.flush();
	}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Servidor server = new Servidor(8088);
        server.aguardarClientes();
    }

    public Servidor(int port) {
        this.port = port;
        setup();
    }

    private void setup() {
        try {
            ss = new ServerSocket(port);
            clientMaps = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aguardarClientes() {
        try {
            while (true) {
            	Socket s = ss.accept();
                new Thread(new FirstConnection(s)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private boolean isValidName(String name){
    	ClienteConectado cc = clientMaps.get(name);
    	return cc == null;
    }
    
    private class FirstConnection implements Runnable{
    	Socket socket;
    	
    	public FirstConnection(Socket socket) {
			this.socket = socket;
		}
    	
    	
		@Override
		public void run() {
	        try {
	        	Boolean validName = false;
	        	ClienteConectado cli;
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				String name = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				out.println("Digite seu nickname");
				out.flush();
				
				while(!validName){
					name = in.readLine();
					if(isValidName(name)){
						validName = true;
			        	cli = new ClienteConectado(socket, name);
						clientMaps.put(name, cli);
						enviaUsuariosConectados(out);
					}else {
						out.println("Nickname invalido, escolha outro");
						out.flush();
					}
				}
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
    }
}

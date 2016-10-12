
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
            msg = name + ": " + msg;
            for(ClienteConectado c:Servidor.clientes){
                c.enviarMensagem(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return null;
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

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
            System.out.println(receber());
        }
    }
    
    
    public static void main(String args[]){
        Scanner in = new Scanner(System.in);
        Cliente c = new Cliente(null, null);
        //System.out.print("Host: ");
        String host = "localhost";//in.nextLine();
        //System.out.print("Porta: ");
        int porta = 8088;//in.nextInt();
        //in.nextLine();
        c.conectar(host, porta);
        System.out.print("Digite seu Nome: ");
        c.enviar(in.nextLine());
        while(true){
            c.enviar(in.nextLine());
        }
        
    }
}
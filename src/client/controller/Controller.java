/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import client.net.NonBlockingClient;
import client.view.UserInterface;


/**
 *
 * @author Sarah
 */
public class Controller {
    private final UserInterface ui; 
    private NonBlockingClient n;

    
    public Controller (NonBlockingClient n, BlockingQueue<String> receivedFromUser) {
        this.n = n;
        ui = new UserInterface (receivedFromUser, this) ;
        ui.start () ;    
    }
    
    public boolean timeToSend() {
        SelectionKey key = n.socketChannel.keyFor(n.selector);
        if(key.isValid()) {
            key.interestOps(SelectionKey.OP_WRITE);
            n.selector.wakeup();
            return true ;
        }
        return false;
    }
    
    public boolean getTimeToWrite (){
        return ui.getTimeToWrite();
    }
    
    public void setTimeToWrite (boolean b){
        ui.setTimeToWrite(b);
    }
    
    public void msgRcvd (ByteBuffer m) throws IOException{
        String msgFromServer = extractMessageFromBuffer(m);
        String [] msg = msgFromServer.split("##");
        int length = 0 ;
        for (String msg1 : msg) {
            try {
                length = Integer.parseInt(msg1); 
            } catch (NumberFormatException e) {
                if (length != msg1.length()) {
                    throw new IOException("Message Received in Wrong Format");
                }
                ui.printToScreen(msg1);
            }
        }
    }
    
    

    
    private String extractMessageFromBuffer(ByteBuffer msgFromServer) {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        String s = new String(bytes);
        return s;
        
    }
    
    public static String prependLengthHeader(String msgWithoutHeader) {
        return Integer.toString(msgWithoutHeader.length())+ "##" +
                msgWithoutHeader + "##" ;
    }
    
}

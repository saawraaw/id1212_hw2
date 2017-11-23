/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import server.model.Game;

/**
 *
 * @author Sarah
 */
public class Controller {
        private final Game game = new Game();
    private boolean stopPlaying = false ; 
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    
 
    public Controller() {
        initialize();
        sendDataToClient(game.getState());
    }
    
    private void initialize () {
        game.newGame();
        sendDataToClient("Starting a New Game...\n");   
    }
    
    public void handleMsg(ByteBuffer msgFromClient) throws IOException{
        String in = extractMessageFromBuffer(msgFromClient);
        //System.out.println("String With Header: " + in);
        String [] msg = in.split("##");
        int length = 0 ;
        for (int i=0; i< msg.length ; i++){
            try {
                length = Integer.parseInt(msg[i]);
                //System.out.println("Sent Length: " + Integer.parseInt(msg[i]));
            } catch (NumberFormatException e) {
                //System.out.println("Real Length: " + msg[i].length());
                if (length != msg[i].length())
                    throw new IOException ("Msg Sent In Wrong Format");
                String input = msg[i].toLowerCase() ;
                interpreter (input);
                if(!game.getWaitForNewGame() && !stopPlaying) 
                    sendDataToClient(game.getState());   
            }
       }
    }
    
    
    
    private synchronized String extractMessageFromBuffer(ByteBuffer msgFromClient) {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        String input = new String (bytes);
        return input ;
    }
       
    
    private void interpreter (String s) {
        if (s.equals("quit game")) {
                stopPlaying = true ;
        } else if (game.getGameFinished()) {
            if (s.equals("play")) {
                initialize();
            } else {
                sendDataToClient("Invalid Command\n");
                sendDataToClient("Enter \"Play\" To Play Again. "
                        + "Otherwise, Enter \"Quit Game\" To Exit.\n" );
            }
        } else {
            game.play(s);
        }
    }
    
    private void sendDataToClient (String s)  {
        String str = prependLengthHeader(s);
        messagesToSend.add(ByteBuffer.wrap(str.getBytes()));
    }
    
    public boolean stop (){
        return stopPlaying ;
    }
    
    public static String prependLengthHeader(String msgWithoutHeader) {
        return Integer.toString(msgWithoutHeader.length())+ "##" +
                msgWithoutHeader + "##" ;
        
    }
    
    public Queue<ByteBuffer> getMsgsToSend (){
        return messagesToSend ;
    }
    
}

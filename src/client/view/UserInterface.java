/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import client.controller.Controller;

/**
 *
 * @author Sarah
 */
public class UserInterface {
   private final BlockingQueue<String> ReceivedFromUser ;
    private final Scanner scanner = new Scanner(System.in);
    private boolean timeToWrite = false ;
    Controller ctrl ;
    
    public UserInterface (BlockingQueue ReceivedFromUser, Controller ctrl) {
        this.ReceivedFromUser = ReceivedFromUser ;
        this.ctrl = ctrl ;
        
    }
    
    public void start () {
        new Thread (new ScreenReader()).start() ;
    }
    
    public void printToScreen (String s) {
        System.out.print(s);   
        
    }

    public boolean getTimeToWrite(){
        return timeToWrite ;
    }
    
    public void setTimeToWrite(boolean b){
        timeToWrite = b ;
    }
    

    
    private class ScreenReader implements Runnable {
        @Override
        public void run () {
            try {
                while (true) {
                    String s = scanner.nextLine() ;
                    ReceivedFromUser.put(Controller.prependLengthHeader(s));
                    timeToWrite = true ;
                    if(!ctrl.timeToSend())
                        break ;
                    if (s.toLowerCase().equals("quit game"))
                        break ;
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage()); ;
            }

        }
    } 
}

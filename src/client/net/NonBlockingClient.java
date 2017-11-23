/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import client.controller.Controller;

/**
 *
 * @author Sarah
 */
public class NonBlockingClient {
    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(8192);
    private final BlockingQueue<String> messagesToSend = new LinkedBlockingQueue<>();
    private final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 3333);
    public SocketChannel socketChannel;
    public Selector selector;
    private Controller ctrl ; 
    private boolean stop = false ;
    

    
    public NonBlockingClient(){
         try {
            initConnection();
            initSelector();
            ctrl = new Controller(this, messagesToSend);
        } catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
    
    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress); 
    }
    
    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
    
    public void play() {
        try {
            while (!stop) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        recvFromServer();
                    } else if (key.isWritable()) 
                        sendToServer(key);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Press Any Key to Exit...");
        }
        try {
            doDisconnect();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
        
    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }
    
    private void doDisconnect() throws IOException {
        if (socketChannel.isConnected()) {
            System.out.println("Disconneting..");
            socketChannel.close();
            socketChannel.keyFor(selector).cancel();
        }
    }
       
    private void recvFromServer() throws IOException {
        msgFromServer.clear();
        socketChannel.read(msgFromServer);
        ForkJoinPool.commonPool().submit(() -> {
            try {
                ctrl.msgRcvd(msgFromServer);  
            } catch (IOException e){
                System.err.println(e.getMessage());
            }
        });   
    }
    
    private void sendToServer(SelectionKey key) throws IOException {
        synchronized (messagesToSend) {
            while(true) {
                String s = messagesToSend.poll() ; 
                if(s == null)
                    break;
                ByteBuffer msg = ByteBuffer.wrap(s.getBytes());
                socketChannel.write(msg);
                //System.out.println("Msg Sent: " + s);
                if (msg.hasRemaining()) {
                    throw new IOException ("Message Was Not Sent");
                }
                if (s.toLowerCase().equals("9##quit game##")){
                    stop = true ; 
                    //System.out.println("Stop Detected");
                }
            }
            ctrl.setTimeToWrite(false);
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    public static void main(String[] args) {
       NonBlockingClient client = new NonBlockingClient(); 
       client.play();
        
    }
}

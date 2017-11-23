/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.net;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import server.controller.Controller;
/**
 *
 * @author Sarah
 */
public class NonBlockingServer {

private final int portNo = 3333;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;
    
    
    private void serve () {
        try {
            selector = Selector.open();
            initListeningSocketChannel();
            while (true) {
                try {
                    selector.select();
                    for (SelectionKey key : selector.selectedKeys()) {
                        selector.selectedKeys().remove(key);
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            startController(key);
                        } else if (key.isReadable()) {
                            recvFromClient(key);
                        } else if (key.isWritable()) {
                            sendToClient(key);
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                } 
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
        }
        
    }
    
    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    private void startController(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        Controller ctrl = new Controller ();
        clientChannel.register(selector, SelectionKey.OP_WRITE, ctrl);
        
    }
    
    private void sendToClient(SelectionKey key) {
        Controller ctrl = (Controller) key.attachment();
        SocketChannel clientChannel = (SocketChannel) key.channel() ;
        ByteBuffer msg;
            synchronized (ctrl.getMsgsToSend()) {
                try {
                    while ((msg = ctrl.getMsgsToSend().peek()) != null) {
                        clientChannel.write(msg);
                        if (msg.hasRemaining()) {
                            throw new IOException("Sending Failed");
                        }
                        ctrl.getMsgsToSend().remove(); 
                    }
                    key.interestOps(SelectionKey.OP_READ);
                } catch(IOException e) {
                    System.err.println(e.getMessage());
                }
            }
    }
    
    private boolean checkingForDisconnectSignal (SelectionKey key){
        Controller ctrl = (Controller) key.attachment();
        if (ctrl.stop()) {
            try {
                disconnect(key);
            } catch (IOException e){
                System.err.println(e.getMessage());
            }
        }
        return ctrl.stop();
    }
    
    
    private void recvFromClient (SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer msgFromClient = ByteBuffer.allocateDirect(8192);
        try {
            int numOfBytes = clientChannel.read(msgFromClient);
            if (numOfBytes == -1 || numOfBytes ==0)
                return;
        } catch (IOException e){
            key.cancel();
            System.err.println(e.getMessage());
        }
        handleMsg(msgFromClient, key) ; 
    }
    
    
    private void handleMsg(ByteBuffer msgFromClient, SelectionKey key){
        Controller ctrl = (Controller) key.attachment();
        CompletableFuture.runAsync(() -> {
            try {
                ctrl.handleMsg(msgFromClient);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }).thenRun(()-> {
            if(!checkingForDisconnectSignal(key)) {
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }); 
    }
    
    private void disconnect (SelectionKey key) throws IOException{
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.close();
        key.cancel();
        //System.out.println("Client Disconnected");
    }
    
    
    public static void main(String[] args) {
        NonBlockingServer server = new NonBlockingServer();
        server.serve();
    }
    
}

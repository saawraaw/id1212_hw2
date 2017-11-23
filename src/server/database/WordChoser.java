/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Sarah
 */
public class WordChoser {
    List<String> words ;
    public WordChoser (){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("words.txt"));
            String line = reader.readLine();
            words = new ArrayList<>();
            while( line != null ) {
                words.add(line.toLowerCase());
                line = reader.readLine();
            }
            
            
        } catch (FileNotFoundException exception){
            System.out.println("FileNotFound");
        } catch (IOException exception) {
            System.out.println("IOException");
        }
        
    }
    
    public String getChosenWord (){
        Random r = new Random();
        String chosenWord = words.get(r.nextInt(words.size()));
        return chosenWord ; 
    }
}

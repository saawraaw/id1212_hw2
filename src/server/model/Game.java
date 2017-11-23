/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import server.database.WordChoser;

/**
 *
 * @author Sarah
 */
public class Game {
    private int attemptsLeft ; 
    private String currentWord ; 
    private boolean gameFinished ;
    private String chosenWord; 
    public boolean gameWon ; 
    private boolean waitForNewGame = false ; 
    private int totalScore = 0 ;
    private WordChoser wordChoser = new WordChoser ();
    
    
    
    private void changeState (String input){
        //System.out.println("In Change State");
        //System.out.println(input);
        //System.out.println("Input length:" + input.length());
        if (input.length()==1){
            if (!chosenWord.contains(input)){
                //System.out.println("Wrong Guess For Character");
                wrongGuess ();
            } else {
                charGuessCorrect (input.charAt(0));
                
            }
        } else {
            if (chosenWord.equals(input)){
                wordGuessCorrect ();
            } else {
                wrongGuess () ;
            }
        }
            
    }
    
    public void wrongGuess (){
        attemptsLeft --;
        if (attemptsLeft ==0){
            gameFinished = true ;
            totalScore -- ;
        }
    }
    

    private void charGuessCorrect (char c){
        int i = chosenWord.indexOf(c) ;
        String temp = currentWord ;
        if (i==0){
            temp = c + temp.substring(1);
        } else {
            temp = temp.substring(0,i) + c + temp.substring(i+1);
        }
        while (true){
            i = chosenWord.indexOf(c , i+1) ;
            if (i==-1)
                break ; 
            else {
                temp = temp.substring(0,i) + c + temp.substring(i+1);
            }
        }
        currentWord = temp; 
        if (chosenWord.equals(currentWord)){
            wordGuessCorrect ();
        }
    }
    
    private void wordGuessCorrect (){
        currentWord = chosenWord;
        gameFinished  = true ;
        gameWon = true ; 
        totalScore ++ ;
    }
    
    public void play (String input) {
        //System.out.println("the word being played: ");
        //System.out.println(input);
        String inputWord = input.toLowerCase();
        changeState (inputWord);
        
               
    }
                
    public String getChosenWord () {
        return chosenWord ;
    }
    
    public String getState () {
        String s ;
        if (gameFinished) {
            waitForNewGame = true ; 
            if (gameWon) {
                s = insertSpaces (currentWord)  + "\n" ; 
                s += "You Have Won!\n" ;        
            }
            else {
                s = "You Have Lost!\n" ;
                s += "The Correct Answer Was: " + 
                        insertSpaces (chosenWord) + "\n" ;     
            }
            s += "Total Score: " + totalScore + "\n" ;
            s += "Enter \"Play\" To Play Again. Otherwise,"
                    + " Enter \"Quit Game\" To Exit.\n" ;   
        }
        else {
            s = "Total Score: " + totalScore + "\n" ;
            s += "You Have " + attemptsLeft + " Attempts\n";
            s += "Enter \"Quit Game\" To Stop Playing\n" ;
            s += insertSpaces (currentWord) + "\n" ;
        }
        return s ; 
    }
    
    public void newGame () {
        
        chosenWord = wordChoser.getChosenWord() ; 
        System.out.println(chosenWord);
        gameFinished = false ;
        attemptsLeft = chosenWord.length();
        currentWord = new String ();
        for(int i=0 ;i<attemptsLeft ; i++){
            currentWord += "_" ; 
        }
        gameWon = false ;
        waitForNewGame = false ; 
    }
    
    public boolean getGameFinished () {
        return gameFinished ;
    }
    
    public boolean getGameWon () {
        return gameWon ;
    }
    
    public boolean getWaitForNewGame () {
        return waitForNewGame ;
    }
    
    private String insertSpaces (String in) {
        String s = new String () ;
        for (int i=0 ; i<in.length();i++){
                s += in.charAt(i) + " " ;
        }
        return s ;
    }
}

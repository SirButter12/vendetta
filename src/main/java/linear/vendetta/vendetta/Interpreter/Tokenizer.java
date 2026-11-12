/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import linear.vendetta.vendetta.Interpreter.Token.TokenType;

/**
 *
 * @author ian
 */
public class Tokenizer {
    private String command;
    private List<Token> tokens = new ArrayList<>();
    private static final List<String> reservedWords = List.of(
            "adj","inv","trans","det","dot", "cross", "project", "solveSystem", "Vector", "Matrix", "triangulize",
            "sin", "cos", "tan", "sec", "csc", "cot", "ln", "sqrt"
    );
    private static HashMap<Character, TokenType> tokenMapping = new HashMap();
     
    
    static {
        tokenMapping.put(',', TokenType.COMMA);
        tokenMapping.put('=', TokenType.EQUALS);
        tokenMapping.put('(', TokenType.LPAREN);
        tokenMapping.put('-', TokenType.MINUS);
        tokenMapping.put('+', TokenType.PLUS);
        tokenMapping.put(')', TokenType.RPAREN);
        tokenMapping.put('/', TokenType.SLASH);
        tokenMapping.put('*', TokenType.STAR);
        tokenMapping.put('^', TokenType.EXP);
    }
    
    public Tokenizer(String command) {
        this.command = command;
        
        tokenize();
    }
    
    private void tokenize() {
        StringBuilder current = new StringBuilder();
        char[] commandChars = command.toCharArray();
        
        for (int i = 0; i < command.length(); i++) {
            char c = commandChars[i];
            
            if (tokenMapping.containsKey(c)) {
                validate(current.toString());
                tokens.add(new Token(String.valueOf(c), tokenMapping.get(c)));
                clear(current);
                continue;
            }
            
            if (Character.isWhitespace(c)) {
                validate(current.toString());
                clear(current);
                continue;
            }    
            
            current.append(commandChars[i]);
        }
        
        validate(current.toString());
        tokens.add(new Token("", Token.TokenType.END));
    }
    
    private void validate(String str) {
        if (str.isEmpty()) {
            return;
        }
        
        if (reservedWords.contains(str)) {
            tokens.add(new Token(str, Token.TokenType.FUNCT));
        } else if (str.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
            tokens.add(new Token(str, Token.TokenType.NAME));
        } else if (str.matches("^\\d+(\\.\\d+)?$")) {
            tokens.add(new Token(str, Token.TokenType.NUMBER));
        } else {
            throw new CommandException();
        }   
    }
    
    private void clear(StringBuilder str) {
        str.setLength(0);
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
}

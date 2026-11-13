/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

/**
 *
 * @author ian
 */
public class Token {
    public String oriText;
    public TokenType tokenType;
    
    public Token(String oriText, TokenType tokenType) {
        this.oriText = oriText;
        this.tokenType = tokenType;
    }
    
    public static enum TokenType {
        NAME, NUMBER, PLUS, MINUS, STAR, SLASH, EXP, LPAREN, RPAREN, COMMA, EQUALS, FUNCT, CONSTRUCTOR, END
    }
}

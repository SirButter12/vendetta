/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.List;
import linear.vendetta.vendetta.Interpreter.Node.NodeKind;
import linear.vendetta.vendetta.Interpreter.Token.TokenType;

/**
 *
 * @author ian
 */
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {                          
        return tokens.get(pos);
    }

    private Token next() {                          
        Token t = tokens.get(pos);
        if (t.tokenType != TokenType.END) {
            pos++;                                  
        }
        return t;
    }

    private Token expect(TokenType type, String message) {   
        if (peek().tokenType != type) {
            throw new CommandException(message);
        }
        return next();
    }

    public Node parse() {
        Node tree = parseStatement();
        if (peek().tokenType != TokenType.END) {   
            throw new CommandException("Unexpected '" + peek().oriText + "'");
        }
        return tree;
    }
    
    
    private Node parseFactor() {
        Token t = next();

        if (t.tokenType == TokenType.NUMBER) {
            return new Node(NodeKind.NUMBER, t.oriText, List.of());
        }
        if (t.tokenType == TokenType.NAME) {
            return new Node(NodeKind.NAME, t.oriText, List.of());
        }
        if (t.tokenType == TokenType.LPAREN) {
            Node inner = parseExpression();            
            expect(TokenType.RPAREN, "Missing ')'");
            return inner;                              
        }
        
        if (t.tokenType == TokenType.FUNCT) {
            expect(Token.TokenType.LPAREN, "Expected '(' after " + t.oriText);
            ArrayList<Node> arguments = new ArrayList<>();

            if (peek().tokenType != TokenType.RPAREN) {
                arguments.add(parseExpression());
            }

            while (peek().tokenType == TokenType.COMMA) {
                next();
                arguments.add(parseExpression());
            }

            expect(TokenType.RPAREN, "Missing ')'");

            return new Node(NodeKind.CALL, t.oriText, arguments);
        }
        
        if (t.tokenType == TokenType.END) {
            throw new CommandException("Unexpected end of command");
        }    
            
        throw new CommandException("Unexpected '" + t.oriText + "'");    
    }
    
    private Node parseUnary() {
        if (peek().tokenType == TokenType.MINUS) {
            next();                                    
            Node operand = parseUnary();                

            if (operand.kind() == NodeKind.NUMBER) {
                return new Node(NodeKind.NUMBER, String.valueOf(-Double.parseDouble(operand.text())), List.of());
            }
            
            return new Node(NodeKind.BINARY, "*", List.of(new Node(NodeKind.NUMBER, "-1", List.of()), operand));
        }
        
        return parseFactor();
    }
    
    private Node parseExpression() {
        Node left = parseTerm();                        
        while (peek().tokenType == TokenType.PLUS || peek().tokenType == TokenType.MINUS) {
            String op = next().oriText;                 
            Node right = parseTerm();                   
            left = new Node(NodeKind.BINARY, op, List.of(left, right));
        }
        return left;
    }

    private Node parseTerm() {
        Node left = parseExp();                        
        while (peek().tokenType == TokenType.STAR || peek().tokenType == TokenType.SLASH) {
            String op = next().oriText;                 
            Node right = parseExp();                   
            left = new Node(NodeKind.BINARY, op, List.of(left, right));
        }
        return left;
    }
    
    private Node parseExp() {
        Node left = parseUnary();
        while (peek().tokenType == TokenType.EXP) {
            String op = next().oriText;                 
            Node right = parseUnary();                   
            left = new Node(NodeKind.BINARY, op, List.of(left, right));
        }
        
        return left;
    }
    
    private Node parseStatement() {
        if ((pos + 1 < tokens.size()) && tokens.get(pos).tokenType.equals(TokenType.NAME) && tokens.get(pos + 1).tokenType.equals(TokenType.EQUALS)) {
            String name = next().oriText;
            next();
            Node valor = parseExpression();
            
            return new Node(NodeKind.ASSIGN, name, List.of(valor));
        }
        
        return parseExpression();
    }
}

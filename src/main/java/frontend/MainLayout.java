/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frontend;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author adria
 */
public class MainLayout extends BorderPane {
    private IOPanel ioPanel;
    private CartesianPane cartesianPane;
    
    public MainLayout() {
        ioPanel = new IOPanel();
        cartesianPane = new CartesianPane();
        
        HBox centerContainer = new HBox(ioPanel, cartesianPane);
        centerContainer.setStyle("-fx-alignment: center");
        setCenter(centerContainer);
    }
    
    public IOPanel getIOPanel() {
        return ioPanel;
    }
    
    public CartesianPane getCartesianPane() {
        return cartesianPane;
    }
}

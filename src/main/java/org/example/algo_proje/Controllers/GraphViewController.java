package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphViewController {

    @FXML
    private Pane graphPane;

    // ID ile ekrandaki Düğüm (Node) nesnesini eşleştirmek için
    private Map<String, StackPaneWrapper> nodeMap = new HashMap<>();

    public void drawGraph(List<String[]> usersList, List<String> userIDs, int[][] adjacencyMatrix) {
        graphPane.getChildren().clear();
        nodeMap.clear();

        int userCount = userIDs.size();
        if (userCount == 0) return;

        // Dairesel Dizilim Hesabı
        double centerX = graphPane.getPrefWidth() / 2;
        double centerY = graphPane.getPrefHeight() / 2;
        double radius = Math.min(centerX, centerY) - 100;

        // 1. ÖNCE DÜĞÜMLERİ OLUŞTUR
        for (int i = 0; i < userCount; i++) {
            String id = userIDs.get(i);
            String name = findName(usersList, id);

            // Matematiksel açı hesabı
            double angle = 2 * Math.PI * i / userCount;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            StackPaneWrapper node = createNode(id, name, x, y);
            nodeMap.put(id, node);
            graphPane.getChildren().add(node);
        }

        // 2. SONRA ÇİZGİLERİ (İLİŞKİLERİ) ÇİZ
        for (int i = 0; i < userCount; i++) {
            for (int j = i + 1; j < userCount; j++) {
                int score = adjacencyMatrix[i][j];

                if (score > 0) {
                    String id1 = userIDs.get(i);
                    String id2 = userIDs.get(j);

                    StackPaneWrapper n1 = nodeMap.get(id1);
                    StackPaneWrapper n2 = nodeMap.get(id2);

                    Line line = new Line();

                    // Çizgiyi düğümlerin merkezine bağla (hareket edince kopsun istemeyiz)
                    line.startXProperty().bind(n1.layoutXProperty().add(20)); // 20 = yarıçap
                    line.startYProperty().bind(n1.layoutYProperty().add(20));
                    line.endXProperty().bind(n2.layoutXProperty().add(20));
                    line.endYProperty().bind(n2.layoutYProperty().add(20));

                    line.setStrokeWidth(2);

                    // RENK AYARI: 2 Puan (Yakın) = Kırmızı, 1 Puan = Siyah
                    if (score == 2) line.setStroke(Color.RED);
                    else line.setStroke(Color.BLACK);

                    // Çizgiyi en arkaya at ki yazının üstüne gelmesin
                    graphPane.getChildren().add(0, line);
                }
            }
        }
    }

    private StackPaneWrapper createNode(String id, String name, double x, double y) {
        Circle circle = new Circle(20);
        circle.setFill(Color.CORNFLOWERBLUE);
        circle.setStroke(Color.DARKBLUE);

        Text text = new Text(id + "\n" + name);
        text.setFont(new Font("Arial", 10));
        text.setStyle("-fx-font-weight: bold");
        text.setFill(Color.WHITE);

        StackPaneWrapper wrapper = new StackPaneWrapper(circle, text);
        wrapper.setLayoutX(x);
        wrapper.setLayoutY(y);

        makeDraggable(wrapper); // Sürükleme özelliği ekle
        return wrapper;
    }

    // Basit isim bulma
    private String findName(List<String[]> list, String id) {
        for(String[] u : list) if(u[0].equals(id)) return u[1];
        return "Bilinmiyor";
    }

    // Sürükle Bırak Mantığı
    private void makeDraggable(StackPane node) {
        final Delta dragDelta = new Delta();
        node.setOnMousePressed(e -> {
            dragDelta.x = node.getLayoutX() - e.getSceneX();
            dragDelta.y = node.getLayoutY() - e.getSceneY();
        });
        node.setOnMouseDragged(e -> {
            node.setLayoutX(e.getSceneX() + dragDelta.x);
            node.setLayoutY(e.getSceneY() + dragDelta.y);
        });
    }

    private static class StackPaneWrapper extends StackPane {
        public StackPaneWrapper(javafx.scene.Node... children) { super(children); }
    }
    private static class Delta { double x, y; }
}
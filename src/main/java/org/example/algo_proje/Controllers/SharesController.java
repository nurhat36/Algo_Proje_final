package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.algo_proje.Models.Shares;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.ShareService;

import java.io.ByteArrayInputStream;
import java.util.List;

public class SharesController {

    @FXML private TextField txtNewShare;
    @FXML private Button btnAddShare;
    @FXML private VBox shareList;

    private Users loggedUser;

    public void setUser(Users user) {
        this.loggedUser = user;
        loadShares();
    }

    @FXML
    public void initialize() {
        btnAddShare.setOnAction(e -> addShare());
    }

    private void addShare() {
        if (loggedUser == null) return;
        Shares s = new Shares();
        s.setUserId(loggedUser.getUserId());
        s.setTitle(null);
        s.setDescription(txtNewShare.getText());
        s.setImage(false);
        s.setPath(null);

        if (ShareService.addShare(s)) {
            txtNewShare.clear();
            loadShares();
        } else {
            System.out.println("Share eklenemedi");
        }
    }

    private void loadShares() {
        shareList.getChildren().clear();
        List<Shares> list = ShareService.getAllSharesWithLikeCount();

        for (Shares s : list) {
            Label lblUser = new Label("UserId: " + s.getUserId());
            Label lblText = new Label(s.getDescription());
            lblText.setWrapText(true);

            Button btnLike = new Button("Beğen (" + ShareService.getLikeCount(s.getId()) + ")");
            btnLike.setOnAction(ev -> {
                if (loggedUser == null) return;
                boolean liked = ShareService.likeShare(s.getId(), loggedUser.getUserId());
                if (!liked) {
                    // zaten beğenmişse unlike yap
                    ShareService.unlikeShare(s.getId(), loggedUser.getUserId());
                }
                // buton ve sayaç güncelle
                btnLike.setText("Beğen (" + ShareService.getLikeCount(s.getId()) + ")");
            });

            Button btnLikers = new Button("Kim beğenmiş?");
            btnLikers.setOnAction(ev -> {
                List<Users> likers = ShareService.getLikers(s.getId());
                StringBuilder sb = new StringBuilder();
                for (Users u : likers) sb.append(u.getUsername()).append("\n");
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Beğenenler");
                a.setContentText(sb.toString());
                a.showAndWait();
            });

            VBox box = new VBox(5, lblUser, lblText, new HBox(8, btnLike, btnLikers));
            box.setStyle("-fx-padding:8; -fx-background-color:#f2f2f2; -fx-background-radius:6;");
            shareList.getChildren().add(box);
        }
    }
}

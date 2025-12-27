package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.algo_proje.Models.Comments;
import org.example.algo_proje.Models.DTOs.PostDTO;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.CommentService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CommentController {
    @FXML private VBox commentContainer;
    @FXML private TextArea txtComment;

    private PostDTO currentPost;
    private Users loggedUser;
    private final CommentService commentService = new CommentService();
    @FXML private HBox replyIndicatorBox;
    @FXML private Label lblReplyTarget;

    public void initData(PostDTO post, Users user) {
        this.currentPost = post;
        this.loggedUser = user;
        refreshComments();
    }

    private Integer selectedParentId = null; // Sınıf düzeyinde değişken

    @FXML
    private void handleSendComment() {
        String content = txtComment.getText().trim();
        if (content.isEmpty()) return;

        // selectedParentId null ise ana yorum, değilse cevaptır
        boolean success = commentService.addComment(
                currentPost.shareId,
                loggedUser.getUserId(),
                content,
                selectedParentId
        );

        if (success) {
            txtComment.clear();
            txtComment.setPromptText("Yorumunuzu yazın...");
            selectedParentId = null; // Yanıt modundan çık
            refreshComments(); // Ekranı yenile
        }
    }


    private void refreshComments() {
        commentContainer.getChildren().clear();
        List<Comments> allComments = commentService.getCommentsByShareId(currentPost.shareId);

        // 1. Sadece ANA yorumları (parentId == null veya 0) bul
        for (Comments c : allComments) {
            if (c.getParentCommentId() == 0 || c.getParentCommentId() == 0) {

                // Ana yorumu oluştur
                VBox mainCommentUI = createCommentUI(c, 0);
                commentContainer.getChildren().add(mainCommentUI);

                // 2. Bu ana yoruma ait TÜM alt yanıtları (yanıtın yanıtı dahil) topla
                List<Comments> allRepliesOfThisParent = new ArrayList<>();
                findAllRepliesRecursive(c.getId(), allComments, allRepliesOfThisParent);

                if (!allRepliesOfThisParent.isEmpty()) {
                    // Yanıtları tutacak gizli konteyner
                    VBox replyContainer = new VBox(10);
                    replyContainer.setVisible(false);
                    replyContainer.setManaged(false);

                    // Tüm yanıtları sadece 1 tab içeriden (depth=1) ekle
                    for (Comments reply : allRepliesOfThisParent) {
                        replyContainer.getChildren().add(createCommentUI(reply, 1));
                    }

                    // Yanıtları Gör Butonu
                    Hyperlink toggleBtn = new Hyperlink("——— Yanıtları gör (" + allRepliesOfThisParent.size() + ")");
                    toggleBtn.getStyleClass().add("view-replies-link");
                    VBox.setMargin(toggleBtn, new Insets(0, 0, 0, 45)); // Butonu hizala

                    // ÇALIŞAN BUTON MANTIĞI
                    toggleBtn.setOnAction(e -> {
                        boolean isVisible = replyContainer.isVisible();
                        replyContainer.setVisible(!isVisible);
                        replyContainer.setManaged(!isVisible);
                        toggleBtn.setText(isVisible ? "——— Yanıtları gör (" + allRepliesOfThisParent.size() + ")" : "——— Yanıtları gizle");
                    });

                    commentContainer.getChildren().addAll(toggleBtn, replyContainer);
                }
            }
        }
    }

    /**
     * Bir yorumun altındaki tüm seviyelerdeki yanıtları tek bir listeye toplar (Özyinelemeli)
     */
    private void findAllRepliesRecursive(int parentId, List<Comments> allComments, List<Comments> resultList) {
        for (Comments c : allComments) {
            if (c.getParentCommentId() != 0 && c.getParentCommentId() == parentId) {
                resultList.add(c);
                // Bu yanıtın da yanıtları var mı diye bak (Sınırsız derinliği tek listeye döker)
                findAllRepliesRecursive(c.getId(), allComments, resultList);
            }
        }
    }

    private void prepareReply(Comments c) {
        selectedParentId = c.getId();
        lblReplyTarget.setText("@" + c.getAuthorName() + " yanıtlanıyor...");
        replyIndicatorBox.setVisible(true);
        replyIndicatorBox.setManaged(true);
        txtComment.requestFocus();
    }

    // Vazgeç (X) butonu için
    @FXML
    private void cancelReply() {
        selectedParentId = null;
        replyIndicatorBox.setVisible(false);
        replyIndicatorBox.setManaged(false);
        txtComment.setPromptText("Düşüncelerini paylaş...");
    }

    // Yorum UI oluştururken Yanıtla aksiyonunu güncelle
    private VBox createCommentUI(Comments c, int depth) {
        VBox box = new VBox(5);
        box.getStyleClass().add(depth > 0 ? "reply-comment-box" : "single-comment-box");
        if (depth > 0) VBox.setMargin(box, new Insets(0, 0, 0, 45));

        // Yazar ve İçerik Yan Yana (Instagram stili)
        TextFlow textFlow = new TextFlow();
        Text authorText = new Text(c.getAuthorName() + " ");
        authorText.setStyle("-fx-font-weight: bold; -fx-fill: #262626;");

        if (c.getParentAuthorName() != null && depth > 0) {
            Text mentionText = new Text("@" + c.getParentAuthorName() + " ");
            mentionText.setStyle("-fx-fill: #00376B; -fx-font-weight: bold;");
            textFlow.getChildren().add(mentionText);
        }

        Text commentContent = new Text(c.getContent());
        textFlow.getChildren().addAll(authorText, commentContent);

        // Alt bar (Zaman + Yanıtla)
        HBox actionBar = new HBox(15);
        Label lblTime = new Label(formatDate(c.getCreatedAt()));
        lblTime.setStyle("-fx-text-fill: #8E8E8E; -fx-font-size: 11px;");

        Hyperlink btnReply = new Hyperlink("Yanıtla");
        btnReply.getStyleClass().add("reply-link");
        btnReply.setOnAction(e -> prepareReply(c));

        actionBar.getChildren().addAll(lblTime, btnReply);
        box.getChildren().addAll(textFlow, actionBar);
        return box;
    }
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";

        // Format: 14:30 - 27 Ara
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - d MMM", new Locale("tr"));
        return sdf.format(timestamp);
    }
}
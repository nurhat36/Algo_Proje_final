package org.example.algo_proje.Attribute;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class PhotoAttribute {
    public static String saveImageToStaticFolder(File selectedFile, String fileSystemSavePath) {
        if (selectedFile == null) {
            return null;
        }

        try {
            // ... (Dosya Uzantısı Alma ve UUID Oluşturma mantığı aynı kalır) ...
            String fileName = selectedFile.getName();
            String extension = "";
            int lastIndexOfDot = fileName.lastIndexOf('.');
            if (lastIndexOfDot > 0) {
                extension = fileName.substring(lastIndexOfDot);
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 3. Hedef Klasörü Kontrol Et ve Oluştur
            // Gelen yolu kullanıyoruz
            File targetDir = new File(fileSystemSavePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // 4. Dosyayı Hedefe Kopyala
            File targetFile = new File(targetDir, uniqueFileName);

            Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Resim başarıyla kaydedildi: " + targetFile.getAbsolutePath());

            return uniqueFileName;

        } catch (IOException e) {
            System.err.println("Resim kaydetme hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Veritabanında kayıtlı benzersiz dosya adını kullanarak Image nesnesini oluşturur.
     * @param uniqueFileName Veritabanında kayıtlı dosya adı.
     * @param resourceBasePath JavaFX'in ClassLoader'ının görebileceği kaynak yolu.
     * (Örn: "/static/Images/profile_pics/")
     * @param controllerClass Mevcut Controller sınıfı (ClassLoader için gereklidir).
     * @return Yüklü Image nesnesi veya hata durumunda null.
     */
    public static Image loadImageFromResources(String uniqueFileName, String resourceBasePath, Class<?> controllerClass) {
        if (uniqueFileName == null || uniqueFileName.isEmpty()) {
            return null;
        }

        // Gelen kaynak yolu ve dosya adını birleştir
        String resourcePath = resourceBasePath + uniqueFileName;

        try {
            Image image = new Image(controllerClass.getResourceAsStream(resourcePath));

            if (image.isError() || image.getWidth() <= 0) {
                System.err.println("HATA: Kaynak dosya bulunamadı veya yüklenemedi: " + resourcePath);
                return null;
            }
            return image;

        } catch (Exception e) {
            System.err.println("Resim yüklenirken hata oluştu: " + e.getMessage());
            return null;
        }
    }
}

package me.rodrigo.lobby.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Http {
    public static void DownloadFile(String fileUrl, String savePath) throws IOException {
        URL url = new URL(fileUrl);
        InputStream inputStream = url.openStream();
        Path outputPath = Path.of(savePath);

        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
    }
}

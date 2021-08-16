package utils.web;


import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Message;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ImgurUploader {

    private final static String URL = "https://api.imgur.com/3/upload";
    private final static String CLIENT_ID = "400ef7912d9055c";

    public static String[] VIDEO_FORMATS = {"mp4", "webm", "quicktime", "flv", "msvideo", "mpeg", "wmv", "matroska", "mkv"};
    public static String[] IMAGE_FORMATS = {"png", "jpg", "gif", "anigif", "album"};

    public static String uploadMedia(File file) throws IllegalArgumentException, IOException {
        boolean video;
        if (isVideo(file.getName())) {
            video = true;
        } else if (isImage(file.getName())) {
            video = false;
        } else throw new IllegalArgumentException("Unsupported file type!");

        return uploadMedia(file.getName(), new FileInputStream(file), video);
    }

    public static String uploadMedia(String name, InputStream stream, boolean video) throws IllegalArgumentException, IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost(URL);

            uploadFile.addHeader("Authorization", "Client-ID {{" + CLIENT_ID + "}}");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addTextBody("title", name)
                    .addBinaryBody(
                            video ? "video" : "image",
                            stream,
                            ContentType.DEFAULT_BINARY,
                            name
                    );
            uploadFile.setEntity(builder.build());
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();

            JsonElement json = JsonParser.parseString(Utils.convertStreamToString(responseEntity.getContent())).getAsJsonObject().get("data");

            return json.getAsJsonObject().get("link").getAsString();

        }

    }

    public static boolean isVideo(String file) {
        String extension = Files.getFileExtension(file);
        return Arrays.asList(VIDEO_FORMATS).contains(extension.toLowerCase());
    }

    public static boolean isImage(String file) {
        String extension = Files.getFileExtension(file);
        return Arrays.asList(IMAGE_FORMATS).contains(extension.toLowerCase());
    }

    public static boolean isSupportedExtension(String extension) {
        return Arrays.asList(IMAGE_FORMATS).contains(extension.toLowerCase()) || Arrays.asList(VIDEO_FORMATS).contains(extension.toLowerCase());
    }

    public static boolean isValidMedia(String file) {
        return isVideo(file) || isImage(file);
    }

    public static boolean checkAttachmentSize(Message.Attachment attachment) {
        if (isImage(attachment.getFileName() + "." + attachment.getFileExtension())) {
            return attachment.getSize() < 10000000;
        } else if (isVideo(attachment.getFileName() + "." + attachment.getFileExtension())) {
            return attachment.getSize() < 200000000;
        }
        return false;
    }

}

package net.grandtheftmc.discordbot.utils.web;

import lombok.Getter;
import net.grandtheftmc.discordbot.utils.StringUtils;
import org.jfree.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GrandTheftMCUploader {

    /**
     * Uploads a .png file to the GTM web server but deletes on shutdown
     */
    public static UploadedPNG uploadedTempPNG(String name, InputStream stream) throws IllegalArgumentException, IOException {

        UploadedPNG png = uploadedPNG(name, stream);
        File fileDir = new File(png.getInternalDirPath());

        fileDir.deleteOnExit();
        for (File f : fileDir.listFiles()) {
            f.deleteOnExit();
        }

        return png;
    }

    /**
     * Uploads .png file to the gtm web server (but unload temp upload keeps like the file until deleted)
     */
    public static UploadedPNG uploadedPNG(String name, InputStream stream) throws IllegalArgumentException, IOException {

        name = name.replace(" ", "-");

        String secretId = StringUtils.getRandomString(12);
        File fileDir = new File("/home/webserver/sites/grandtheftmc.net/upload/" + secretId);
        fileDir.mkdir();
        File file = new File(fileDir, name + ".png");
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.getInstance().copyStreams(stream, fos);

        return new UploadedPNG(
                secretId,
                file.getAbsolutePath(),
                "https://grandtheftmc.net/upload/" + secretId + "/" + name + ".png"
        );
    }

    public static boolean deletePngFolder(UploadedPNG uploadedPNG) {
        File dir = new File(uploadedPNG.getInternalDirPath());
        boolean success = true;
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                success = false;
            }
        }
        return success && dir.delete();
    }

    @Getter
    public static class UploadedPNG {
        private String code;
        private String internalFilePath;
        private String internalDirPath;
        private String url;
        public UploadedPNG(String code, String internalPath, String url) {
            this.code = code;
            this.internalFilePath = internalPath;
            this.internalDirPath = new File(internalPath).getParentFile().getAbsolutePath();
            this.url = url;
        }
    }

}

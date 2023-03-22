import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.regex.*;

public class Updater {
    private static final Path versionPath = Paths.get("version.txt");
    private static final String releaseURL = "https://github.com/SNBeast/JustModerate/releases/";

    private static void downloadRelease (String tag) throws Exception {
        try (FileOutputStream fos = new FileOutputStream("JustModerate.jar"); InputStream is = new URL(releaseURL + "download/" + tag + "/JustModerate.jar").openStream()) {
            fos.getChannel().transferFrom(Channels.newChannel(is), 0, Long.MAX_VALUE);
        }
        Files.writeString(versionPath, tag);
    }
    private static String getLatestTag () throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(releaseURL + "latest").openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        String latestURL = con.getHeaderField("Location");
        con.disconnect();
        return latestURL.substring(latestURL.lastIndexOf('/') + 1);
    }
    public static void main (String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java Updater token");
            System.exit(0);
        }

        String latestTag = getLatestTag();
        if (!Files.exists(versionPath)) {
            downloadRelease(latestTag);
        }
        else {
            String currentVersion = Files.readString(versionPath);
            String[] currentVersionNumbers = currentVersion.split(Pattern.quote("."));
            String[] latestVersionNumbers = latestTag.split(Pattern.quote("."));
            for (int i = 0; i < 3; i++) {
                if (Integer.parseInt(latestVersionNumbers[i]) > Integer.parseInt(currentVersionNumbers[i])) {
                    downloadRelease(latestTag);
                    break;
                }
            }
        }

        while (true) {
            int retVal = new ProcessBuilder(new String[] {"java", "-jar", "JustModerate.jar", args[0], "--updater-launch"}).inheritIO().start().waitFor();
            if (retVal != 1) {
                System.exit(retVal);
            }

            downloadRelease(getLatestTag());
        }
    }
}

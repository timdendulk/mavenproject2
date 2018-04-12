package Testutil;

import Other.OmgevingConfig;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.chomp;

/**
 * Created by tjitte.bouma on 2-2-2017.
 */
public class FtpHelper {
    private static boolean connected = false;
    private static FTPClient ftpClient;

    private static String ftpHost;
    private static int ftpPort = 21;
    private static String ftpUserName;
    private static String ftpPassword;

    private static String getLatestReplyString() {
        return chomp(ftpClient.getReplyString());
    }

    public static void printFilesInFtpFolder(String ftpFolderLocatie) {
        if (!connected) {
            connectEnLogin();
        }
        gaNaarFolder(ftpFolderLocatie);
        FTPFile[] files = listFiles();
        try {
            System.out.println("Bestanden in huidige folder: " + ftpClient.printWorkingDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (FTPFile file : files) {
            System.out.println(file.toString());
        }

    }

    private static void connectEnLogin() {
        if (!connected) {
            bepaalFtpLocatieEnInlogGegevens();
            ftpClient = new FTPClient();
            try {
                ftpClient.connect(ftpHost, ftpPort);
                boolean loggedIn = ftpClient.login(ftpUserName, ftpPassword);
                if (!loggedIn) {
                    throw new RuntimeException("Het is niet gelukt om op de FTP server '" + ftpHost + ":" + ftpPort + "' in te loggen als '" + ftpUserName + "' met als reden: " + ftpClient.getReplyString());
                }
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Het is niet gelukt om verbinding te krijgen met de FTP server '" + ftpHost + ":" + ftpPort + "\n" + e);
            }
        }
    }

    private static void bepaalFtpLocatieEnInlogGegevens() {
        Properties ftpProp = PropertiesHelper.loadProperties("ftp");
        String omgeving = OmgevingConfig.getOmgeving();
        if (omgeving != null && (omgeving.equals("TB3") || omgeving.equals("TB4"))) {
            ftpHost = ftpProp.getProperty("ftpHost_" + omgeving);
            ftpUserName = ftpProp.getProperty("ftpUserName_" + omgeving);
            ftpPassword = ftpProp.getProperty("ftpPassword_" + omgeving);
        } else {
            ftpHost = ftpProp.getProperty("ftpHost_Ontw");
            ftpUserName = ftpProp.getProperty("ftpUserName_Ontw");
            ftpPassword = ftpProp.getProperty("ftpPassword_Ontw");
        }
    }

    public static void uploadBestand(final String bestandslocatie) throws IOException {
        if (!connected) {
            connectEnLogin();
        }
        String bestandsnaam = BestandHelper.haalBestandsnaamUitBestandslocatie(bestandslocatie);
        InputStream inputStream = BestandHelper.haalBestandAlsInputStream(bestandslocatie);
        System.out.println("Start met uploaden van bestand: " + bestandsnaam);
        ftpClient.storeFile(bestandsnaam, inputStream);
        System.out.println(getLatestReplyString());
    }

    private static FTPFile[] listFiles() {
        if (!connected) {
            connectEnLogin();
        }
        try {
            ftpClient.noop(); // Vanwege willekeurige connection error een extra noop() call naar de server.
            return ftpClient.listFiles();
        } catch (IOException e) {
            throw new RuntimeException("Cannot list", e);
        }
    }

    public static void gaNaarFolder(String folderlocatie) {
        if (!connected) {
            connectEnLogin();
        }
        try {
            ftpClient.changeWorkingDirectory(folderlocatie);
            System.out.println(getLatestReplyString());
        } catch (IOException e) {
            System.out.println(getLatestReplyString());
            throw new RuntimeException("Het is niet gelukt te navigeren naar de gewenste folder. ", e);
        }
    }

    public static void downloadBestand(final String bestand) throws IOException {
        BestandHelper.maakLokaleFolderAlsDezeNietBestaat("testfiles", "ftpOutput");
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(BestandHelper.getScriptPath() + "testfiles" + File.separator + "ftpOutput" + File.separator + WillekeurigeData.willekeurigeGetallen(5) + "_" + bestand);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Start met download van bestand: " + bestand);
        ftpClient.retrieveFile(bestand, outputStream);
        System.out.println(getLatestReplyString());
    }

    public static void maakFtpFolderAan(String newFolder) {
        if (!connected) {
            connectEnLogin();
        }
        try {
            ftpClient.makeDirectory(newFolder);
            System.out.println(getLatestReplyString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot MKD", e);
        }
    }

    private void renameFileOrFolder(String oldname, String newname) {
        try {
            ftpClient.rename(oldname, newname);
            System.out.println(getLatestReplyString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot REN", e);
        }
    }

    private static void deleteFile(String filename) {
        try {
            ftpClient.deleteFile(filename);
            System.out.println(getLatestReplyString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot DELE", e);
        }
    }

    public static void disconnect() {
        try {
            if (connected) {
                ftpClient.logout();
                ftpClient.disconnect();
                connected = false;
            } else {
                System.out.println("FTP disconnect niet nodig, had al geen connectie.");
            }
        } catch (IOException e) {
            System.out.println("Disconnecten van FTP mislukt!");
            e.printStackTrace();
        }
    }
}

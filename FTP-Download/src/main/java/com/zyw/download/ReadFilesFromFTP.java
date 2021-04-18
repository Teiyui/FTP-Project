package com.zyw.download;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ReadFilesFromFTP {

    private static Logger logger = Logger.getLogger(ReadFilesFromFTP.class);

    public FTPClient ftpClient;

    public List<String> arFiles;

    /**
     * Construction Function
     * @param isPrintCommmand
     */
    public ReadFilesFromFTP(boolean isPrintCommmand) {
        this.ftpClient = new FTPClient();
        this.arFiles = new ArrayList<>();
        if (isPrintCommmand) {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        }
    }

    /**
     * Log into FTP server
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public boolean login(String host, int port, String username, String password) throws IOException {
        this.ftpClient.connect(host, port);
        if (FTPReply.isPositiveCompletion(this.ftpClient.getReplyCode())) {
            if (this.ftpClient.login(username, password)) {
                this.ftpClient.setControlEncoding("GBK");
                FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
                conf.setServerLanguageCode("zh");
                this.ftpClient.configure(conf);
                return true;
            }
        }
        if (this.ftpClient.isConnected()) {
            this.ftpClient.disconnect();
        }
        return false;
    }

    /**
     * 递归遍历目录下面指定的文件名
     *
     * @param pathName 需要遍历的目录，必须以"/"开始和结束
     * @param ext      文件的扩展名
     * @throws IOException
     */
    public void getAllFiles(String pathName) throws IOException {
        if (pathName.startsWith("/") && pathName.endsWith("/")) {
            //更换目录到当前目录
            boolean isTrue = this.ftpClient.changeWorkingDirectory(pathName);
            FTPFile[] files = this.ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.isFile()) {
                    arFiles.add(pathName + file.getName());
                } else if(file.isDirectory()) {
                    if (!".".equals(file.getName()) && !"..".equals(file.getName())) {
                        getAllFiles(pathName + file.getName() + "/");
                    }
                }
            }
        }
    }

    /**
     * 关闭数据链接
     *
     * @throws IOException
     */
    public void disConnection() throws IOException {
        if (this.ftpClient.isConnected()) {
            this.ftpClient.disconnect();
        }
    }

    public static void main(String[] args) throws IOException {
        ReadFilesFromFTP filesFromFTP = new ReadFilesFromFTP(true);
        if (filesFromFTP.login("192.168.31.196", 21, "ftpuser", "Zyw970413!")) {
            filesFromFTP.getAllFiles("/");
        }
        filesFromFTP.disConnection();
        for (String arFile : filesFromFTP.arFiles) {
            logger.info(arFile);
        }
    }
}

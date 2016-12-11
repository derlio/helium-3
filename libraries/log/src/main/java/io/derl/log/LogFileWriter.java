package io.derl.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by derlio
 * On 2016/12/11 11:40
 */

class LogFileWriter {

    private static final String THREAD_NAME = "h3_log_thread";

    private static final int LOG_FILES_MAX_NUM = 5; //文件最多有5个
    private static final int LOG_FILE_MAX_SIZE = 1000 * 1000; //文件最大1MB

    private static final long TIME_TO_RELEASE = 10 * 1000;


    private static HandlerThread sHandlerThread;
    private static Handler sHandler;
    private static String sLogDir;

    private static final DateFormat LOG_FILE_DATE_FORMAT = new SimpleDateFormat("MM-dd-HH-mm");
    private static final DateFormat LOG_TIME_TAG_FORMAT = new SimpleDateFormat("MM-dd HH-mm:ss:SSS");
    private static final LogFileFilter sLogFileFilter = new LogFileFilter();

    public static void setLogDir(String dir) {
        sLogDir = dir;
    }

    public static void writeLogToFile(String tag, String message) {
        checkThreadAlive();
        sHandler.obtainMessage(Code.WRITE, buildLog(tag, message)).sendToTarget();
    }

    private static String buildLog(String tag, String message){
        StringBuilder sb = new StringBuilder(LOG_TIME_TAG_FORMAT.format(new Date()));
        sb.append(" ").append(tag).append("\n").append(message).append("\n");
        return sb.toString();
    }

    private static void checkThreadAlive() {
        if (sHandlerThread == null) {
            sHandlerThread = new HandlerThread(THREAD_NAME);
            sHandlerThread.start();
            initHandler();
        }
        if (sHandler == null) {
            initHandler();
        }
    }

    private static void initHandler() {
        sHandler = new Handler(sHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Code.WRITE:
                        writeFileInternal((String) msg.obj);
                        this.removeMessages(Code.SLEEP);
                        this.sendEmptyMessageDelayed(Code.SLEEP, TIME_TO_RELEASE);
                        break;
                    case Code.SLEEP:
                        releaseResource();
                        break;
                }
            }
        };
    }

    private static void releaseResource() {
        sHandlerThread.quitSafely();
        sHandler = null;
        sHandlerThread = null;
    }

    private static void writeFileInternal(String log) {
        File logFile = getLogFile();
        if (logFile != null) {
            FileUtils.writeToFile(log, logFile.getPath());
        }
    }

    private static File getLogFile() {
        File dir = new File(sLogDir);
        File[] files = dir.listFiles(sLogFileFilter);
        if (files == null || files.length == 0) {
            // 创建新文件
            return createNewLogFile();
        }
        List<File> sortedFiles = sortFiles(files);
        if (files.length > LOG_FILES_MAX_NUM) {
            // 删掉最老的文件
            FileUtils.delete(sortedFiles.get(0));
        }
        // 取最新的文件，看写没写满
        File lastLogFile = sortedFiles.get(sortedFiles.size() - 1);
        if (lastLogFile.length() < LOG_FILE_MAX_SIZE) {
            return lastLogFile;
        } else {
            // 创建新文件
            return createNewLogFile();
        }
    }

    private static File createNewLogFile() {
        return FileUtils.createFile(sLogDir + "/log-" + LOG_FILE_DATE_FORMAT.format(new Date()) + ".txt");
    }

    private static List<File> sortFiles(File[] files) {
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new FileComparator());
        return fileList;
    }

    private static class FileComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            if (file1.lastModified() < file2.lastModified()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static class LogFileFilter implements FileFilter {

        public boolean accept(File file) {
            String tmp = file.getName().toLowerCase();
            if (tmp.startsWith("log") && tmp.endsWith(".txt")) {
                return true;
            }
            return false;
        }
    }

    private static class Code {
        public static final int WRITE = 0;
        public static final int SLEEP = 1;
    }
}

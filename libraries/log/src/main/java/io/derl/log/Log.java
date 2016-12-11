package io.derl.log;

/**
 * Created by derlio
 * On 2016/12/11 08:51
 */

public class Log {
    private static final String LOG_PREFIX = "He3_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void setLogDir(String logDir) {
        LogFileWriter.setLogDir(logDir);
    }

    public static void v(String tag, Object... messages) {
        // Only log VERBOSE if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, android.util.Log.VERBOSE, null, messages);
        }
    }

    public static void d(String tag, Object... messages) {
        // Only log DEBUG if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, android.util.Log.DEBUG, null, messages);
        }
    }

    public static void i(String tag, Object... messages) {
        log(tag, android.util.Log.INFO, null, messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag, android.util.Log.WARN, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages) {
        log(tag, android.util.Log.WARN, t, messages);
    }

    public static void e(String tag, Object... messages) {
        log(tag, android.util.Log.ERROR, null, messages);
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, android.util.Log.ERROR, t, messages);
    }

    public static void f(String tag, Object... messages){
        f(tag, null, messages);
    }

    public static void f(String tag, Throwable t, Object... messages) {
        LogFileWriter.writeLogToFile(tag, buildMessage(t, messages));
    }

    public static void log(String tag, int level, Throwable t, Object... messages) {
        if (android.util.Log.isLoggable(tag, level)) {
            android.util.Log.println(level, tag, buildMessage(t, messages));
        }
    }


    private static String buildMessage(Throwable t, Object... messages){
        String message;
        if (t == null && messages != null && messages.length == 1) {
            // handle this common case without the extra cost of creating a stringbuffer:
            message = messages[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (messages != null) for (Object m : messages) {
                sb.append(m);
            }
            if (t != null) {
                sb.append("\n").append(android.util.Log.getStackTraceString(t));
            }
            message = sb.toString();
        }
        return message;
    }
}

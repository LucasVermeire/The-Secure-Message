package utils;

public class Logger {
    public static void log(String message){
        log( "", "", message);
    }

    public static void logError(String message){ logError( "", "", message); }

    public static void log(String classSimpleName, String methodName, String message){ ConsoleLogger.log(false, classSimpleName, methodName, message); }

    public static void logError(String classSimpleName, String methodName, String message) { ConsoleLogger.log(true, classSimpleName, methodName, message); }
}

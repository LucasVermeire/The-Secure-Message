package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger {

    private static final boolean DISPLAY_DATE = true;

    public static void log(boolean isError, String classSimpleName, String methodName, String message) {
        boolean classSimpleNameToPrint = classSimpleName.trim().length() > 0;
        boolean methodNameToPrint = methodName.trim().length() > 0;
        boolean messageToPrint = message.trim().length() > 0;
        String dateString = String.format("[%s]", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));

        String stringBuilder = (DISPLAY_DATE ? dateString : "")
                + (classSimpleNameToPrint && methodNameToPrint ? String.format("[ %s - %s ]", classSimpleName, methodName) :
                    classSimpleNameToPrint ? String.format("[ %s ]", classSimpleName) : "")
                + (messageToPrint ? String.format(" %s", message) : "");

        if(isError){
            System.err.println(stringBuilder);
        } else {
            System.out.println(stringBuilder);
        }
    }
}

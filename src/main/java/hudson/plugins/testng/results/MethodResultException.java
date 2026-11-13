package hudson.plugins.testng.results;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;

@SuppressFBWarnings(
        value = "NM_CLASS_NOT_EXCEPTION",
        justification = "Not an exception, but represents one associated with a test result parsed by this plugin")
@SuppressWarnings("serial")
public class MethodResultException implements Serializable {

    private String exceptionName;
    private String message;
    private String stackTrace;

    public MethodResultException(String exceptionName, String message, String shortStackTrace, String fullStackTrace) {
        this.message = message == null ? null : message.trim();
        this.exceptionName = exceptionName;
        trySettingData(shortStackTrace, fullStackTrace);
    }

    public String getExceptionName() {
        return exceptionName;
    }

    // Backfills exceptionName/stackTrace for old builds serialized before those fields existed.
    private void trySettingData(String shortStackTrace, String fullStackTrace) {
        String tmpStackTrace = shortStackTrace;
        if (((shortStackTrace == null) || "".equals(shortStackTrace)) && (fullStackTrace != null)) {
            tmpStackTrace = fullStackTrace;
        }

        // Neither stacktrace field is required by the TestNG XML schema (e.g. SkipException);
        // treat a missing one as "" instead of throwing and aborting the whole parse.
        stackTrace = tmpStackTrace == null ? "" : tmpStackTrace.trim();
        int index;

        if (message == null) {
            // no message means first line will only show exception class name
            index = stackTrace.indexOf("\n");
            if (index != -1) {
                if (exceptionName == null || exceptionName.isEmpty()) {
                    exceptionName = stackTrace.substring(0, index);
                }
                stackTrace = stackTrace.substring(index + 1, stackTrace.length());
            }
        } else {
            message = message.trim();
            // message being present means first line will be of type
            // <exception class name>: <message>
            index = stackTrace.indexOf(": ");
            if (index != -1) {
                if (exceptionName == null || exceptionName.isEmpty()) {
                    exceptionName = stackTrace.substring(0, index);
                }
                stackTrace =
                        stackTrace.substring(index + 2, stackTrace.length()).replace(message, "");
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(exceptionName).append(": ");
        if (message != null) {
            str.append(message);
        }
        str.append("\n");
        str.append(stackTrace);
        return str.toString();
    }
}

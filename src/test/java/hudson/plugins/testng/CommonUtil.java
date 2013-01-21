package hudson.plugins.testng;

import hudson.FilePath;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestNGResult;

import java.io.*;
import java.net.URL;

/**
 * Common utility methods
 *
 * @author nullin
 */
public class CommonUtil {

    public static URL getResource(String filepath) {
       return CommonUtil.class.getClassLoader().getResource(filepath);
    }

    public static String getContents(String filepath) throws IOException {
        InputStream is = CommonUtil.class.getClassLoader().getResourceAsStream(filepath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            StringBuilder sb = new StringBuilder("");
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    public static TestNGResult getResults(String filepath) {
       ResultsParser parser = new ResultsParser();
       FilePath[] filePaths = new FilePath[1];
       filePaths[0] = new FilePath(new File(filepath));
       return parser.parse(filePaths);
    }
}

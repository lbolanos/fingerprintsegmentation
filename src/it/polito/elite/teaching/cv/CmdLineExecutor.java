package it.polito.elite.teaching.cv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;

public class CmdLineExecutor {
    private static final String NBIS = "NBIS";

	public CmdLineExecutor() {
    }

    public static String execCommand(String command) {
        long time = Calendar.getInstance().getTimeInMillis();

        try {
            Process nativeApp = Runtime.getRuntime().exec(new String[]{"cmd", "/C", command});
            AppUtils.logFM( null,NBIS, command);
            nativeApp.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(nativeApp.getInputStream()));
            StringBuilder log = new StringBuilder();

            String line;
            while((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }

            AppUtils.logFM( null,NBIS, log.toString());
            AppUtils.logFM( null,NBIS, " time taken " + (Calendar.getInstance().getTimeInMillis() - time));
            return log.toString();
        } catch (Exception var7) {
            AppUtils.logFM( null,NBIS, var7.getMessage());
            return "";
        }
    }
}

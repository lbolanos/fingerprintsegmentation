package it.polito.elite.teaching.cv;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

import android.util.Log;
import android.util.Pair;

public final class AppUtils {
    private static String id = null;

    public AppUtils() {
    }

    public static void setEmployeeId( String Id ) {
        id = Id;
    }

    private static String getEmployeePath() {
        if( id != null && id.length() > 0 ){
            return id +  "/";
        }
        return "";
    }


    public static boolean checkSizeQuality(Mat m) {
        return m.rows() >= 10 && m.cols() >= 10;
    }

    public static <A, B> List<Pair<A, B>> zip(List<A> listA, List<B> listB) {
        if (listA.size() != listB.size()) {
            throw new IllegalArgumentException("Lists must have same size");
        } else {
            List<Pair<A, B>> pairList = new LinkedList();

            for(int index = 0; index < listA.size(); ++index) {
                pairList.add(Pair.create(listA.get(index), listB.get(index)));
            }

            return pairList;
        }
    }

    public static void showTimeTaken(String what, long when) {
        long now = Calendar.getInstance().getTimeInMillis();
        Log.d("TIME_TAKEN", "time taken in " + what + " " + (now - when) + " ms");
    }

    public static void deleteGarbageEnrollFiles() {
    	CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "\\*");
        if( StaticConfig.DEBUG ) {
            return;
        }
        //CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.xyt");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.brw");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.qm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.lfm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.jpeg");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.hcm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.lcm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.lfm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.min");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.q");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/ENROLL/*.dm");
        //CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + getEmployeePath() + "/ENROLL/*.xyt");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.brw");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.qm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.lfm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.jpeg");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.hcm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.lcm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.lfm");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.min");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.q");
        CmdLineExecutor.execCommand("del /Q " + StaticConfig.DEST_FOLDER + "/" + getEmployeePath() + "ENROLL/*.dm");
    }

	public static void logFM(IDebugImage di, String TAG, String s) {
		if( di != null ) {
			di.log(TAG,s);
		} else {
			try  {
				Log.d(TAG,s);
			} catch (Exception e) {
				System.out.println(s);
			}
		}
	}
}

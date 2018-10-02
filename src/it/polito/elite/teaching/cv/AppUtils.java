package it.polito.elite.teaching.cv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.os.Environment;
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

    public static void copyExecutableFile(String assetPath, String localPath, Context context) {
        try {
            InputStream in = context.getAssets().open(assetPath);
            FileOutputStream out = new FileOutputStream(localPath);
            byte[] buffer = new byte[4096];

            int read;
            while((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            out.close();
            in.close();
            File file = new File(localPath);
            file.setExecutable(true, false);
        } catch (IOException var8) {
            Log.i("NBIS", var8.getMessage());
        }

    }

    public static void removeExecutable(String assetPath) {
        try {
            (new File(assetPath)).delete();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public static void copyExecutableFile(String assetPath, String localPath) {
        try {
            InputStream in = new FileInputStream(assetPath);
            FileOutputStream out = new FileOutputStream(localPath);
            byte[] buffer = new byte[4096];

            int read;
            while((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            out.close();
            in.close();
            File file = new File(localPath);
            file.setExecutable(true, false);
        } catch (IOException var7) {
            Log.i("NBIS", var7.getMessage());
        }

    }

    public static void copyInternalStorageFile(String filePath, String localPath) {
        try {
            InputStream in = new FileInputStream(filePath);
            FileOutputStream out = new FileOutputStream(localPath);
            byte[] buffer = new byte[512];

            int read;
            while((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

            out.close();
            in.close();
            File file = new File(localPath);
            file.setExecutable(true, false);
        } catch (IOException var7) {
            Log.i("NBIS", var7.getMessage());
        }

    }

    public static void showTimeTaken(String what, long when) {
        long now = Calendar.getInstance().getTimeInMillis();
        Log.d("TIME_TAKEN", "time taken in " + what + " " + (now - when) + " ms");
    }

    public static boolean isOverlapping(RectF first, RectF second, float overlappingthres) {
        float x_left = Math.max(first.left, second.left);
        float y_bottom = Math.min(first.bottom, second.bottom);
        float x_right = Math.min(first.right, second.right);
        float y_top = Math.max(first.top, second.top);
        float width = Math.max(0.0F, x_right - x_left + 1.0F);
        float height = Math.max(0.0F, y_bottom - y_top + 1.0F);
        float area = (second.right - second.left) * (second.bottom - second.top);
        float overlap = width * height / area;
        return overlap > overlappingthres;
    }

    public static boolean isOverlappingWhileFrame(RectF first, RectF second, float overlappingthres) {
        Log.d("OVERLAPPING", "first:" + first.toString());
        Log.d("OVERLAPPING", "second:" + second.toString());
        float x_left = Math.max(first.left, second.left);
        float y_bottom = Math.min(first.bottom, second.bottom);
        float x_right = Math.min(first.right, second.right);
        float y_top = Math.max(first.top, second.top);
        float width = Math.max(0.0F, x_right - x_left + 1.0F);
        float height = Math.max(0.0F, y_bottom - y_top + 1.0F);
        float area = (second.right - second.left) * (second.bottom - second.top);
        float overlap = width * height / area;
        Log.d("OVERLAPPING", "overlap:" + overlap);
        return overlap > overlappingthres;
    }

    public static boolean isNear(RectF first, RectF second, float overlappingthres) {
        return true;
    }

    public static File createExternalDirectory(String folder) {
        String dir_path = getAppDirectory() + "/"  + getEmployeePath() + folder;
        File dir = new File(dir_path);
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            Log.d("mkdir:", String.valueOf( res ) );
        }

        return dir;
    }

    public static File createSecureDirectory(Activity ac, String folder) {
        String dir_path = ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + folder;
        File dir = new File(dir_path);
        if (!dir.exists()) {
            boolean res = dir.mkdirs();
            Log.d("mkdir:", String.valueOf( res ) );
        }
        return dir;
    }

    public static String getAppDirectory() {
        String folder = Environment.getExternalStorageDirectory() + "/" + StaticConfig.APP_DIR;
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir.toString();
    }

    public static void deleteGarbageEnrollFiles(Activity ac) {
        if( StaticConfig.DEBUG ) {
            return;
        }
        //CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.xyt");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.brw");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.qm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.lfm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.jpeg");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.hcm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.lcm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.lfm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.min");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.q");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/ENROLL/*.dm");
        //CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + getEmployeePath() + "/ENROLL/*.xyt");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.brw");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.qm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.lfm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.jpeg");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.hcm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.lcm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.lfm");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.min");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.q");
        CmdLineExecutor.execCommand("rm " + ac.getFilesDir().getAbsolutePath() + "/" + getEmployeePath() + "ENROLL/*.dm");
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

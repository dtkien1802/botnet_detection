package uet.PeerCatcher.main;

import java.io.File;

public class FileModifier {
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            assert children != null;
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        // System.out.println("The directory "+dir.toString()+" is deleted.");
        return dir.delete();
    }
}

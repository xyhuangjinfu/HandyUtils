package cn.hjf.handyutils;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A class provide the storage function on file system.
 * Created by huangjinfu on 2016/9/29.
 */

public final class FileUtil {

    private static final String TAG = "HandyUtils-FileStorage";

    /**
     * save data, override the old file if exist.
     *
     * @param absolutePath file path.
     * @param data         data to storage in above path.
     * @return true-save success, false-save failed.
     */
    public static boolean save(String absolutePath, byte[] data) {
        return save(absolutePath, data, false);
    }

    /**
     * save data, client can choose whether or not override the old file if exist.
     *
     * @param absolutePath file path.
     * @param data         data to storage in above path.
     * @param append       true-append save, false-override save.
     * @return true-save success, false-save failed.
     */
    public static boolean save(String absolutePath, byte[] data, boolean append) {
        File destFile = createFile(absolutePath);
        if (destFile == null) {
            return false;
        }
        if (!hasMoreSpace(destFile, data.length)) {
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(destFile, append);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    /**
     * save serializable object.
     *
     * @param absolutePath file path.
     * @param object       object to storage in above path.
     * @return true-save success, false-save failed.
     */
    public static boolean save(String absolutePath, Serializable object) {
        byte[] objectData = objectToByteArray(object);
        if (objectData == null) {
            return false;
        }
        return save(absolutePath, objectData);
    }

    /**
     * read byte[] from file.
     *
     * @param absolutePath file path from which we read the data.
     * @return file data, or null if file not exist or other error occurs.
     */
    @Nullable
    public static byte[] readBytes(String absolutePath) {
        byte[] data = null;
        try {
            File destFile = new File(absolutePath);
            FileInputStream fileInputStream = new FileInputStream(destFile);
            data = new byte[fileInputStream.available()];
            fileInputStream.read(data);
            fileInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return data;
    }

    /**
     * read object from file.
     *
     * @param absolutePath file path from which we read the data.
     * @return file data, or null if file not exist or other error occurs.
     */
    public static Object readObject(String absolutePath) {
        Object object = null;
        try {
            File destFile = new File(absolutePath);
            if (destFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(destFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                object = objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return object;
    }

    /**
     * convert serializable object to byte array.
     *
     * @param object serializable object
     * @return byte array which present this object, or null if object is null or other error occurs.
     */
    @Nullable
    public static byte[] objectToByteArray(Serializable object) {
        if (object == null) {
            return null;
        }

        byte[] bytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            System.out.println(e);
            Log.e(TAG, e.toString());
        }
        return bytes;
    }

    /**
     * convert byte array to object
     *
     * @param byteArray byte array which present an object, may generate by {@link FileUtil#objectToByteArray(Serializable)}.
     * @return the object, or null if byte array is null or other error occurs.
     */
    @Nullable
    public static Object byteArrayToObject(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        Object object = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            ObjectInputStream ois = new ObjectInputStream(bis);
            object = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return object;
    }

    /**
     * copy a file.
     *
     * @param fromPath the src file path.
     * @param toPath   the dest file path.
     * @param cover    true-cover the dest file if exist.
     * @return true-actual do copy, false-copy failed or skipped.
     */
    public static boolean copy(String fromPath, String toPath, boolean cover) {
        //create dest file.
        File toFile = createFile(toPath);
        if (toFile == null) {
            return false;
        }
        //get src file.
        File fromFile = createFile(fromPath);
        if (fromFile == null) {
            return false;
        }
        //if in cover mode, delete the dest file if exists.
        if (cover) {
            if (toFile.exists() && !toFile.delete()) {
                Log.e(TAG, "cover mode, cannot delete dest file : " + toPath);
                return false;
            }
        } else {
            //not cover.
            if (toFile.exists()) {
                return false;
            }
        }
        //read data from src file
        byte[] data = readBytes(fromPath);
        if (data == null) {
            Log.e(TAG, "data read from : " + fromPath + ", is null");
            return false;
        }
        //do save
        return save(toPath, data);
    }

    /**
     * delete a file.
     *
     * @param path file to be deleted.
     * @return true-delete success, false-delete failed or file not exist.
     */
    public static boolean delete(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * detect a specific file whether exist.
     *
     * @param path detect path.
     * @return true-exists, false-not exists.
     */
    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * ********************************************************************************************************
     * ********************************************************************************************************
     */

    /**
     * create file and it's parents.
     *
     * @param absolutePath
     * @return
     */
    @Nullable
    private static File createFile(String absolutePath) {
        File destFile = new File(absolutePath);
        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                Log.e(TAG, "create parent dirs fail, path : " + absolutePath);
                return null;
            }
        }
        return destFile;
    }

    /**
     * calculate remain space whether enough to save the specific file.
     *
     * @param file      file to be saved.
     * @param needSpace data length.
     * @return true-have enough space, false-have't enough space.
     */
    private static boolean hasMoreSpace(File file, long needSpace) {
        File existParent = getExistParent(file);
        if (existParent.getUsableSpace() <= needSpace) {
            Log.e(TAG, "no more space, require : " + needSpace + ", but remain : " + existParent.getUsableSpace());
            return false;
        }
        return true;
    }

    /**
     * find already exist parent along the up direction of the file tree.
     *
     * @param file
     * @return
     */
    private static File getExistParent(File file) {
        File parent = file.getParentFile();
        while (!parent.exists()) {
            parent = parent.getParentFile();
        }
        return parent;
    }
}

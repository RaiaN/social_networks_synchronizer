package com.example.SocialNetworksSynchronizer;

import android.util.Log;

import java.io.*;

public final class Serializer {
    public static byte[] serializeObject(Object o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();
            //
            byte[] buf = bos.toByteArray();

            return buf;
        } catch(IOException ioe) {
            Log.e("serializeObject", "error", ioe);
            return null;
        }
    }

    public static Object deserializeObject(byte[] b) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();

            return object;

        } catch(ClassNotFoundException cnfe) {
            Log.e("deserializeObject", "class not found error", cnfe);
            return null;

        } catch(IOException ioe) {
            Log.e("deserializeObject", "io error", ioe);
            return null;
        }
    }
}

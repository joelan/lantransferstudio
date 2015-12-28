package com.example.networkbroard.utils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created by Administrator on 2015/10/13.
 */
public class ObjectUtil {
    public static Object ByteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }



}

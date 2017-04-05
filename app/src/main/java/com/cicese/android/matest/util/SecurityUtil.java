package com.cicese.android.matest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by netzahdzc on 7/20/16.
 */
public class SecurityUtil {

    public SecurityUtil() {
    }

    public String convert(String s) {
        String outcome = "";

        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            outcome = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return outcome;
    }
}

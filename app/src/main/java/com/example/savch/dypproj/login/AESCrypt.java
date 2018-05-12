package com.example.savch.dypproj.login;

/**
 * Created by savch on 12.05.2018.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import android.util.Base64;

/**
 * Created by Andrii Savchuk on 12.05.2018.
 * All rights are reserved.
 * If you will have any cuastion, please
 * contact via email (savchukndr@gmail.com)
 */
class AESCrypt
{
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";

    static String decrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue,"utf-8");

    }

    private static Key generateKey() throws Exception
    {
        return new SecretKeySpec(AESCrypt.KEY.getBytes(),AESCrypt.ALGORITHM);
    }
}

package ufsm.csi.pilacoin.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static ufsm.csi.pilacoin.util.Constants.PRIVATE_KEY;

public class PilaUtil {

    @SneakyThrows
    public static byte[] geraAssinatura(Object object) {
        String str;
        if (object instanceof String){
            str = (String) object;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            str = objectMapper.writeValueAsString(object);
        }
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, PRIVATE_KEY);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
        return cipher.doFinal(hash);
    }

    @SneakyThrows
    public static byte[] getAssinatura(Object object){
        ObjectMapper om = new ObjectMapper();
        String strObj = om.writeValueAsString(object);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, PRIVATE_KEY);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] assinatura = md.digest(strObj.getBytes(StandardCharsets.UTF_8));
        return cipher.doFinal(assinatura);
    }

    public String geraNonce(){
        Random rnd = new Random();
        byte[] bytes = new byte[256/8];
        rnd.nextBytes(bytes);
        return new BigInteger(bytes).abs().toString();
    }

    public static BigInteger geraHash(Object object) throws JsonProcessingException, NoSuchAlgorithmException {
        String str;
        if (object instanceof String){
            str = (String) object;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            str = objectMapper.writeValueAsString(object);
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return new BigInteger(md.digest(str.getBytes(StandardCharsets.UTF_8))).abs();
    }
}

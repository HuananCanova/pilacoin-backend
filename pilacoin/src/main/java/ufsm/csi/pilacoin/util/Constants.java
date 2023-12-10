package ufsm.csi.pilacoin.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.*;

public class Constants {
    public static String USERNAME = "Huanan Canova";
    public static BigInteger DIFICULDADE = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
    public static PublicKey PUBLIC_KEY;

    public static PrivateKey PRIVATE_KEY;

    public static void initializeKeys() {
        KeyPair keyPair = generateKeyPair();

        PUBLIC_KEY = keyPair.getPublic();
        PRIVATE_KEY = keyPair.getPrivate();

        saveKeyPairToFile(keyPair, "keypair.ser");
    }

    private static KeyPair generateKeyPair() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    private static void saveKeyPairToFile(KeyPair keyPair, String fileName) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(keyPair);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        initializeKeys(); // Chama o método para inicializar as chaves quando a classe é carregada.
    }
}

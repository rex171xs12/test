package com.android.system.qspaas;

import android.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário de decodificação polimórfica.
 * Formato da string codificada: Base64( [ALGO_ID] + [PAYLOAD] )
 */
public class StrUtilAdvanced {

    // IDs dos algoritmos
    private static final byte ALGO_PLAIN = 0;
    private static final byte ALGO_XOR = 1;
    private static final byte ALGO_XOR_DYN = 2;
    private static final byte ALGO_CAESAR = 3;
    private static final byte ALGO_SUB = 4;

    private static final int XOR_KEY = 0x42;

    /**
     * Decodifica string automaticamente baseada no header byte.
     * @param encoded String Base64 contendo [ALGO_ID][DATA]
     * @return String decodificada ou null se falhar
     */
    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return null;

        try {
            byte[] data = Base64.decode(encoded, Base64.NO_WRAP);
            if (data.length < 2) return null;

            byte algo = data[0];
            byte[] payload = new byte[data.length - 1];
            System.arraycopy(data, 1, payload, 0, payload.length);

            switch (algo) {
                case ALGO_PLAIN:
                    return new String(payload, StandardCharsets.UTF_8);

                case ALGO_XOR:
                    return applyXor(payload, XOR_KEY);

                case ALGO_XOR_DYN:
                    return applyXorDynamic(payload, XOR_KEY);

                case ALGO_CAESAR:
                    return applyCaesar(payload, -3); // Inverso do +3

                case ALGO_SUB:
                    return applySubstitution(payload, -5); // Inverso do +5

                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Codifica string com algoritmo aleatório para uso em desenvolvimento/geração.
     * Adiciona header byte automaticamente.
     */
    public static String encode(String plaintext) {
        if (plaintext == null) return null;

        byte[] raw = plaintext.getBytes(StandardCharsets.UTF_8);
        byte algo = (byte) (Math.random() * 5); // 0 a 4
        byte[] payload = applyEncoding(raw, algo);

        // Combina [ALGO] + [PAYLOAD]
        byte[] finalData = new byte[payload.length + 1];
        finalData[0] = algo;
        System.arraycopy(payload, 0, finalData, 1, payload.length);

        return Base64.encodeToString(finalData, Base64.NO_WRAP);
    }

    // --- Lógica Interna de Codificação/Decodificação ---

    private static byte[] applyEncoding(byte[] data, byte algo) {
        byte[] result = new byte[data.length];
        System.arraycopy(data, 0, result, 0, data.length);

        switch (algo) {
            case ALGO_PLAIN:
                break;
            case ALGO_XOR:
                for (int i = 0; i < result.length; i++) result[i] ^= XOR_KEY;
                break;
            case ALGO_XOR_DYN:
                for (int i = 0; i < result.length; i++) result[i] ^= (XOR_KEY + i);
                break;
            case ALGO_CAESAR:
                for (int i = 0; i < result.length; i++) result[i] = (byte) (result[i] + 3);
                break;
            case ALGO_SUB:
                for (int i = 0; i < result.length; i++) result[i] = (byte) ((result[i] + 5) & 0xFF);
                break;
        }
        return result;
    }

    private static String applyXor(byte[] data, int key) {
        for (int i = 0; i < data.length; i++) data[i] ^= key;
        return new String(data, StandardCharsets.UTF_8);
    }

    private static String applyXorDynamic(byte[] data, int key) {
        for (int i = 0; i < data.length; i++) data[i] ^= (key + i);
        return new String(data, StandardCharsets.UTF_8);
    }

    private static String applyCaesar(byte[] data, int shift) {
        for (int i = 0; i < data.length; i++) data[i] = (byte) (data[i] + shift);
        return new String(data, StandardCharsets.UTF_8);
    }

    private static String applySubstitution(byte[] data, int shift) {
        for (int i = 0; i < data.length; i++) data[i] = (byte) ((data[i] + shift) & 0xFF);
        return new String(data, StandardCharsets.UTF_8);
    }
}

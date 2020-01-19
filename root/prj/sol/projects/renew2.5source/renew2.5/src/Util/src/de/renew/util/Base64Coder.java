package de.renew.util;

import java.util.Hashtable;


/**
 * The Base64Coder is an encoder and decoder for byte arrays to base 64
 * coded Strings and vice versa. It can be used where escape characters
 * make problems.
 */
public class Base64Coder {

    /**
     * The encoding array.
     */
    private static final char[] ENCODE_ARRAY = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    /**
     * The decoding hashtable.
     */
    private static final Hashtable<Character, Integer> DECODE_TABLE = new Hashtable<Character, Integer>();

    static {
        for (int i = 0; i < 64; i++) {
            DECODE_TABLE.put(new Character(ENCODE_ARRAY[i]), new Integer(i));
        }
    }

    /**
     * Encodes a byte array into a String of base 64 coded characters.
     * @param plain The byte array.
     * @return The base 64 encoded String.
     */
    public static String encode(byte[] plain) {
        StringBuffer encoded = new StringBuffer();
        encoded.append(String.valueOf(plain.length));
        encoded.append(":");

        for (int i = 0; i < plain.length; i += 3) {
            int plain1 = i + 0 < plain.length ? plain[i + 0] : 0;
            plain1 = plain1 < 0 ? plain1 + 256 : plain1;
            int plain2 = i + 1 < plain.length ? plain[i + 1] : 0;
            plain2 = plain2 < 0 ? plain2 + 256 : plain2;
            int plain3 = i + 2 < plain.length ? plain[i + 2] : 0;
            plain3 = plain3 < 0 ? plain3 + 256 : plain3;
            int plainValue = plain1 + plain2 * 256 + plain3 * 256 * 256;

            encoded.append(ENCODE_ARRAY[plainValue % 64]);
            plainValue /= 64;
            encoded.append(ENCODE_ARRAY[plainValue % 64]);
            plainValue /= 64;
            encoded.append(ENCODE_ARRAY[plainValue % 64]);
            plainValue /= 64;
            encoded.append(ENCODE_ARRAY[plainValue % 64]);
        }

        return encoded.toString();
    }

    /**
     * Decodes a base 64 encoded String into a byte array.
     * @param encoded The base 64 encoded String.
     * @return The byte array.
     */
    public static byte[] decode(String encoded) {
        int colonPosition = encoded.indexOf(":");
        if (colonPosition < 0) {
            throw new IllegalArgumentException("Not a valid base 64 string (no colon)");
        }

        int length;
        try {
            length = Integer.parseInt(encoded.substring(0, colonPosition));
            encoded = encoded.substring(colonPosition + 1);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid base 64 string (invalid length number)");
        }

        byte[] plain = new byte[length];
        for (int i = 0; i * 4 < encoded.length(); i++) {
            try {
                char encoded1 = encoded.charAt(i * 4 + 0);
                char encoded2 = encoded.charAt(i * 4 + 1);
                char encoded3 = encoded.charAt(i * 4 + 2);
                char encoded4 = encoded.charAt(i * 4 + 3);

                int plainValue = (DECODE_TABLE.get(new Character(encoded1)))
                                     .intValue()
                                 + (DECODE_TABLE.get(new Character(encoded2)))
                                     .intValue() * 64
                                 + (DECODE_TABLE.get(new Character(encoded3)))
                                     .intValue() * 64 * 64
                                 + (DECODE_TABLE.get(new Character(encoded4)))
                                     .intValue() * 64 * 64 * 64;

                if (i * 3 + 0 < length) {
                    plain[i * 3 + 0] = (byte) (plainValue % 256);
                }
                plainValue /= 256;

                if (i * 3 + 1 < length) {
                    plain[i * 3 + 1] = (byte) (plainValue % 256);
                }
                plainValue /= 256;

                if (i * 3 + 2 < length) {
                    plain[i * 3 + 2] = (byte) (plainValue % 256);
                }
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Not a valid base 64 string (illegal character)");
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Not a valid base 64 string (mismatching length)");
            }
        }

        return plain;
    }
}
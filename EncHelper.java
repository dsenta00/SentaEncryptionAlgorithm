import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

class EncHelper {

    static Integer [] createSortedIntegerArray(int n) {
        Integer[] numbers = new Integer[n];

        for (int i = 0; i < n; i++) {
            numbers[i] = i;
        }

        return numbers;
    }

    static void shuffleAccordingToKey(Integer[] numbers, String key64) {
        long key = Long.parseLong(key64, 16);
        Collections.shuffle(Arrays.asList(numbers), new Random(key));
    }

    static String xorHexStrings(String hexString, String key64) {
        StringBuilder result = new StringBuilder();
        long key = parseLongBlock(key64, 0, Long.BYTES * HashCubeParams.HEX_BYTE_STR_LENGTH);

        hexString = resizeHexStringToTheFirstPowerOf16(hexString);

        System.out.println("\tinput:  " + hexString);
        int n = ((hexString.length() / HashCubeParams.HEX_BYTE_STR_LENGTH) / Long.BYTES);
        for (int i = 0; i < n; i++) {

            long chunk = parseLongBlock(hexString, i, Long.BYTES * HashCubeParams.HEX_BYTE_STR_LENGTH);
            System.out.printf("\t(chunk) %016X ^ (key) %016X = (result) %s\r\n", chunk, key, String.format("%016X", chunk ^ key));

            result.append(String.format("%016x", chunk ^ key));
        }

        System.out.println("\tresult: " + result.substring(0, hexString.length()));

        return result.substring(0, hexString.length());
    }

    static byte[] prepareBytes(String message) {

        /* 1. Add checksum */
        byte[] checkSum = createChecksum(message);
        byte[] result = arrayConcatenation(message.getBytes(), checkSum);

        /* 2. Prepend length */
        byte[] length = { (byte) message.length() };
        result = arrayConcatenation(length, result);

        /* 3. Add padding */
        byte paddingVal = (byte)(HashCubeParams.NUMBER_OF_DIMENSIONS - (result.length % HashCubeParams.NUMBER_OF_DIMENSIONS));
        result = addPadding(result, paddingVal);

        return result;
    }

    static String validateThenParseMessage(String decryptedMessage) throws Exception {
        int length = decryptedMessage.charAt(0);

        // 1. VALIDATE LENGTH
        if (length > decryptedMessage.length()) {
            throw new Exception("Invalid original text length!");
        }

        // 2. VALIDATE PADDING IF EXISTS
        if (length + 2 < decryptedMessage.length()) {
            byte[] padding = decryptedMessage.substring(length + 2, decryptedMessage.length()).getBytes();

            for (byte b : padding) {
                if (b != padding.length) {
                    throw new Exception("Invalid padding!");
                }
            }
        }

        String original = decryptedMessage.substring(1, length + 1);
        byte checkSum = (byte) decryptedMessage.charAt(length + 1);
        byte [] checkSumToCompare = createChecksum(original);

        if (checkSum != checkSumToCompare[0]) {
            throw new Exception("Checksum failed!");
        }

        return original;
    }

    static String toStringHex6(int number) {
        return String.format(
                "%02x%02x%02x",
                number & HashCubeParams.X_MASK,
                (number & HashCubeParams.Y_MASK) >> 8,
                (number & HashCubeParams.Z_MASK) >> 16
        );
    }

    static String toStringText3(int number) {
        return String.format(
                "%c%c%c",
                number & HashCubeParams.X_MASK,
                (number & HashCubeParams.Y_MASK) >> 8,
                (number & HashCubeParams.Z_MASK) >> 16
        );
    }

    static long parseLongBlock(String hexString, int blockNo, int blockOffset) {
        int startIndex = blockNo * blockOffset;
        int endIndex = startIndex + blockOffset;

        if (endIndex > hexString.length()) {
            endIndex = hexString.length();
        }

        return Long.parseLong(hexString.substring(startIndex, endIndex), 16);
    }

    private static int firstMultipleOf(int multipleOf, int number) {
        int i = multipleOf;

        while (i < number) {
            i += multipleOf;
        }

        return i;
    }

    private static String resizeHexStringToTheFirstPowerOf16(String hexString) {
        int hexStringLength = firstMultipleOf(Long.BYTES * HashCubeParams.HEX_BYTE_STR_LENGTH, hexString.length());

        if (hexStringLength > hexString.length()) {
            hexString = hexString.concat(new String(new char[hexStringLength - hexString.length()]).replace('\0', '0'));
        }

        return hexString;
    }


    private static byte[] arrayConcatenation(byte[] arr1, byte[] arr2) {
        int newLength = arr1.length + arr2.length;
        byte[] result = Arrays.copyOf(arr1, newLength);

        System.arraycopy(arr2, 0, result, arr1.length, newLength - arr1.length);

        return result;
    }

    private static byte[] createChecksum(String message) {
        byte result = 0;

        for (byte b : message.getBytes()) {
            result = (byte)(((int)result + b) % Byte.MAX_VALUE + 1);
        }

        return new byte[] { result };
    }


    private static byte[] addPadding(byte[] arr, byte paddingVal) {

        if (paddingVal == 0) {
            return arr;
        }

        byte[] padding = new byte[paddingVal];

        for (int i = 0; i < paddingVal; i++) {
            padding[i] = paddingVal;
        }

        return arrayConcatenation(arr, padding);
    }
}

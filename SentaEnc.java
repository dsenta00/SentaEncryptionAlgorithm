/**
 * # SENTA ENCRYPTION ALGORITHM
 *
 * Hash-then-encrypt algorithm that use hash cubes (HC) to store and get minihashes.
 * Every minihash has unique value in HC. Every three characters of the plaintext is replaced with a minihash.
 * Resulting hash is then encrypted with a 64 bit key (XOR).
 * The same key is used in order to shuffle minihashes from the original position in hash cube.
 *
 * ### Hash cube - HC
 *
 * Hash cube is a three dimensional integer array with dimensions 256 x 256 x 256.
 * Every element of HC with coordinates (x, y, z) represents the unique 3 byte hash element  { h0, h1, h2 } [1]
 *
 *     miniHash = HC(x, y, z)
 *     miniHash => { h0, h1, h2 }
 *
 * ### Inverse hash cube - IHC
 *
 * Inverse hash cube is a three dimensional integer array with dimensions 256 x 256 x 256.
 * Every HC has one and only one matching IHC.
 * Every element of the IHC with coordinates (h0, h1, h2) represents the unique three character string (s0, s1, s2)
 *
 *     str3 = IHC(h0, h1, h2)
 *     str3 => { s0, s1, s2 }
 *     s0 => x
 *     s1 => y
 *     s2 => z
 *
 * ### Key64:
 *
 * Key64 is an 64 bit key used for following purposes:
 *
 * 1. For shuffling element positions in HC.
 * 2. XOR encryption of hash output
 * 3. XOR decryption of ciphertext
 *
 * ## ----- ENCRYPTION PROCESS
 *
 * #### 1. Let the plaintext be: "Hello"
 *
 *     P => { 'H', 'e', 'l', 'l', 'o' } => { 0x68, 0x65, 0x6c, 0x6c, 0x6f }
 *
 * #### 2. Add fields plaintext in order to add length (1 byte), checksum (1 byte) and padding in order to length be a number that is multiple of number 3.
 *
 *               +-- length (5)                          +--------+--- padding
 *               |                                       |        |
 *      P => { 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x14, 0x02, 0x02 }
 *                   |                          |   |
 *                   +----- original text ------+   +----- checksum
 *
 * #### 3. Hashing H = HC(P)
 *
 *     p012 => { 0x05, 0x68, 0x65 } => HC(0x05, 0x68, 0x65) => { h0, h1, h2 }
 *     p345 => { 0x6c, 0x6c, 0x6f } => HC(0x6c, 0x6c, 0x6f) => { h3, h4, h5 }
 *     p678 => { 0x14, 0x02, 0x02 } => HC(0x14, 0x02, 0x02) => { h6, h7, h8 }
 *
 *     Result: H = { h0, h1, ..., h8 }
 *
 * #### 4. XOR encryption with key K: C = E(H, K)
 *
 *     H = { h0, h1, ..., h8 }
 *     K = { k0, k1, ..., k8 } XOR
 *     -----------------------
 *     C = { c0, c1, ..., c8 } (result ciphertext)
 *
 * ## ----- DECRYPTION PROCESS
 *
 * #### 1. Let the ciphertext be previous C = { c0, c1, ..., c8 }
 *
 * #### 2. XOR decryption with key K: H = E(C, K)
 *
 *     C = { c0, c1, ..., c8 }
 *     K = { k0, k1, ..., k8 } XOR
 *     -----------------------
 *     H = { h0, h1, ..., h8 } (result hash)
 *
 * #### 3. Get the plaintext with header and footer by using IHC algorithm: P = IHC(H)
 *
 *     h012 => { h0, h1, h2 } => IHC(h0, h1, h2) => { 0x05, 0x68, 0x65 }
 *     h345 => { h3, h4, h5 } => IHC(h3, h4, h5) => { 0x6c, 0x6c, 0x6f }
 *     h678 => { h6, h7, h8 } => IHC(h6, h7, h8) => { 0x14, 0x02, 0x02 }
 *
 *     Result:
 *
 *               +-- length (5)                          +--------+--- padding
 *               |                                       |        |
 *      P => { 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x14, 0x02, 0x02 }
 *                   |                          |   |
 *                   +----- original text ------+   +----- checksum
 *
 * #### 4. Validate then extract the plaintext
 *
 * 1. Get length of plaintext Check checksum.
 * 2. Validate length
 * 3. Validate padding
 * 4. Validate checksum
 * 5. Extract the plaintext P = "Hello"
 *
 * [1] - integer size is 4 bytes, thus one byte is unused and set to zero value.
 */
class SentaEnc {
    private HashCube hashCube;
    private String key64;

    /**
     * The constructor.
     *
     * @param key64
     * @throws Exception
     */
    SentaEnc(String key64) throws Exception {
        this.key64 = key64;

        Integer[] numbers = EncHelper.createSortedIntegerArray(HashCubeParams.NUMBER_OF_FIELDS);
        EncHelper.shuffleAccordingToKey(numbers, key64);

        this.hashCube = new HashCube(numbers);
    }

    /**
     * Encrypt message.
     *
     * @param message
     * @return
     */
    String encrypt(String message) {
        byte[] bytes = EncHelper.prepareBytes(message);
        int N = bytes.length / HashCubeParams.NUMBER_OF_DIMENSIONS;

        StringBuilder ciphertext = new StringBuilder();

        System.out.println("1. HASHING");
        for (int i = 0; i < N; i++) {
            int x = i * HashCubeParams.NUMBER_OF_DIMENSIONS;
            int y = x + 1;
            int z = y + 1;

            String miniHash = hashCube.getMiniHashString(bytes[x], bytes[y], bytes[z]);

            ciphertext.append(miniHash);
            System.out.printf("\tHC(%c%c%c) => %s\r\n", bytes[x], bytes[y], bytes[z], miniHash);
        }

        System.out.println("2. XOR ENCRYPT");
        return EncHelper.xorHexStrings(ciphertext.toString(), key64);
    }

    /**
     * Decrypt message.
     *
     * @param ciphertext
     * @return
     * @throws Exception
     */
    String decrypt(String ciphertext) throws Exception {
        StringBuilder message = new StringBuilder();

        System.out.println("3. XOR DECRYPT");
        ciphertext = EncHelper.xorHexStrings(ciphertext, key64);

        System.out.println("4. DEHASHING");
        for (int i = 0; i < ciphertext.length() / HashCubeParams.MINI_HASH_SIZE_BYTES; i++) {
            long miniHash = EncHelper.parseLongBlock(ciphertext, i, HashCubeParams.MINI_HASH_SIZE_BYTES);
            String originalText = hashCube.getOriginalTextFromMiniHash((int)miniHash);
            System.out.printf("\tHCI(%06x) => %s\r\n", miniHash, originalText);

            message.append(originalText);
        }

        System.out.println("\r\n5. VALIDATE & EXTRACT PLAINTEXT:");
        return EncHelper.validateThenParseMessage(message.toString());
    }
}

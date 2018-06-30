


class HashCubeParams {
    static final int X_MASK = 0x000000ff;
    static final int Y_MASK = 0x0000ff00;
    static final int Z_MASK = 0x00ff0000;
    static final int DIMENSION_SIZE = 256;
    static final int NUMBER_OF_FIELDS = DIMENSION_SIZE * DIMENSION_SIZE * DIMENSION_SIZE;
    static final int NUMBER_OF_DIMENSIONS = 3;
    static final int MINI_HASH_SIZE_BYTES = 6;
    static final int HEX_BYTE_STR_LENGTH = 2; // aa 0a ...
}

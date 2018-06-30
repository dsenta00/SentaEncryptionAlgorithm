
class HashCube {
    private int[][][] cube = new int[HashCubeParams.DIMENSION_SIZE][HashCubeParams.DIMENSION_SIZE][HashCubeParams.DIMENSION_SIZE];
    private int[][][] inverseCube = new int[HashCubeParams.DIMENSION_SIZE][HashCubeParams.DIMENSION_SIZE][HashCubeParams.DIMENSION_SIZE];

    HashCube(Integer[] numbers) throws Exception {
        if (numbers.length != HashCubeParams.NUMBER_OF_FIELDS) {
            throw new Exception("Generated number array should be " + HashCubeParams.NUMBER_OF_FIELDS + " length!");
        }

        for (int i = 0; i < HashCubeParams.NUMBER_OF_FIELDS; i++) {
            int miniHash = numbers[i];

            /* Initialize cube field with miniHash */
            int cubeX = i & HashCubeParams.X_MASK;
            int cubeY = (i & HashCubeParams.Y_MASK) >> 8;
            int cubeZ = (i & HashCubeParams.Z_MASK) >> 16;

            cube[cubeX][cubeY][cubeZ] = miniHash;

            /* Initialize inverseCube with original */
            int inverseCubeX = miniHash & HashCubeParams.X_MASK;
            int inverseCubeY = (miniHash & HashCubeParams.Y_MASK) >> 8;
            int inverseCubeZ = (miniHash & HashCubeParams.Z_MASK) >> 16;

            inverseCube[inverseCubeX][inverseCubeY][inverseCubeZ] = i;
        }
    }

    String getMiniHashString(int x, int y, int z) {
        int miniHash = cube[x][y][z];

        return EncHelper.toStringHex6(miniHash);
    }

    String getOriginalTextFromMiniHash(int miniHash) {
        int z = miniHash & HashCubeParams.X_MASK;
        int y = (miniHash & HashCubeParams.Y_MASK) >> 8;
        int x = (miniHash & HashCubeParams.Z_MASK) >> 16;

        int original = inverseCube[x][y][z];

        return EncHelper.toStringText3(original);
    }
}

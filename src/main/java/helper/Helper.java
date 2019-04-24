package helper;

public class Helper {
    static String LINE_END = "0\n";

    // Returns a Big Endian array -> MSB at arr[len-1]
    public static boolean[] getBitsOfInteger (int numBits, int value) {
        boolean[] bitArray = new boolean[numBits];
        for (int i = numBits - 1; i >= 0; i --) {
            bitArray[i] = (value & (1 << i)) != 0;
        }
        return bitArray;
    }

    // Assumption that the bit array is in Big Endian
    public static int bitsToInteger(boolean[] bits) {
        int val = 0;
        for (int i = 0 ; i < bits.length; i++) {
            if (bits[i]){
                val += Math.pow(2, i);
            }
        }
        return val;
    }
}

package in.ac.bits.javagen;

public class ReturnType {

    @SuppressWarnings("rawtypes")
    public static final Class getReturnType(int numberofBits)
            throws IllegalArgumentException {

        if (numberofBits <= 0) {
            throw new IllegalArgumentException(
                    "Number of bits are non-positive!");
        } else if (numberofBits > 0 && numberofBits < 8) {
            return byte.class;
        } else if (numberofBits >= 8 && numberofBits < 16) {
            return short.class;
        } else if (numberofBits >= 16 && numberofBits < 32) {
            return int.class;
        } else if (numberofBits >= 32 && numberofBits < 64) {
            return long.class;
        } else {
            return double.class;
        }
    }
}

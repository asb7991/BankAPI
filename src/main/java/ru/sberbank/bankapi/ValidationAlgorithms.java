package ru.sberbank.bankapi;

public final class ValidationAlgorithms {
    public static int getControlNumberForCardByLuhnAlgorithm(String input) {
        String testNumber = input + "0";
        int sum1 = 0;
        int sum2 = 0;
        final int nDigits = testNumber.length();
        for (int i = nDigits; i > 0; i--){
            int digit = Character.getNumericValue(testNumber.charAt(i-1));
            int z=digit;
            int y=digit;
            if ((nDigits - i - 1) % 2 == 0){
                z *= 2;
                if (z > 9) {
                    z -= 9;
                }
                sum1 += z;
            }
            else  sum2 += y;
        }
        int sum = sum1 + sum2;
        int controlNum;
        if(sum % 10 == 0) {
            controlNum = 0;
        } else {
            controlNum = 10 - (sum % 10);
        }
        return controlNum;
    }

    public static boolean checkValidityCardByLuhnAlgorithm(String input) {
        int sum1 = 0;
        int sum2 = 0;
        final int nDigits = input.length();
        for (int i = nDigits; i > 0; i--){
            int digit = Character.getNumericValue(input.charAt(i-1));
            int z=digit;
            int y=digit;
            if ((nDigits - i - 1) % 2 == 0){
                z *= 2;
                if (z > 9) {
                    z -= 9;
                }
                sum1 += z;
            }
            else  sum2 += y;
        }
        int sum = sum1 + sum2;
        return sum % 10 == 0;
    }

    public static int getControlNumberForAccountNumber(String input) {
        String testNumber = input.replace("K", "0");
        final String weights = "71371371371371371371371";
        int sum = 0;
        for(int i = 0; i < testNumber.length(); i++) {
            sum += (testNumber.charAt(i)*weights.charAt(i)) % 10;
        }
        return ((sum % 10) * 3) % 10;
    }

    public static boolean checkValidityAccountNumber(String input) {
        final String weights = "71371371371371371371371";
        int sum = 0;
        for(int i = 0; i < input.length(); i++) {
            sum += (input.charAt(i)*weights.charAt(i)) % 10;
        }
        return sum % 10 == 0;
    }
}

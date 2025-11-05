public class Base62 {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62.length();
    public static String encode(long number){
        StringBuilder sb = new StringBuilder();
        while(number > 0){
            sb.insert(0, BASE62.charAt((int) (number % BASE)));
            number = number / BASE;
        }
        return sb.toString();
    }
}

public class Application {

    public static void main(String[] args) {
        String[] split = "%ACCEPT% or {DENY}".split("%");

        String messageKey = split[1];
        String messageValue = split[2];

        System.out.println("Message Key: " + messageKey);
        System.out.println("Message Value: " + messageValue);
    }
}

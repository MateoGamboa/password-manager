public class VaultClient {

    public static String runCommand(String... args) {
        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            java.util.Scanner sc =
                new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");

            return sc.hasNext() ? sc.next() : "";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
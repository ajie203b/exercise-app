import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/** 复刻 UpdateManager 里的十六进制拼接逻辑，确认 Byte 格式化不会产生 ffffff90 之类的符号扩展 */
public class HashCheck {
    public static void main(String[] args) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream in = new FileInputStream(new File(args[0]))) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) digest.update(buf, 0, n);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) sb.append(String.format("%02x", b));
        System.out.println(sb);
        System.out.println("expected: " + args[1]);
        System.out.println("match=" + sb.toString().equalsIgnoreCase(args[1]));
    }
}

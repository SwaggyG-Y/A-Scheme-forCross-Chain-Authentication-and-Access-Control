package main.java.com.example.shamir;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShamirServiceTest {

    @org.junit.Test
    @Test
    public void testShamirSecretSharing() {
        ShamirService shamirService = new ShamirService();

        // 要保护的秘密
        BigInteger secret = new BigInteger("12345678901234567890");

        // 分片数量 n=5，重构阈值 k=3
        List<BigInteger[]> shares = shamirService.splitSecret(secret, 5, 3);

        // 输出分片结果
        for (BigInteger[] share : shares) {
            System.out.println("Share: x=" + share[0] + ", y=" + share[1]);
        }

        // 选择任意3个分片进行重构
        List<BigInteger[]> selectedShares = shares.subList(0, 3);
        BigInteger reconstructed = shamirService.reconstructSecret(selectedShares);

        // 验证重构秘密
        assertEquals(secret, reconstructed);
    }
}

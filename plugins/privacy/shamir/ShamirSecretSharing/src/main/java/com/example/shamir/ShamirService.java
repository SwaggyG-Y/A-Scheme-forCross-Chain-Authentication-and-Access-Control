package main.java.com.example.shamir;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ShamirService {
    private static final SecureRandom random = new SecureRandom();

    // 数据分片方法
    public List<BigInteger[]> splitSecret(BigInteger secret, int n, int k) {
        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(secret);  // 常数项即秘密

        // 生成随机多项式系数
        for (int i = 1; i < k; i++) {
            coefficients.add(new BigInteger(256, random));
        }

        List<BigInteger[]> shares = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            BigInteger x = BigInteger.valueOf(i);
            BigInteger y = evaluatePolynomial(coefficients, x);
            shares.add(new BigInteger[]{x, y});
        }
        return shares;
    }

    // 计算多项式值
    private BigInteger evaluatePolynomial(List<BigInteger> coefficients, BigInteger x) {
        BigInteger y = BigInteger.ZERO;
        for (int i = 0; i < coefficients.size(); i++) {
            y = y.add(coefficients.get(i).multiply(x.pow(i)));
        }
        return y;
    }

    // 还原秘密
    public BigInteger reconstructSecret(List<BigInteger[]> shares) {
        BigInteger secret = BigInteger.ZERO;
        for (int i = 0; i < shares.size(); i++) {
            BigInteger[] share = shares.get(i);
            BigInteger xi = share[0];
            BigInteger yi = share[1];

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < shares.size(); j++) {
                if (i != j) {
                    BigInteger xj = shares.get(j)[0];
                    numerator = numerator.multiply(xj.negate());
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }
            secret = secret.add(yi.multiply(numerator).divide(denominator));
        }
        return secret;
    }
}

// 路径：src/main/java/org/fisco/bcos/sdk/demo/perf/PerformanceDIDRegistry.java
package org.fisco.bcos.sdk.demo.perf;

import org.fisco.bcos.sdk.demo.contract.CrossChainVerifier;
import org.fisco.bcos.sdk.demo.contract.DIDRegistry;
import org.fisco.bcos.sdk.demo.contract.DynamicPermission;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.response.AmopResponse;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PerformanceDIDRegistry {

    public static void main(String[] args) throws ContractException {
        // ============== 初始化配置 ==============
        BcosSDK sdk = BcosSDK.build("config-example.toml");
        Client client = sdk.getClient("group0");
        CryptoKeyPair mainAccount = client.getCryptoSuite().createKeyPair(); // 主DID账户

        // ============== 合约部署阶段 ==============
        // 部署DIDRegistry（需主DID账户）
        DIDRegistry didRegistry = DIDRegistry.deploy(client, mainAccount.getAddress());
        System.out.println("[DIDRegistry] 合约地址: " + didRegistry.getContractAddress());

        // 部署DynamicPermission（依赖DIDRegistry）
        DynamicPermission dynamicPermission = DynamicPermission.deploy(client, didRegistry.getContractAddress());
        System.out.println("[DynamicPermission] 合约地址: " + dynamicPermission.getContractAddress());

        // 部署CrossChainVerifier（依赖DIDRegistry）
        CrossChainVerifier crossChainVerifier = CrossChainVerifier.deploy(client, didRegistry.getContractAddress());
        System.out.println("[CrossChainVerifier] 合约地址: " + crossChainVerifier.getContractAddress());

        // ============== 测试用例 ==============
        testDIDLifecycle(client, didRegistry, dynamicPermission, crossChainVerifier, mainAccount);
        testPermissionControl(didRegistry, dynamicPermission, mainAccount);
    }

    private static void testDIDLifecycle(Client client, DIDRegistry didRegistry, 
            DynamicPermission dynamicPermission, CrossChainVerifier crossChainVerifier,
            CryptoKeyPair mainAccount) throws ContractException {
        
        System.out.println("\n===== 测试DID全生命周期 =====");
        
        // 生成测试用子DID
        CryptoKeyPair subAccount = client.getCryptoSuite().createKeyPair();
        String role = "farmer";
        bytes32 subDIDHash = calculateDIDHash(subAccount.getAddress());
        bytes32 pubKeyHash = calculatePubKeyHash(subAccount.getHexPublicKey());

        // 测试1: 注册子DID（主账户操作）
        TransactionReceipt receipt = didRegistry.registerSubDID(subDIDHash, pubKeyHash, role);
        assertSuccess(receipt, "注册子DID");
        
        // 验证注册结果
        String storedRole = didRegistry.getRole(subDIDHash);
        System.out.println("获取角色: " + storedRole + " (预期: farmer)");

        // 测试2: 跨链验证（模拟签名）
        String message = "TestMessage123";
        // 注意：实际应使用Ed25519签名，此处模拟签名数据
        boolean isValid = crossChainVerifier.verifyCrossChain(
                subDIDHash, 
                message.getBytes(), 
                message.getBytes()
        );
        System.out.println("跨链验证结果: " + isValid + " (预期: true)");

        // 测试3: 吊销子DID
        receipt = didRegistry.revokeSubDID(subDIDHash);
        assertSuccess(receipt, "吊销子DID");
        
        // 验证吊销状态
        try {
            didRegistry.verifyDID(subDIDHash, new byte[0], new byte[0]);
        } catch (Exception e) {
            System.out.println("吊销后验证应失败: " + e.getMessage());
        }
    }

    private static void testPermissionControl(DIDRegistry didRegistry, 
            DynamicPermission dynamicPermission, 
            CryptoKeyPair mainAccount) throws ContractException {
        
        System.out.println("\n===== 测试权限控制 =====");

        // 准备测试账户
        CryptoKeyPair farmerAccount = didRegistry.getClient().getCryptoSuite().createKeyPair();
        CryptoKeyPair regulatorAccount = didRegistry.getClient().getCryptoSuite().createKeyPair();

        // 注册farmer角色
        bytes32 farmerDID = calculateDIDHash(farmerAccount.getAddress());
        TransactionReceipt receipt = didRegistry.registerSubDID(
                farmerDID,
                calculatePubKeyHash(farmerAccount.getHexPublicKey()),
                "farmer"
        );
        assertSuccess(receipt, "注册farmer");

        // 注册regulator角色
        bytes32 regulatorDID = calculateDIDHash(regulatorAccount.getAddress());
        receipt = didRegistry.registerSubDID(
                regulatorDID,
                calculatePubKeyHash(regulatorAccount.getHexPublicKey()),
                "regulator"
        );
        assertSuccess(receipt, "注册regulator");

        // 测试1: farmer存储数据
        String cid = "QmXYZ...";
        receipt = dynamicPermission.storeData(farmerDID, cid);
        assertSuccess(receipt, "farmer存储数据");

        // 测试2: regulator查询数据
        String result = dynamicPermission.getData(regulatorDID);
        System.out.println("Regulator查询结果: " + result + " (预期: " + cid + ")");

        // 测试3: 非授权角色查询
        try {
            dynamicPermission.getData(farmerDID);
        } catch (Exception e) {
            System.out.println("Farmer查询应失败: " + e.getMessage());
        }
    }

    // ============== 工具方法 ==============
    private static bytes32 calculateDIDHash(String address) {
        return bytes32(sha3(address.getBytes()));
    }

    private static bytes32 calculatePubKeyHash(String hexPubKey) {
        return bytes32(sha3(hexPubKey.getBytes()));
    }

    private static void assertSuccess(TransactionReceipt receipt, String operation) {
        if (!receipt.isStatusOK()) {
            throw new RuntimeException(operation + "失败: " + receipt.getMessage());
        }
        System.out.println(operation + "成功");
    }
}
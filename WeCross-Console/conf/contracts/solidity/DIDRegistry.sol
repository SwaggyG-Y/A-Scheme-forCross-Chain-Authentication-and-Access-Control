// SPDX-License-Identifier: MIT
pragma solidity ^0.4.25;

import "./Ed25519.sol"; 

contract DIDRegistry {
    struct DIDInfo {
        address owner;          // 主DID持有者地址
        bytes32 publicKeyHash;  // 子DID公钥哈希（Ed25519公钥的哈希）
        string role;            // 角色（farmer, logistics, etc.）
        bool isRevoked;         // 是否吊销
    }
    
    mapping(bytes32 => DIDInfo) public dids;
    address public mainDIDOwner;

    constructor(address _mainDIDOwner) {
        mainDIDOwner = _mainDIDOwner;
    }

    function getRole(bytes32 subDIDHash) public view returns (string memory) {
        require(dids[subDIDHash].owner != address(0), "DID not exists");
        return dids[subDIDHash].role;
    }

    // 注册子DID（仅主DID持有者调用）
    function registerSubDID(bytes32 subDIDHash, bytes32 pubKeyHash, string memory role) public {
        require(msg.sender == mainDIDOwner, "Unauthorized");
        dids[subDIDHash] = DIDInfo(mainDIDOwner, pubKeyHash, role, false);
    }

    // 验证子DID有效性
    function verifyDID(
        bytes32 subDIDHash,
        bytes memory signature,
        bytes memory message
    ) public view returns (bool) {
        DIDInfo storage did = dids[subDIDHash];
        require(did.owner != address(0), "DID not exists"); // 存在性检查
        require(!did.isRevoked, "DID revoked");
        // 调用Ed25519库验证签名
        return Ed25519.verify(
            did.publicKeyHash,  // Ed25519公钥哈希
            message,            // 原始消息
            signature           // 签名
        );
    }

    // 吊销子DID
    function revokeSubDID(bytes32 subDIDHash) public {
        require(msg.sender == mainDIDOwner, "Unauthorized");
        require(dids[subDIDHash].owner != address(0), "DID not exists"); 
        dids[subDIDHash].isRevoked = true;
    }
}
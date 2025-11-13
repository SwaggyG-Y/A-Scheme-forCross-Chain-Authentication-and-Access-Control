// SPDX-License-Identifier: MIT
pragma solidity >=0.5.2 <0.8.0;

contract ZKPManagement {
    mapping(bytes32 => bytes32) private zkpProofs; // DID哈希 → IPFS哈希

    // 注册ZKP证明（需DID签名授权）
    function registerZKP(bytes32 didHash, bytes32 ipfsHash) public {
        zkpProofs[didHash] = ipfsHash;
    }

    // 验证ZKP证明
    function verifyZKP(bytes32 didHash, bytes32 ipfsHash) public view returns (bool) {
        return zkpProofs[didHash] == ipfsHash;
    }
}

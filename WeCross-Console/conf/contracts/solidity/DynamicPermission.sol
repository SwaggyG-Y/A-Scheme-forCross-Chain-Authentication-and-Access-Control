// SPDX-License-Identifier: MIT
pragma solidity ^0.4.25;
import "./DIDRegistry.sol";

contract DynamicPermission {
    DIDRegistry public didRegistry;
    mapping(bytes32 => string) private ipfsCIDs; // 数据哈希映射

    constructor(address _didRegistry) public {
        didRegistry = DIDRegistry(_didRegistry);
    }

    // 存储数据哈希（仅限特定角色）
    function storeData(bytes32 subDIDHash, string memory cid) public {
        string memory role = didRegistry.getRole(subDIDHash);
        require(keccak256(abi.encodePacked(role)) == keccak256(abi.encodePacked("farmer")), "Permission denied");
        ipfsCIDs[subDIDHash] = cid;
    }

    // 查询数据（根据角色返回不同结果）
    function getData(bytes32 subDIDHash) public view returns (string memory) {
        string memory role = didRegistry.getRole(subDIDHash);
        if (keccak256(abi.encodePacked(role)) == keccak256("regulator")) {
            return ipfsCIDs[subDIDHash]; // 返回完整IPFS CID
        } else {
            return ""; // 消费者仅返回空或哈希
        }
    }
}

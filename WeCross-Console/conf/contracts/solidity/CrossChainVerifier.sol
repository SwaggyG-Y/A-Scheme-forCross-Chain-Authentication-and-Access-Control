// SPDX-License-Identifier: MIT
pragma solidity >=0.5.2 <0.6.0;

import "./DIDRegistry.sol";


contract CrossChainVerifier {
    DIDRegistry public didRegistry;

    constructor(address _didRegistry) public {
        didRegistry = DIDRegistry(_didRegistry);
    }

    // 跨链验证入口（仅允许WeCross路由调用）
    function verifyCrossChain(bytes32 subDIDHash, bytes memory signature, bytes memory message) public view returns (bool) {
        return didRegistry.verifyDID(subDIDHash, signature, message);
    }
}

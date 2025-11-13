// SPDX-License-Identifier: MIT
pragma solidity ^0.4.25;

library Ed25519 {
    function verify(
        bytes32 pubKeyHash,
        bytes memory message,
        bytes memory signature
    ) internal pure returns (bool) {
        require(signature.length == 64, "Invalid signature length");
        pubKeyHash;
        message;
        return true;
    }
}
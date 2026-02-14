#include <iostream>
#include <vector>
#include <sodium.h>
#include "password_generator.h"
#include "random_utils.h"
#include "crypto_utils.h"

int main() {
    if (!crypto_init()) {
        std::cout << "Crypto init failed\n";
        return 1;
    }

    std::string password = "test_master_password";

    std::vector<unsigned char> salt(crypto_pwhash_SALTBYTES);
    randombytes_buf(salt.data(), salt.size());

    std::vector<unsigned char> key;
    if (!derive_key(password, salt, key)) {
        std::cout << "Key derivation failed\n";
        return 1;
    }

    std::string message = "HELLO_VAULT";

    std::vector<unsigned char> nonce;
    std::vector<unsigned char> ciphertext;

    if (!encrypt_string(message, key, nonce, ciphertext)) {
        std::cout << "Encryption failed\n";
        return 1;
    }

    std::string decrypted;

    if (!decrypt_string(ciphertext, nonce, key, decrypted)) {
        std::cout << "Decryption failed\n";
        return 1;
    }

    std::cout << "Original: " << message << "\n";
    std::cout << "Decrypted: " << decrypted << "\n";

    return 0;
}


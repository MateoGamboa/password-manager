#ifndef CRYPTO_UTILS_H
#define CRYPTO_UTILS_H

#include <string>
#include <vector>

bool crypto_init();

bool derive_key(
    const std::string& password,
    const std::vector<unsigned char>& salt,
    std::vector<unsigned char>& key_out
);

bool encrypt_string(
    const std::string& plaintext,
    const std::vector<unsigned char>& key,
    std::vector<unsigned char>& nonce_out,
    std::vector<unsigned char>& ciphertext_out
);

bool decrypt_string(
    const std::vector<unsigned char>& ciphertext,
    const std::vector<unsigned char>& nonce,
    const std::vector<unsigned char>& key,
    std::string& plaintext_out
);

#endif
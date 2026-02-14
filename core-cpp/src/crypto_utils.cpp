#include "crypto_utils.h"
#include <sodium.h>

bool crypto_init(){
    return sodium_init() >= 0;
}

bool derive_key(
    const std::string& password,
    const std::vector<unsigned char>& salt,
    std::vector<unsigned char>& key_out
){
    if (password.empty()){
        return false;
    }

    if (salt.size() != crypto_pwhash_SALTBYTES){
        return false;
    }

    key_out.resize(crypto_secretbox_KEYBYTES);

    return crypto_pwhash(
        key_out.data(),
        key_out.size(),
        password.c_str(),
        password.size(),
        salt.data(),
        crypto_pwhash_OPSLIMIT_INTERACTIVE,
        crypto_pwhash_MEMLIMIT_INTERACTIVE,
        crypto_pwhash_ALG_DEFAULT
    ) == 0;
}

bool encrypt_string(
    const std::string& plaintext,
    const std::vector<unsigned char>& key,
    std:: vector<unsigned char>& nonce_out,
    std:: vector<unsigned char>& ciphertext_out 
){
    if (key.size() != crypto_secretbox_KEYBYTES){
        return false;
    }

    nonce_out.resize(crypto_secretbox_NONCEBYTES);
    randombytes_buf(nonce_out.data(), nonce_out.size());

    ciphertext_out.resize(
        plaintext.size() + crypto_secretbox_MACBYTES
    );

    return crypto_secretbox_easy(
        ciphertext_out.data(),
        reinterpret_cast<const unsigned char*>(plaintext.data()),
        plaintext.size(),
        nonce_out.data(),
        key.data()
    ) == 0;
}

bool decrypt_string(
    const std::vector<unsigned char>& ciphertext,
    const std::vector<unsigned char>& nonce,
    const std::vector<unsigned char>& key,
    std::string& plaintext_out
){
    if (key.size() != crypto_secretbox_KEYBYTES){
        return false;
    }

    if (nonce.size() != crypto_secretbox_NONCEBYTES){
        return false;
    }

    if (ciphertext.size() < crypto_secretbox_MACBYTES){
        return false;
    }

    std::vector<unsigned char> decrypted(
        ciphertext.size() - crypto_secretbox_MACBYTES 
    );

    if (crypto_secretbox_open_easy(
        decrypted.data(),
        ciphertext.data(),
        ciphertext.size(),
        nonce.data(),
        key.data()) != 0){
            return false;
        }

        plaintext_out = std::string(
            reinterpret_cast<char*>(decrypted.data()),
            decrypted.size()
    );

    return true;
}





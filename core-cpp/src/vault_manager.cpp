#include "vault_manager.h"
#include "crypto_utils.h"
#include <sodium.h>

VaultManager::VaultManager()
    : unlocked_(false)
{    
}

bool VaultManager::create_vault(const std::string& master_password){
    if (master_password.empty()){
        return false;
    }

    salt_.resize(crypto_pwhash_SALTBYTES);
    randombytes_buf(salt_.data(), salt_.size());

    if (!derive_key(master_password, salt_, current_key_)){
        return false;
    }

    std::string verification = "VAULT_OK"
;

if (!encrypt_string(
    verification, 
    current_key_,
    verification_nonce_,
    verification_ciphertext_)){
        return false;
    }

    unlocked_ = true;

    return true;
}

bool VaultManager::unlock_vault(const std::string& master_password){
    if (master_password.empty()){
        return false;
    }

    std::vector<unsigned char> derived_key;

    if (!derive_key(master_password, salt_, derived_key)){

        return false;
    }

    std::string decrypted;

    if(!decrypt_string(
        verification_ciphertext_,
        verification_nonce_,
        derived_key,
        decrypted)){
            return false;
        }

        if (decrypted != "VAULT_OK"){
            return false;
        }

        current_key_ = derived_key;
        unlocked_ = true;

        return true;
}

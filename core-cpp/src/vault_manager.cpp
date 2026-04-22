#include "vault_manager.h"
#include "crypto_utils.h"
#include <sodium.h>

VaultManager::VaultManager(DatabaseManager* db)
    : db_(db), unlocked_(false)
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

    VaultMetadata metadata;
    metadata.salt = salt_;
    metadata.verification_nonce = verification_nonce_;
    metadata.verification_ciphertext = verification_ciphertext_;

    if (!db_ ->save_vault_metadata(metadata)){
        return false;
    }

    unlocked_ = true;

    return true;
}

bool VaultManager::unlock_vault(const std::string& master_password){
    if (master_password.empty()){
        return false;
    }

    VaultMetadata metadata;

    if (!db_->load_vault_metadata(metadata)){
        return false;
    }

    salt_ = metadata.salt;
    verification_nonce_ = metadata.verification_nonce;
    verification_nonce_ = metadata.verification_nonce;
    verification_ciphertext_ = metadata.verification_ciphertext;

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

bool VaultManager::add_password(
    const std::string& service,
    const std::string& username,
    const std::string& password
){
    if (!unlocked_){
        return false;
    }

    std::vector<unsigned char> nonce;
    std::vector<unsigned char> ciphertext;

    if (!encrypt_string(password, current_key_, nonce, ciphertext)){
        return false;
    }

    return db_->add_password_entry(service, username, nonce, ciphertext);
}

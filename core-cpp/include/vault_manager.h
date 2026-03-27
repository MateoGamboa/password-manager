#ifndef VAULT_MANAGER_H
#define VAULT_MANAGER_H

#include <string>
#include <vector>

#include "database_manager.h"

class VaultManager{
    public:
        VaultManager(DatabaseManager* db);

        bool create_vault(const std::string& master_password);
        bool unlock_vault(const std::string& master_password);

        bool is_unlocked() const;

    private:
        std::vector<unsigned char> salt_;
        std::vector<unsigned char> verification_nonce_;
        std::vector<unsigned char> verification_ciphertext_;
        std::vector<unsigned char> current_key_;

        bool unlocked_;

        DatabaseManager* db_;
        
};

#endif
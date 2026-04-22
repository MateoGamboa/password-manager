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

        bool add_password(
            const std::string& service,
            const std::string& username,
            const std::string& password
        );

        bool get_passwords(std::vector<std::tuple<std::string, std::string, std::string>>& output);

        bool delete_password(int id);

    private:
        std::vector<unsigned char> salt_;
        std::vector<unsigned char> verification_nonce_;
        std::vector<unsigned char> verification_ciphertext_;
        std::vector<unsigned char> current_key_;

        bool unlocked_;

        DatabaseManager* db_;
        
};

#endif
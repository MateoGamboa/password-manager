#ifndef DATABAE_MANAGER_H
#define DATABASE_MANAGER_H

#include <string>
#include <vector>

struct VaultMetadata {
    std::vector<unsigned char> salt;
    std::vector<unsigned char> verification_nonce;
    std::vector<unsigned char> verification_ciphertext;
};

class DatabaseManager {
    public:
        DatabaseManager(const std::string& db_path);
        ~DatabaseManager();

        bool initialize();

        bool save_vault_metadata(const VaultMetadata& metadata_out);
        bool load_vault_metadata(VaultMetadata& metadata_out);

    private:
        std::string db_path_;
        void* db_;
};   

#endif
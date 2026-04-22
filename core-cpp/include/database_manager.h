#ifndef DATABASE_MANAGER_H
#define DATABASE_MANAGER_H

#include <string>
#include <vector>

struct VaultMetadata {
    std::vector<unsigned char> salt;
    std::vector<unsigned char> verification_nonce;
    std::vector<unsigned char> verification_ciphertext;
};

struct PasswordEntry {
    int id;
    std::string service;
    std::string username;
    std::vector<unsigned char> nonce;
    std::vector<unsigned char> ciphertext;
};

class DatabaseManager {
    public:
        DatabaseManager(const std::string& db_path);
        ~DatabaseManager();

        bool initialize();

        bool save_vault_metadata(const VaultMetadata& metadata_out);
        bool load_vault_metadata(VaultMetadata& metadata_out);

        bool add_password_entry(
            const std::string& service,
            const std::string& username,
            const std::vector<unsigned char>& nonce,
            const std::vector<unsigned char>& ciphertext
        );

        bool get_all_password_entries(std::vector<PasswordEntry>& entries_out);

        bool delete_password_entry(int id);


    private:
        std::string db_path_;
        void* db_;
};   

#endif
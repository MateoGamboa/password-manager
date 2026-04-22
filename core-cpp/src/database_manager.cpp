#include "database_manager.h"
#include <sqlite3.h>
#include <iostream>

DatabaseManager::DatabaseManager(const std::string& db_path)
    :db_path_(db_path), db_(nullptr)
{
}

DatabaseManager::~DatabaseManager(){
    if (db_ != nullptr){
        sqlite3_close(static_cast<sqlite3*>(db_));
    }
}

bool DatabaseManager::initialize(){
    sqlite3* raw_db = nullptr;

    if(sqlite3_open(db_path_.c_str(), &raw_db) != SQLITE_OK){
        std::cerr << "Failed to open database\n";
        return false;
    }

    db_ = raw_db;

    char* error_message = nullptr;

    const char* create_table_sql = 
        "CREATE TABLE IF NOT EXISTS vault_metadata ("
        "id INTEGER PRIMARY KEY,"
        "salt BLOB NOT NULL,"
        "verification_nonce BLOB NOT NULL,"
        "verification_ciphertext BLOB NOT NULL"
        ");";

    if(sqlite3_exec(
            static_cast<sqlite3*>(db_),
            create_table_sql,
            nullptr,
            nullptr,
            &error_message) != SQLITE_OK){
    
        std::cerr <<"Failed to create table: "
                  <<error_message << "\n";

        sqlite3_free(error_message);
        return false;
    }

    const char* create_entries_sql = 
        "CREATE TABLE IF NOT EXISTS password_entries ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "service TEXT NOT NULL,"
        "username TEXT NOT NULL,"
        "nonce BLOB NOT NULL,"
        "ciphertext BLOB NOT NULL"
        ");";

        if (sqlite3_exec(
            static_cast<sqlite3*>(db_),
            create_entries_sql,
            nullptr,
            nullptr,
            &error_message) != SQLITE_OK)
        {
            std::cerr << "Failed to ceate password_entries table: "
                    << error_message << "\n";  
            sqlite3_free(error_message);
            return false;
        }    

        return true;
}

bool DatabaseManager::save_vault_metadata(const VaultMetadata& metadata){

    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* delete_sql = "DELETE FROM vault_metadata";

    if(sqlite3_exec(raw_db, delete_sql, nullptr, nullptr, nullptr) != SQLITE_OK){
        return false;
    }

    const char* insert_sql = 
        "INSERT INTO vault_metadata "
        "(salt, verification_nonce, verification_ciphertext) "
        "VALUES (?, ?, ?);";

    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(raw_db, insert_sql, -1, &stmt, nullptr) != SQLITE_OK){
        return false;
    } 

    sqlite3_bind_blob(stmt, 1, 
        metadata.salt.data(),
        metadata.salt.size(),
        SQLITE_STATIC);

    sqlite3_bind_blob(stmt, 2, 
        metadata.verification_nonce.data(),
        metadata.verification_nonce.size(),
        SQLITE_STATIC);

    sqlite3_bind_blob(stmt, 3, 
        metadata.verification_ciphertext.data(),
        metadata.verification_ciphertext.size(),
        SQLITE_STATIC);

    if(sqlite3_step(stmt) != SQLITE_DONE){
        sqlite3_finalize(stmt);
        return false;
    }

    sqlite3_finalize(stmt);
        return true;
}

bool DatabaseManager::load_vault_metadata(VaultMetadata& metadata_out){

    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* select_sql = 
        "SELECT salt, verification_nonce, verification_ciphertext "
        "FROM vault_metadata LIMIT 1;";

    sqlite3_stmt* stmt = nullptr;

    if(sqlite3_prepare_v2(raw_db, select_sql, -1, &stmt, nullptr) != SQLITE_OK){
        return false;
    }

    int step_result = sqlite3_step(stmt);

    if (step_result != SQLITE_ROW){
        sqlite3_finalize(stmt);
        return false;
    }

     const unsigned char* salt_ptr =
        static_cast<const unsigned char*>(sqlite3_column_blob(stmt, 0));
    int salt_size = sqlite3_column_bytes(stmt, 0);

    metadata_out.salt.assign(salt_ptr, salt_ptr + salt_size);

    const unsigned char* nonce_ptr =
        static_cast<const unsigned char*>(sqlite3_column_blob(stmt, 1));
    int nonce_size = sqlite3_column_bytes(stmt, 1);

    metadata_out.verification_nonce.assign(nonce_ptr, nonce_ptr + nonce_size);

    const unsigned char* cipher_ptr =
        static_cast<const unsigned char*>(sqlite3_column_blob(stmt, 2));
    int cipher_size = sqlite3_column_bytes(stmt, 2);

    metadata_out.verification_ciphertext.assign(cipher_ptr, cipher_ptr + cipher_size);

    sqlite3_finalize(stmt);
    
    return true;
}

bool DatabaseManager::add_password_entry(
    const std::string& service,
    const std::string& username,
    const std::vector<unsigned char>& nonce,
    const std::vector<unsigned char>& ciphertext
) {
    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* insert_sql = 
        "INSERT INTO password_entries "
        "(service, username, nonce, ciphertext) "
        "VALUES (?, ?, ?, ?);";

    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(raw_db, insert_sql, -1, &stmt, nullptr) != SQLITE_OK){
        std::cerr << "Prepare failed: " << sqlite3_errmsg(raw_db) << "\n";
        return false;
    }

    //Bind text
    sqlite3_bind_text(stmt, 1, service.c_str(), -1, SQLITE_STATIC);
    sqlite3_bind_text(stmt, 2, username.c_str(), -1, SQLITE_STATIC);

    //Bind binary
    sqlite3_bind_blob(stmt, 3, nonce.data(), nonce.size(), SQLITE_STATIC);
    sqlite3_bind_blob(stmt, 4, ciphertext.data(), ciphertext.size(), SQLITE_STATIC);

    if (sqlite3_step(stmt) != SQLITE_DONE){
        std::cerr << "Insert failed: " << sqlite3_errmsg(raw_db) << "\n";
        sqlite3_finalize(stmt);
        return false;
    }

    sqlite3_finalize(stmt);
    return true;
}

bool DatabaseManager::get_all_password_entries(std::vector<PasswordEntry>& entries_out){
    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* select_sql = 
        "SELECT id, service, username, nonce, ciphertext FROM password_entries;";

    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(raw_db, select_sql, -1, &stmt, nullptr) != SQLITE_OK){
        return false;
    }

    while (sqlite3_step(stmt) == SQLITE_ROW){
        PasswordEntry entry;

        entry.id = sqlite3_column_int(stmt, 0);

        entry.service = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
        entry.username = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));

        const unsigned char* nonce_ptr = 
            static_cast<const unsigned char*>(sqlite3_column_blob(stmt, 3));
        int nonce_size = sqlite3_column_bytes(stmt, 3);
        entry.nonce.assign(nonce_ptr, nonce_ptr + nonce_size);

        const unsigned char* cipher_ptr =
            static_cast<const unsigned char*>(sqlite3_column_blob(stmt, 4));
        int cipher_size = sqlite3_column_bytes(stmt, 4);
        entry.ciphertext.assign(cipher_ptr, cipher_ptr + cipher_size);

        entries_out.push_back(entry);
    }

    sqlite3_finalize(stmt);
    return true;
}

bool DatabaseManager::delete_password_entry(int id){
    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* delete_sql = 
        "DELETE FROM password_entries WHERE id = ?;";

    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(raw_db, delete_sql, -1, &stmt, nullptr) != SQLITE_OK){
        std::cerr << "Prepare failed: " << sqlite3_errmsg(raw_db) << "\n";
        return false;
    }

    sqlite3_bind_int(stmt, 1, id);

    if (sqlite3_step(stmt) != SQLITE_DONE){
        std::cerr << "Delete failed: " << sqlite3_errmsg(raw_db) << "\n";
        sqlite3_finalize(stmt);
        return false;
    }

    sqlite3_finalize(stmt);
    return true;
}

bool DatabaseManager::update_password_entry(
    int id,
    const std::vector<unsigned char>& nonce,
    const std::vector<unsigned char>& ciphertext
) {
    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* update_sql = 
        "UPDATE password_entries "
        "SET nonce = ?, ciphertext = ? "
        "WHERE id = ?;";

    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(raw_db, update_sql, -1, &stmt, nullptr) !=  SQLITE_OK){
        std::cerr <<"Prepare failed: " << sqlite3_errmsg(raw_db) << "\n";
        return false;
    }

    sqlite3_bind_blob(stmt, 1, nonce.data(), nonce.size(), SQLITE_STATIC);
    sqlite3_bind_blob(stmt, 2, ciphertext.data(), ciphertext.size(), SQLITE_STATIC);
    sqlite3_bind_int(stmt, 3, id);

    if (sqlite3_step(stmt) != SQLITE_DONE){
        std::cerr << "Update failed: " << sqlite3_errmsg(raw_db) << "\n";
        return false;
    }

    sqlite3_finalize(stmt);
    return true;
}
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

    const char* create_table_sql = 
        "CREATE TABLE IF NOT EXISTS vault_metadata ("
        "id INTEGER PRIMARY KEY,"
        "salt BLOB NOT NULL,"
        "verification_nonce BLOB NOT NULL,"
        "verification_ciphertext BLOB NOT NULL"
        ");";

    char* error_message = nullptr;

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

    return true;

}

bool DatabaseManager::save_vault_metadata(const VaultMetadata& metadata){

    sqlite3* raw_db = static_cast<sqlite3*>(db_);

    const char* delete_sql = "DELETE FROM vault_metadata";

    if(sqlite3_exec(raw_db, delete_sql, nullptr, nullptr, nullptr) != SQLITE_OK){
        return false;
    }

    const char* insert_sql = 
        "INSERT INTO vault_metadata"
        "(salt, verification_nonce, verification_ciphertext)"
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
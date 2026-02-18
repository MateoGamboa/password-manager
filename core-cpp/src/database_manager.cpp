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
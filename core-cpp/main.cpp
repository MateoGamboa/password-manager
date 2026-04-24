#include "database_manager.h"
#include "vault_manager.h"
#include "crypto_utils.h"
#include <iostream>
#include <vector>
#include <tuple>

int main(int argc, char* argv[]) {

    if (!crypto_init()) {
        std::cout << "Crypto init failed\n";
        return 1;
    }

    if (argc < 2){
        std::cout << "ERROR: no command provided.";
        return 1;
    }

    std::string command = argv[1];

    DatabaseManager db("vault.db");

    if (!db.initialize()) {
        std::cout << "Database init failed\n";
        return 1;
    }

    VaultManager vault(&db);

    if (command == "create"){
        if (argc < 3){
            std::cout << "ERROR: missing password\n";
            return 1;
        }

        std::string password = argv[2];

        if (!vault.create_vault(password)){
            std::cout << "ERROR: vault creation failed\n";
            return 1;
        }

        std::cout << "SUCCESS\n";
        return 0;
    }

    if (command == "unlock"){
        if (argc < 3){
            std::cout << "ERROR: missing password\n";
            return 1;
        }

        std::string password = argv[2];

        if (!vault.unlock_vault(password)){
            std::cout << "ERROR: unlock failed\n";
            return 1;
        }

        std::cout << "SUCCESS\n";
        return 0;
    }

    if (command == "add"){
        if (argc < 6){
            std::cout << "ERROR: missing arguments\n";
            return 1;
        }

        std::string password = argv[2];
        std::string service = argv[3];
        std::string username = argv[4];
        std::string value = argv[5];
        
        if (!vault.unlock_vault(password)) {
            std::cout << "ERROR: unlock failed\n";
            return 1;
        }

        if (!vault.add_password(service, username, value)) {
            std::cout << "ERROR: add failed\n";
            return 1;
        }

        std::cout << "SUCCESS\n";
        return 0;
    }

    if (command == "get") {
        if (argc < 3) {
            std::cout << "ERROR: missing password\n";
            return 1;
        }

        std::string password = argv[2];

        if (!vault.unlock_vault(password)) {
            std::cout << "ERROR: unlock failed\n";
            return 1;
        }

        std::vector<std::tuple<std::string, std::string, std::string>> entries;

        if (!vault.get_passwords(entries)) {
            std::cout << "ERROR: get failed\n";
            return 1;
        }
        
        std::cout << "SUCCESS\n";
        
        for (const auto& [service, username, pass] : entries) {
            std::cout << service << "|" << username << "|" << pass << "\n";
        }

        return 0;
    }

    std::cout << "ERROR: unknown command\n";

    return 1;
}
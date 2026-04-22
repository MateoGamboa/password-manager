#include "database_manager.h"
#include "vault_manager.h"
#include "crypto_utils.h"
#include <iostream>
#include <vector>
#include <tuple>

int main() {

    if (!crypto_init()) {
        std::cout << "Crypto init failed\n";
        return 1;
    }

    DatabaseManager db("vault.db");

    if (!db.initialize()) {
        std::cout << "Database init failed\n";
        return 1;
    }

    VaultManager vault(&db);

    // 🔹 Create vault
    if (!vault.create_vault("test_password")) {
        std::cout << "Vault creation failed\n";
        return 1;
    }

    std::cout << "Vault created\n";

    // 🔹 Unlock vault
    if (!vault.unlock_vault("test_password")) {
        std::cout << "Unlock failed\n";
        return 1;
    }

    std::cout << "Vault unlocked\n";

    // 🔹 Add password
    if (!vault.add_password("Gmail", "mateo@gmail.com", "mypassword123")) {
        std::cout << "Failed to add password\n";
        return 1;
    }

    std::cout << "Password added\n";

    // 🔹 Retrieve passwords
    std::vector<std::tuple<std::string, std::string, std::string>> passwords;

    if (!vault.get_passwords(passwords)) {
        std::cout << "Failed to retrieve passwords\n";
        return 1;
    }

    std::cout << "\nStored Passwords:\n";

    for (const auto& [service, username, password] : passwords) {
        std::cout << "Service: " << service << "\n";
        std::cout << "Username: " << username << "\n";
        std::cout << "Password: " << password << "\n\n";
    }

    return 0;
}

#include "database_manager.h"
#include "vault_manager.h"
#include "crypto_utils.h"
#include <iostream>

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

    std::cout << "Backend initialized successfully\n";

    return 0;
}
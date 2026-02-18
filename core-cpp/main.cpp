#include "vault_manager.h"
#include "crypto_utils.h"
#include "database_manager.h"
#include <iostream>

int main() {
    // if (!crypto_init()) {
    //     std::cout << "Crypto init failed\n";
    //     return 1;
    // }

    // VaultManager vault;

    // std::cout << "Creating vault...\n";
    // if (!vault.create_vault("correct_password")) {
    //     std::cout << "Vault creation failed\n";
    //     return 1;
    // }

    // std::cout << "Attempting correct unlock...\n";
    // if (vault.unlock_vault("correct_password")) {
    //     std::cout << "Correct password accepted\n";
    // } else {
    //     std::cout << "Correct password rejected\n";
    // }

    // std::cout << "Attempting wrong unlock...\n";
    // if (vault.unlock_vault("wrong_password")) {
    //     std::cout << "Wrong password accepted (ERROR)\n";
    // } else {
    //     std::cout << "Wrong password rejected (GOOD)\n";
    // }

    // return 0;

    DatabaseManager db("vault.db");

if (!db.initialize()) {
    std::cout << "DB init failed\n";
} else {
    std::cout << "DB initialized\n";
}

return 0;

}

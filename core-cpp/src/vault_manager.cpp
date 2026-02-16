#include "vault_manager.h"
#include "crypto_utils.h"
#include <sodium.h>

VaultManager::VaultManager()
    : unlocked_(false)
{    
}
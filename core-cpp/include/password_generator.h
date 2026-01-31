#ifndef PASSWORD_GENERATOR_H
#define PASSWORD_GENERATOR_H

#include <string>

std::string generate_password(
    int length,
    bool use_letters, 
    bool use_numbers,
    bool use_symbols
);

#endif
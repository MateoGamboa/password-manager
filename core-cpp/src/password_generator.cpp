#include "password_generator.h"
#include "random_utils.h"
#include <string>

const std::string LETTERS =     
    "abcdefghijkmnopqrstuvwxyz"
    "ABCDEFGHJKLMNPQRSTUVWXYZ";
const std::string NUMBERS = "23456789";
const std::string SYMBOLS = "!@#$%^&*()-_=+";

std::string generate_password(
    int length, bool use_letters, bool use_numbers, bool use_symbols
){
    std::string allowed = "";
    std::string password = "";

    if (use_letters){
        allowed+=LETTERS;
    }
    if (use_numbers){
        allowed+=NUMBERS;
    }

    if (use_symbols){
        allowed+=SYMBOLS;
    }

    for (int i = 0; i < length; i++){
        password += rand_char(allowed);
    }

    return password;
}


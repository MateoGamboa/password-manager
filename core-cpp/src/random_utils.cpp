#include "random_utils.h"
#include <random>
#include <string>

int rand_int(int min, int max){
    static std::mt19937 engine(std::random_device{}());
    std::uniform_int_distribution<int> dist(min, max);
    return dist(engine);
}

char rand_char(const std::string& charset){
    int index = rand_int(0, charset.length() - 1);
    return charset[index];
}
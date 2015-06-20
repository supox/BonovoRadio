#pragma once

class Logger {
public:
    static void Debug(const char* log);
    static void Error(const char* log);
    static void Verbose(const char* log);
};
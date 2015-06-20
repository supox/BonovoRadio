#include "Logger.h"
#include <android/log.h>

void Logger::Debug(const char* log) {
  __android_log_write(ANDROID_LOG_DEBUG, "JNI", log);
}

void Logger::Error(const char* log) {
  __android_log_write(ANDROID_LOG_ERROR, "JNI", log);
}

void Logger::Verbose(const char* log) {
  __android_log_write(ANDROID_LOG_VERBOSE, "JNI", log);
}

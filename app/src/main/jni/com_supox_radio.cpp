#include <stddef.h>
#include "jni.h"
#include "IRadio.h"

static const char *classPathName = "com/supox/bonovoradio/api/NativeRadio";
static IRadio* s_radio = IRadio::getRadio();

void jni_setState(JNIEnv *env, jobject thiz, jboolean state) {
  s_radio->setState(state ? IRadio::START : IRadio::STOP);
}

jboolean jni_getState(JNIEnv *env, jobject thiz) {
  return s_radio->getState() == IRadio::START;
}

jint jni_getFrequency(JNIEnv *env, jobject thiz) {
  return s_radio->getFrequency();
}

void jni_setFrequency(JNIEnv *env, jobject thiz, jint freq) {
  s_radio->setFrequency(freq);
}

jint jni_getBand(JNIEnv *env, jobject thiz) {
  return static_cast<jint>(s_radio->getBand());
}

void jni_setBand(JNIEnv *env, jobject thiz, jint band) {
  s_radio->setBand(static_cast<IRadio::Band>(band));
}

jint jni_getSeekState(JNIEnv *env, jobject thiz) {
  return s_radio->getSeekState();
}

void jni_setSeekState(JNIEnv *env, jobject thiz, jint state) {
  s_radio->setSeekState(state);
}

jboolean jni_getRDSState(JNIEnv *env, jobject thiz) {
  return s_radio->getRDSState() == IRadio::START;
}

jint jni_readRDSBuffer(JNIEnv* env, jobject thiz) {
	return s_radio->readRDS();
}

void jni_setRDSState(JNIEnv *env, jobject thiz, jboolean state) {
  s_radio->setRDSState(state ? IRadio::START : IRadio::STOP);
}

jint jni_getVolume(JNIEnv *env, jobject thiz) { return s_radio->getVolume(); }
void jni_setVolume(JNIEnv *env, jobject thiz, jint volume) {
  s_radio->setVolume(volume);
}

static JNINativeMethod methods[] = {
    {"setState", "(Z)V", (void *)jni_setState},
    {"getState", "()Z", (void *)jni_getState},

    {"getFrequency", "()I", (void *)jni_getFrequency},
    {"setFrequency", "(I)V", (void *)jni_setFrequency},
    {"getBand", "()I", (void *)jni_getBand},
    {"setBand", "(I)V", (void *)jni_setBand},
    {"getSeekState", "()I", (void *)jni_getSeekState},
    {"setSeekState", "(I)V", (void *)jni_setSeekState},

    {"getRDSState", "()Z", (void *)jni_getRDSState},
    {"setRDSState", "(Z)V", (void *)jni_setRDSState},
    {"readRDS", "()I", (void *)jni_readRDSBuffer},

    {"getVolume", "()I", (void *)jni_getVolume},
    {"setVolume", "(I)V", (void *)jni_setVolume},
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *methods, int numMethods) {
  jclass clazz = env->FindClass(className);
  if (clazz == NULL) {
    return JNI_FALSE;
  }

  if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv *env) {
  return registerNativeMethods(env, classPathName, methods,
                             sizeof(methods) / sizeof(methods[0]));
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = NULL;
  if (vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }

  if (registerNatives(env) != JNI_TRUE) {
    return -1;
  }

  return JNI_VERSION_1_6;
}


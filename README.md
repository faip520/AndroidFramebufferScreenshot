AndroidFramebufferScreenshot
============================

Use this command to build the so file to use Arm Neon Assembly:
  ndk-build APP_ABI=armeabi-v7a LOCAL_ARM_MODE=arm LOCAL_ARM_NEON=true ARCH_ARM_HAVE_NEON=true

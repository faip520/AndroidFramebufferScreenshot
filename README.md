AndroidFramebufferScreenshot
============================
Take screenshot in jni, return byte array to java, decode and display in ImageView.

Only works in rooted devices!



Performance
============================
Take a 1280 * 720 jpeg from framebuffer cost about 150 - 180 ms (done with turbo-jpeg!).




Build jni
============================
Use this command to build the so file to use Arm Neon Assembly:
  ndk-build APP_ABI=armeabi-v7a LOCAL_ARM_MODE=arm LOCAL_ARM_NEON=true ARCH_ARM_HAVE_NEON=true

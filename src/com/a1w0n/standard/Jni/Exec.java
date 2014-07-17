package com.a1w0n.standard.Jni;

public class Exec
{
    static {
        System.loadLibrary("a1w0n");
    }

    public static native int test(byte[] data);
}


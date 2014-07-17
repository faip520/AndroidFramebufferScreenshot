#include "common.h"

#define LOG_TAG "termExec"

#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <termios.h>
#include <signal.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <jni.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <linux/kd.h>
#include <linux/fb.h>

#include <sys/mman.h>
#include <sys/ioctl.h>
#include <sys/types.h>

#include <asm/page.h>

#include "termExec.h"
#include "turbojpeg.h"

static int android_os_Exec_test(JNIEnv *env, jobject clazz, jbyteArray ba) {
	/*
	 * 像素偏移量计算公式
	 * location=(x+vinfo.xoffset)*(vinfo.bits_per_pixel/8)+
	 *	                    (y+vinfo.yoffset)*finfo.line_length;
	 */
	int fd, ret, byte_per_frame;
	unsigned long	jpegSize = 0;
	// 映射 /dev/graphics/fb0 内存地址
	void	*framebuffer_memory;
	// 转换成 jpeg 数据后的地址
	unsigned char* jpeg_data = NULL;

	unsigned char* result = NULL;

	bgra8888_t *fb_pixels = NULL;

	static struct fb_var_screeninfo vinfo;
	struct fb_fix_screeninfo finfo;
	// turbo-jpeg 实例
	tjhandle	 handle;

	fd = open("/dev/graphics/fb0", O_RDONLY);

	if(fd < 0)
	{
		LOGD("======Cannot open /dev/graphics/fb0!");
		return -1;
	}

	// variable info 可变信息
	ret = ioctl(fd, FBIOGET_VSCREENINFO, &vinfo);
	if(ret < 0 )
	{
		LOGD("======Cannot get variable screen information.");
		close(fd);
		return -1;
	}

	// 这两个是显示在显示屏上时候的分辨率
	LOGD("====== xres : %d",  vinfo.xres);
	LOGD("====== yres : %d",  vinfo.yres);
	// 这两个是显存缓存的分辨率 如果显存缓存了两个屏幕的时候
	// yres_virtula 应该等于 yres * 2
	// 而 xres_virtual 就应该 == xres
	LOGD("====== xres_virtual : %d",  vinfo.xres_virtual);
	LOGD("====== yres_virtual : %d",  vinfo.yres_virtual);

	/* offset from virtual to visible */
	// 显存可能缓存了多个屏幕，哪到底哪个屏幕才是显示屏应该显示的内容呢
	// 这就是由下面这两个offset来决定了
	LOGD("====== xoffset : %d",  vinfo.xoffset);
	LOGD("====== yoffset : %d",  vinfo.yoffset);

	LOGD("====== bits_per_pixel : %d",  vinfo.bits_per_pixel);

	// 下面这一段是每个像素点的格式
	LOGD("====== fb_bitfield red.offset : %d",  vinfo.red.offset);
	LOGD("====== fb_bitfield red.length : %d",  vinfo.red.length);
	// 如果 == 0，指的是数据的最高有效位在最左边 也就是Big endian
	LOGD("====== fb_bitfield red.msb_right : %d",  vinfo.red.msb_right);
	LOGD("====== fb_bitfield green.offset : %d",  vinfo.green.offset);
	LOGD("====== fb_bitfield green.length : %d",  vinfo.green.length);
	LOGD("====== fb_bitfield green.msb_right : %d",  vinfo.green.msb_right);
	LOGD("====== fb_bitfield blue.offset : %d",  vinfo.blue.offset);
	LOGD("====== fb_bitfield blue.length : %d",  vinfo.blue.length);
	LOGD("====== fb_bitfield blue.msb_right : %d",  vinfo.blue.msb_right);
	LOGD("====== fb_bitfield transp.offset : %d",  vinfo.transp.offset);
	LOGD("====== fb_bitfield transp.length : %d",  vinfo.transp.length);
	LOGD("====== fb_bitfield transp.msb_right : %d",  vinfo.transp.msb_right);

	LOGD("====== height : %d",  vinfo.height);
	// width of picture in mm 毫米
	LOGD("====== width : %d",  vinfo.width);
	// UP 0
	// CW 1
	// UD 2
	// CCW 3
	/* angle we rotate counter clockwise */
	LOGD("====== rotate : %d",  vinfo.rotate);

	// fixed info 不变信息
	ret = ioctl(fd, FBIOGET_FSCREENINFO, &finfo);
	if(ret < 0 )
	{
		LOGD("Cannot get fixed screen information.");
		close(fd);
		return -1;
	}

	LOGD("====== smem_start : %lu",  finfo.smem_start);
	// Framebuffer设备的大小
	LOGD("====== smem_len : %d",  finfo.smem_len);
	// 一行的byte数目 除以 (bits_per_pixel/8) 就是一行的像素点的数目
	LOGD("====== line_length : %d",  finfo.line_length);

	//FB_TYPE_PACKED_PIXELS                0       /* Packed Pixels        */
	//FB_TYPE_PLANES                            1       /* Non interleaved planes */
	//FB_TYPE_INTERLEAVED_PLANES      2       /* Interleaved planes   */
	//FB_TYPE_TEXT                                3       /* Text/attributes      */
	//FB_TYPE_VGA_PLANES                    4       /* EGA/VGA planes       */
	//FB_TYPE_FOURCC                          5       /* Type identified by a V4L2 FOURCC */
	LOGD("====== type : %d",  finfo.type);
	// 每屏数据的字节数
	byte_per_frame = finfo.line_length * vinfo.yres;

//	framebuffer_memory = (unsigned char*)mmap(
//			0,
//			byte_per_frame,
//			PROT_READ,
//			// 不要用MAP_PRIVATE
//			MAP_SHARED,
//			fd,
//			// 假定xoffset为0
//			finfo.line_length * vinfo.yoffset);

	framebuffer_memory = (void*)mmap(
			0,
			byte_per_frame,
			PROT_READ,
			// 不要用MAP_PRIVATE
			MAP_SHARED,
			fd,
			// 假定xoffset为0
			finfo.line_length * vinfo.yoffset);

	if(framebuffer_memory == MAP_FAILED) {
		close(fd);
		LOGE("mmap for fb0 failed!");
		return -1;
	}

	handle = tjInitCompress();

	// 每四个像素，只取左上角的那个
	jpeg_data = (unsigned char *) malloc(byte_per_frame / 4);
	result = (unsigned char *) malloc(byte_per_frame / 4);

	bgra_data_scale_quarter(framebuffer_memory, jpeg_data, byte_per_frame / 4, finfo.line_length / 4);

//	tjCompress2(handle, jpeg_data,
//			// 希望生成的jpeg图片的宽 源数据里头屏幕每行的字节数
//			640, finfo.line_length,
//			// 希望生成的jpeg图片的高
//			360, TJPF_BGRA,
//			&result, &jpegSize,
//			TJSAMP_444, 10,
//			TJFLAG_NOREALLOC);
	LOGD("Turbo-jpeg compress fb0 done!");

	// 释放资源
	munmap(framebuffer_memory, byte_per_frame);

	(*env)->SetByteArrayRegion(env,
			ba,
			0,
			jpegSize,
			(jbyte*)result);

	tjFree(jpeg_data);
	tjFree(result);
	close(fd);
	tjDestroy(handle);

	return jpegSize;
}

/**
 * 横向和竖向都每两行取一行像素值，也就是图像大小缩小到原来的四分之一
 */
int bgra_data_scale_quarter(const void* src, unsigned char* dst, size_t pixels, size_t row_pixels)
{
    bgra8888_t  *from;
    bgra8888_t  *to;

    from = (bgra8888_t *) src;
    to = (bgra8888_t *) dst;

    size_t i = 0;
    size_t current_row = 0;
    size_t index_in_dst = 0;
    /* traverse pixel of the row */
    while(i++ < pixels) {
    	current_row = i / row_pixels;
    	if (i % 2 == 1 || current_row % 2 == 1) continue;

    	to[index_in_dst] = from[i];
    	index_in_dst++;
    }

    return 0;
}

static const char *classPathName = "com/a1w0n/standard/Jni/Exec";

/**
 * 写法：
 *     括号 () 里头的是参数 紧跟着的是返回值 比如 ： (I)I 就是参数是一个int 返回值也是一个int
 *	   返回值void 用 V 表示
 *
 *	   值得注意的一点是，当参数类型是引用数据类型时，其格式是“L包名；”其中包名中的“.” 换成“/”，
 *	   所以在上面的例子中(Ljava/lang/String;Ljava/lang/String;)V 表示 void Func(String,String)；
 *
 *	   数组则以”["开始,用两个字符表示
 *	   [B jbyteArra byte[]
 */
static JNINativeMethod method_table[] = { { "test", "([B)I",
		(void*) android_os_Exec_test } };

int init_Exec(JNIEnv *env) {
	if (!registerNativeMethods(env, classPathName, method_table,
			sizeof(method_table) / sizeof(method_table[0]))) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

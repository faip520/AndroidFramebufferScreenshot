package com.a1w0n.standard;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import android.content.Context;
import android.net.Uri;

/**
 * IO utilities.
 */
public class IOUtils {

	/**
	 * The default size of the buffer.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	private static final int EOF = -1;

	private IOUtils() {
	}

	/**
	 * Copy bytes from an InputStream to an OutputStream. Reference from apache common io package.
	 */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Reads an InputStream and converts it to a String.
	 */
	public String inputStreamToString(InputStream stream, int len) throws IOException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

	/**
	 * Usually used in try/catch block's finally block.
	 */
	public static void closeSilently(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ignored) {

			}
		}
	}

	public static int string2Int(String num) {
		int result;
		try {
			result = Integer.parseInt(num);
		} catch (NumberFormatException nfe) {
			return -1;
		}
		return result;
	}

	public static InputStream getInputStreamByUri(Context context, Uri uri) {
		if (uri == null)
			return null;
		String scheme = uri.getScheme();
		InputStream stream = null;
		if ((scheme == null) || ("file".equals(scheme))) {
			if (("file".equals(scheme)) && (uri.getPath().startsWith("/android_asset/"))) {
				try {
					stream = context.getAssets().open(uri.getPath().substring("/android_asset/".length()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else
				stream = getFileInputStream(uri.getPath());
		} else if ("content".equals(scheme)) {
			stream = getContentInputStreamByUri(context, uri);
		} else if (("http".equals(scheme)) || ("https".equals(scheme))) {
			stream = openRemoteInputStream(uri);
		}
		return stream;
	}

	public static InputStream getFileInputStream(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream getContentInputStreamByUri(Context context, Uri uri) {
		try {
			return context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static InputStream openRemoteInputStream(Uri uri) {
		URL finalUrl;
		HttpURLConnection connection;
		
		try {
			finalUrl = new URL(uri.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			connection = (HttpURLConnection) finalUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		connection.setInstanceFollowRedirects(false);
		
		int code;
		try {
			code = connection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		if ((code == 301) || (code == 302) || (code == 303)) {
			String newLocation = connection.getHeaderField("Location");
			return openRemoteInputStream(Uri.parse(newLocation));
		}
		
		try {
			return (InputStream) finalUrl.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
    // write byte[]
    //-----------------------------------------------------------------------
    /**
     * Writes bytes from a <code>byte[]</code> to an <code>OutputStream</code>.
     * 
     * @param data  the byte array to write, do not modify during output,
     * null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(byte[] data, OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code>
     * using the default character encoding of the platform.
     * <p>
     * This method uses {@link String#String(byte[])}.
     * 
     * @param data  the byte array to write, do not modify during output,
     * null ignored
     * @param output  the <code>Writer</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(byte[] data, Writer output) throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code>
     * using the specified character encoding.
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     * 
     * @param data  the byte array to write, do not modify during output,
     * null ignored
     * @param output  the <code>Writer</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.3
     */
    public static void write(byte[] data, Writer output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(new String(data, Charsets.toCharset(encoding)));
        }
    }

    /**
     * Writes bytes from a <code>byte[]</code> to chars on a <code>Writer</code>
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#String(byte[], String)}.
     * 
     * @param data  the byte array to write, do not modify during output,
     * null ignored
     * @param output  the <code>Writer</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedCharsetException
     *             thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     *             supported.
     * @since 1.1
     */
    public static void write(byte[] data, Writer output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    // write char[]
    //-----------------------------------------------------------------------
    /**
     * Writes chars from a <code>char[]</code> to a <code>Writer</code>
     * using the default character encoding of the platform.
     * 
     * @param data  the char array to write, do not modify during output,
     * null ignored
     * @param output  the <code>Writer</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(char[] data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes chars from a <code>char[]</code> to bytes on an
     * <code>OutputStream</code>.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes()}.
     * 
     * @param data  the char array to write, do not modify during output,
     * null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(char[] data, OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a <code>char[]</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     * 
     * @param data  the char array to write, do not modify during output,
     * null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.3
     */
    public static void write(char[] data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charsets.toCharset(encoding)));
        }
    }

    /**
     * Writes chars from a <code>char[]</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#String(char[])} and
     * {@link String#getBytes(String)}.
     * 
     * @param data  the char array to write, do not modify during output,
     * null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedCharsetException
     *             thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     *             supported.
     * @since 1.1
     */
    public static void write(char[] data, OutputStream output, String encoding)
            throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    // write CharSequence
    //-----------------------------------------------------------------------
    /**
     * Writes chars from a <code>CharSequence</code> to a <code>Writer</code>.
     * 
     * @param data  the <code>CharSequence</code> to write, null ignored
     * @param output  the <code>Writer</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    public static void write(CharSequence data, Writer output) throws IOException {
        if (data != null) {
            write(data.toString(), output);
        }
    }

    /**
     * Writes chars from a <code>CharSequence</code> to bytes on an
     * <code>OutputStream</code> using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * 
     * @param data  the <code>CharSequence</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    public static void write(CharSequence data, OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a <code>CharSequence</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * 
     * @param data  the <code>CharSequence</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.3
     */
    public static void write(CharSequence data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            write(data.toString(), output, encoding);
        }
    }

    /**
     * Writes chars from a <code>CharSequence</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * 
     * @param data  the <code>CharSequence</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedCharsetException
     *             thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     *             supported.
     * @since 2.0
     */
    public static void write(CharSequence data, OutputStream output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    // write String
    //-----------------------------------------------------------------------
    /**
     * Writes chars from a <code>String</code> to a <code>Writer</code>.
     * 
     * @param data  the <code>String</code> to write, null ignored
     * @param output  the <code>Writer</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(String data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    /**
     * Writes chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code> using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * 
     * @param data  the <code>String</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     */
    public static void write(String data, OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    /**
     * Writes chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * 
     * @param data  the <code>String</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 2.3
     */
    public static void write(String data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(data.getBytes(Charsets.toCharset(encoding)));
        }
    }

    /**
     * Writes chars from a <code>String</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * 
     * @param data  the <code>String</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedCharsetException
     *             thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     *             supported.
     * @since 1.1
     */
    public static void write(String data, OutputStream output, String encoding)
            throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    // write StringBuffer
    //-----------------------------------------------------------------------
    /**
     * Writes chars from a <code>StringBuffer</code> to a <code>Writer</code>.
     * 
     * @param data  the <code>StringBuffer</code> to write, null ignored
     * @param output  the <code>Writer</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     * @deprecated replaced by write(CharSequence, Writer)
     */
    @Deprecated
    public static void write(StringBuffer data, Writer output)
            throws IOException {
        if (data != null) {
            output.write(data.toString());
        }
    }

    /**
     * Writes chars from a <code>StringBuffer</code> to bytes on an
     * <code>OutputStream</code> using the default character encoding of the
     * platform.
     * <p>
     * This method uses {@link String#getBytes()}.
     * 
     * @param data  the <code>StringBuffer</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @since 1.1
     * @deprecated replaced by write(CharSequence, OutputStream)
     */
    @Deprecated
    public static void write(StringBuffer data, OutputStream output)
            throws IOException {
        write(data, output, (String) null);
    }

    /**
     * Writes chars from a <code>StringBuffer</code> to bytes on an
     * <code>OutputStream</code> using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link String#getBytes(String)}.
     * 
     * @param data  the <code>StringBuffer</code> to write, null ignored
     * @param output  the <code>OutputStream</code> to write to
     * @param encoding  the encoding to use, null means platform default
     * @throws NullPointerException if output is null
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedCharsetException
     *             thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     *             supported.
     * @since 1.1
     * @deprecated replaced by write(CharSequence, OutputStream, String)
     */
    @Deprecated
    public static void write(StringBuffer data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charsets.toCharset(encoding)));
        }
    }
    
	public static String toString(InputStream is) throws IOException {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
	
	/**
	 * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>. Use this method
	 * instead of <code>toByteArray(InputStream)</code> when <code>InputStream</code> size is known
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param size
	 *            the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException
	 *             if an I/O error occurs or <code>InputStream</code> size differ from parameter
	 *             size
	 * @throws IllegalArgumentException
	 *             if size is less than zero
	 * @since 2.1
	 */
	public static byte[] toByteArray(InputStream input, int size) throws IOException {

		if (size < 0) {
			throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
		}

		if (size == 0) {
			return new byte[0];
		}

		byte[] data = new byte[size];
		int offset = 0;
		int readed;

		while (offset < size && (readed = input.read(data, offset, size - offset)) != EOF) {
			offset += readed;
		}

		if (offset != size) {
			throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
		}

		return data;
	}
	
	/**
	 * Get contents of an <code>InputStream</code> as a <code>byte[]</code>. Use this method instead
	 * of <code>toByteArray(InputStream)</code> when <code>InputStream</code> size is known.
	 * <b>NOTE:</b> the method checks that the length can safely be cast to an int without
	 * truncation before using {@link IOUtils#toByteArray(java.io.InputStream, int)} to read into
	 * the byte array. (Arrays can have no more than Integer.MAX_VALUE entries anyway)
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param size
	 *            the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws IOException
	 *             if an I/O error occurs or <code>InputStream</code> size differ from parameter
	 *             size
	 * @throws IllegalArgumentException
	 *             if size is less than zero or size is greater than Integer.MAX_VALUE
	 * @see IOUtils#toByteArray(java.io.InputStream, int)
	 * @since 2.1
	 */
	public static byte[] toByteArray(InputStream input, long size) throws IOException {

		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
		}

		return toByteArray(input, (int) size);
	}
}

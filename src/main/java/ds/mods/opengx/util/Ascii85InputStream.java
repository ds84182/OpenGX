/*
 * Copyright (c) 2009-2013, i Data Connect!
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ds.mods.opengx.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * An ascii85 decoder, implemented as an {@link InputStream}.
 * </p>
 * <p>
 * <code>mark()</code> and <code>reset()</code> are supported, provided that
 * the underlying input stream supports them.
 * <p>
 * This implementation accepts encoded text with space character compression
 * enabled. See {@link Ascii85OutputStream} for details.
 * </p>
 * @author Ben Upsavs
 */
public class Ascii85InputStream extends FilterInputStream {

    private static final int[] POW85 = {85 * 85 * 85 * 85, 85 * 85 * 85, 85 * 85, 85, 1};
    private boolean preserveUnencoded;
    private final StreamState streamState = new StreamState();
    private final StreamState markStreamState = new StreamState();

    /**
     * Creates an input stream to decode ascii85 data from the underlying input
     * stream.
     * Any non ascii85 data will be discarded.
     */
    public Ascii85InputStream(InputStream in) {
        super(in);
    }

    /**
     * Creates an input stream to decode ascii85 data from the underlying input
     * stream. If <code>preserveUnencoeded</code> is <code>true</code>, any
     * non-ascii85 data will be output as-is. Otherwise, it is discarded.
     * @param preserveUnencoded Whether to preserve non-ascii85 encoded data.
     */
    public Ascii85InputStream(InputStream in, boolean preserveUnencoded) {
        this(in);
        this.preserveUnencoded = preserveUnencoded;
    }

    /**
     * {@inheritDoc}
     * @throws java.io.IOException If an underlying I/O error occurs, or if
     * the ascii85 data stream is not valid.
     */
    public int read() throws IOException {
        if (streamState.tupleBytesRemaining > 0) {
            int returnByte = 0;
            // pull decoded bytes from tuple
            switch (4 - (streamState.tupleSendStartBytes - streamState.tupleBytesRemaining--)) {
                case 4:
                    returnByte = (streamState.tuple >>> 24) & 0xff;
                    break;
                case 3:
                    returnByte = (streamState.tuple >>> 16) & 0xff;
                    break;
                case 2:
                    returnByte = (streamState.tuple >>>  8) & 0xff;
                    break;
                case 1:
                    returnByte = (streamState.tuple)        & 0xff;
                    break;
            }

            if (streamState.tupleBytesRemaining == 0)
                streamState.count = streamState.tuple = 0;

            return returnByte;
        } else if (streamState.nextByte != -1) {
            int returnByte = streamState.nextByte;
            streamState.nextByte = -1;
            return returnByte;
        } else if (!streamState.decoding) {
            int c = in.read();

            if (streamState.maybeStarting) {
                switch (c) {
                    case '~':
                        streamState.maybeStarting = false;
                        streamState.decoding = true;
                        return read();
                    default:
                        streamState.maybeStarting = false;
                        streamState.nextByte = c;
                    case '<':
                        return '<';
                }
            } else if (c == '<') {
                streamState.maybeStarting = true;
                return read();
            } else if (preserveUnencoded || c == -1)
                return c;
            else
                return read();
        } else {
            int c = in.read();

            if (streamState.maybeStopping && c != '>') {
                throw new IOException("~ without > in ascii85 section");
            }

            // Ignore whitespace
            if (Character.isWhitespace((char) c))
                return read();

            switch (c) {
                case '>':
                    if (streamState.maybeStopping) {
                        if (streamState.count > 0) {
                            streamState.count--;
                            streamState.tuple += POW85[streamState.count];
                            streamState.tupleBytesRemaining = streamState.tupleSendStartBytes = streamState.count;
                        }
                        streamState.maybeStopping = streamState.decoding = false;
                        return read();
                    }
                default:
                    if (c < '!' || c > 'u')
                        throw new IOException("Bad character in ascii85 section: [ascii " + c + "]: " + (char) c);
                    streamState.tuple += (c - '!') * POW85[streamState.count++];
                    if (streamState.count == 5)
                        streamState.tupleBytesRemaining = streamState.tupleSendStartBytes = 4;
                    return read();
                case 'y': // space compression
                    streamState.tuple |= 0x20202020;
                case 'z': // null compression
                    if (streamState.count != 0)
                        throw new IOException((char) c + " inside ascii85 5-tuple");
                    streamState.tupleBytesRemaining = streamState.tupleSendStartBytes = 4;
                    return read();
                case '~':
                    streamState.maybeStopping = true;
                    return read();
                case -1:
                    throw new IOException("EOF inside ascii85 section");
            }
        }
    }

    /**
     * {@inheritDoc}
     * @throws java.io.IOException If an underlying I/O error occurs, or if
     * the ascii85 data stream is not valid.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int readByte = read();
            if (readByte == -1) {
                return i == 0 ? -1 : i;
            }
            b[i] = (byte) readByte;
        }
        return len;
    }

    /**
     * Marks the stream for later reset. See
     * {@link java.io.InputStream#mark(int readLimit)} for details.
     * Note that this method relies on the underlying stream having support
     * for mark and reset.
     */
    public synchronized void mark(int readlimit) {
        // Save state for mark
        streamState.copyInto(markStreamState);

        super.mark(readlimit * 5);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method relies on the underlying stream having support
     * for mark and reset.
     * </p>
     */
    public synchronized void reset() throws IOException {
        // Reset state to mark
        markStreamState.copyInto(streamState);

        super.reset();
    }

    /**
     * Skips <code>n</code> bytes or less. This version will skip less bytes
     * than requested if an end of file is received and there is no error
     * in the underlying data stream. In other words, it is not valid to use
     * this method to skip over invalid ascii85 data.
     */
    public long skip(long n) throws IOException {
        int skipCount;
        for (skipCount = 0; skipCount < n; skipCount++) {
            if (read() == -1)
                break;
        }

        return skipCount - 1;
    }

    private class StreamState {
        private int tuple;
        private int count;
        private boolean decoding;
        private boolean maybeStarting;
        private boolean maybeStopping;
        private int tupleBytesRemaining;
        private int tupleSendStartBytes;
        private int nextByte = -1;

        private void copyInto(StreamState other) {
            other.tuple = this.tuple;
            other.count = this.count;
            other.decoding = this.decoding;
            other.maybeStarting = this.maybeStarting;
            other.maybeStopping = this.maybeStopping;
            other.tupleBytesRemaining = this.tupleBytesRemaining;
            other.tupleSendStartBytes = this.tupleSendStartBytes;
            other.nextByte = this.nextByte;
        }
    }
}

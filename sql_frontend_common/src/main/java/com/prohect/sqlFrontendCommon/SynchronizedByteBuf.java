package com.prohect.sqlFrontendCommon;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public final class SynchronizedByteBuf {

    private final ByteBuf buf;

    public SynchronizedByteBuf(ByteBuf buf) {
        this.buf = buf;
    }

    public int readerIndex() {
        synchronized (buf) {
            return buf.readerIndex();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ByteBuf readerIndex(int readerIndex) {
        synchronized (buf) {
            return buf.readerIndex(readerIndex);
        }
    }

    public int readableBytes() {
        synchronized (buf) {
            return buf.readableBytes();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ByteBuf readBytes(ByteBuf dst, int length) {
        synchronized (buf) {
            return buf.readBytes(dst, length);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ByteBuf readBytes(byte[] dst) {
        synchronized (buf) {
            return buf.readBytes(dst);
        }
    }

    public byte readByte() {
        synchronized (buf) {
            return buf.readByte();
        }
    }

    public boolean isWritable() {
        synchronized (buf) {
            return buf.isWritable();
        }
    }

    public ByteBufAllocator alloc() {
        synchronized (buf) {
            return buf.alloc();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ByteBuf discardReadBytes() {
        synchronized (buf) {
            return buf.discardReadBytes();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ByteBuf writeBytes(ByteBuf msg) {
        synchronized (buf) {
            return buf.writeBytes(msg);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean release() {
        return buf.release();
    }
}

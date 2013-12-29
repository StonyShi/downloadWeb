package com.stoney.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopy {
    private static final int BUFFER_SIZE_1024 = 1024;
    private static final int BUFFER_SIZE_4096 = 4096;
    private static final int BUFFER_SIZE_10240 = 10240;
        
    private static final String FROM_FILE_42MB = "G:/from_42MB.rar";
    private static final String FROM_FILE_1GB = "G:/from_350MB.rar";
        
    private static int BUFFER_SIZE = BUFFER_SIZE_1024;
    private static String FROM_FILE = FROM_FILE_42MB;
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
        BUFFER_SIZE = BUFFER_SIZE_4096;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
        BUFFER_SIZE = BUFFER_SIZE_10240;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
        BUFFER_SIZE = BUFFER_SIZE_1024;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
        BUFFER_SIZE = BUFFER_SIZE_4096;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
        BUFFER_SIZE = BUFFER_SIZE_10240;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
            
    }
    private static void testFileCopy() throws FileNotFoundException,
            IOException {
        coypByMbb();
        copyByNioTransferFrom();
        copyByNioTransferTo();
        coypByBufferRead();
        coypByFastBufferRead();          
        coypByStream();//Old IO style
    }
    /**
     * 使用FileChannel.transferFrom()实现
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyByNioTransferFrom() throws FileNotFoundException,
            IOException {
        long startTime = System.currentTimeMillis();
        RandomAccessFile fromFile = new RandomAccessFile(FROM_FILE, "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("G:/to1.rar", "rw");
        FileChannel toChannel = toFile.getChannel();
        long position = 0;
        long count = fromChannel.size();
        toChannel.transferFrom(fromChannel, position, count);
        long endTime = System.currentTimeMillis();
        System.out.println("copyByNioTransferFrom time consumed(buffer size no effect) : "
                + (endTime - startTime));
    }
    /**
     * 使用FileChannel.transferTo()实现
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyByNioTransferTo() throws FileNotFoundException,
            IOException {
        long startTime = System.currentTimeMillis();
        RandomAccessFile fromFile = new RandomAccessFile(FROM_FILE, "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("G:/to2.rar", "rw");
        FileChannel toChannel = toFile.getChannel();
        long position = 0;
        long count = fromChannel.size();
        fromChannel.transferTo(position, count, toChannel);
        long endTime = System.currentTimeMillis();
        System.out.println("copyByNioTransferTo time consumed(buffer size no effect) : "
                + (endTime - startTime));
    }
    /**
     * 使用Channel, Buffer简单读写实现
     * @throws IOException
     */
    private static void coypByBufferRead() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/to3.rar");
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (true) {
            buffer.clear();
            int r = fcin.read(buffer);
            if (r == -1) {
                break;
            }
            buffer.flip();
            fcout.write(buffer);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("coypByBufferRead time consumed(buffer size take effect) : "
                + (endTime - startTime));
    }
    /**
     * 使用连续内存的Buffer实现
     * @throws IOException
     */
    private static void coypByFastBufferRead() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/to4.rar");
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        while (true) {
            buffer.clear();
            int r = fcin.read(buffer);
            if (r == -1) {
                break;
            }
            buffer.flip();
            fcout.write(buffer);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("coypByFastBufferRead time consumed(buffer size take effect) : "
                + (endTime - startTime));
    }
    /**
     * 使用文件内存映射实现
     * @throws IOException
     */
    private static void coypByMbb() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        RandomAccessFile fout = new RandomAccessFile("G:/to5.rar", "rw");
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        MappedByteBuffer mbbi = fcin.map(FileChannel.MapMode.READ_ONLY, 0,
                fcin.size());
        MappedByteBuffer mbbo = fcout.map(FileChannel.MapMode.READ_WRITE, 0,
                fcin.size());
        mbbo.put(mbbi);
        mbbi.clear();
        mbbo.clear();
        long endTime = System.currentTimeMillis();
        System.out
                .println("coypByMbb time consumed(buffer size no effect) : " + (endTime - startTime));
    }
    /**
     * 使用传统IO的流读写方式实现
     * @throws IOException
     */
    private static void coypByStream() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/to6.rar");
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int ins = fin.read(buffer);
            if (ins == -1) {
                fin.close();
                fout.flush();
                fout.close();
                break;
            } else{
                fout.write(buffer, 0, ins);
            }            
        }
        long endTime = System.currentTimeMillis();
        System.out.println("coypByStream time consumed(buffer size take effect) : " + (endTime - startTime));
    }
}
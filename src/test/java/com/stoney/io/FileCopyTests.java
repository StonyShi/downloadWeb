package com.stoney.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;
import com.google.common.io.Files;

/**
 * Created with IntelliJ IDEA.
 * User: ShiHui
 * Date: 13-12-31
 * Time: 下午5:50
 * To change this template use File | Settings | File Templates.
 */
public class FileCopyTests {

    private static final int BUFFER_SIZE_1024 = 1024;
    private static final int BUFFER_SIZE_4096 = 4096;
    private static final int BUF_SIZE = 0x1000; // 4K
    private static final int BUFFER_SIZE_10240 = 10240;

    private static final String FROM_FILE_42MB = "C:/Java/apache-commons/jdk-src/jdk-src-B27.zip";
    private static final String FROM_FILE_1GB = "G:/相册/20130405.zip";

    private static final String TO_DIR = "G:/TEMP/";

    private static int BUFFER_SIZE = BUFFER_SIZE_1024;
    private static String FROM_FILE = FROM_FILE_42MB;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        clearFiles(TO_DIR);
        // download(new
        // URL("http://search.maven.org/remotecontent?filepath=com/google/guava/guava/15.0/guava-15.0.jar"),
        // new File(TO_DIR + "guava.jar"));
        download(new URL("https://www.google.com.hk/images/nav_logo170_hr.png"), new File(TO_DIR + "nav_logo170_hr.png"));
        download("https://ssl.gstatic.com/gb/images/v1_53a1fa6a.png", (TO_DIR + "v1_53a1fa6a.png"));
        download("https://sb.scorecardresearch.com/beacon.js", (TO_DIR + "beacon.js"));

    }

    private static void testcopy() throws Exception {
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);

        BUFFER_SIZE = BUFFER_SIZE_4096;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);

        BUFFER_SIZE = BUFFER_SIZE_10240;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);

        BUFFER_SIZE = BUFFER_SIZE_1024;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);

        BUFFER_SIZE = BUFFER_SIZE_4096;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);

        BUFFER_SIZE = BUFFER_SIZE_10240;
        FROM_FILE = FROM_FILE_1GB;
        System.out.println("File :" + FROM_FILE + " ---- Buffer Size : " + BUFFER_SIZE + "--------------");
        testFileCopy();
        clearFiles(TO_DIR);
    }

    public static void print(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    private static void testFileCopy() throws FileNotFoundException, IOException {
        // coypByMbb();
        // copyFile(FROM_FILE, "G:/TEMP/to121.zip", BUFFER_SIZE);
        // buferReader();
        // fileReader();
        // charBuferReader();
        // charBuferFileReader();
        // copyByNioTransferFrom();
        // copyByNioTransferTo();
        fileUtilsCopy();
        guavaFilesCopy();
        coypByBufferRead();
        // coypByFastBufferRead();
        // coypByStream();// Old IO style
    }

    private static void clearFiles(String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.exists() && file.isFile())
                    file.delete();
            }
        }
    }

    private static void fileUtilsCopy() {
        long startTime = System.currentTimeMillis();
        try {
            FileUtils.copyFile(new File(FROM_FILE), new File("G:/TEMP/to211.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("fileUtilsCopy time consumed(buffer size no effect) : " + (endTime - startTime));
        }
    }

    /**
     * 使用FileChannel.transferFrom()实现
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyByNioTransferFrom() throws FileNotFoundException, IOException {
        long startTime = System.currentTimeMillis();
        RandomAccessFile fromFile = new RandomAccessFile(FROM_FILE, "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("G:/TEMP/to1.zip", "rw");
        FileChannel toChannel = toFile.getChannel();
        long position = 0;
        long count = fromChannel.size();
        toChannel.transferFrom(fromChannel, position, count);
        long endTime = System.currentTimeMillis();
        System.out.println("copyByNioTransferFrom time consumed(buffer size no effect) : " + (endTime - startTime));
    }

    /**
     * 使用FileChannel.transferTo()实现
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void copyByNioTransferTo() throws FileNotFoundException, IOException {
        long startTime = System.currentTimeMillis();
        RandomAccessFile fromFile = new RandomAccessFile(FROM_FILE, "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("G:/TEMP/to2.zip", "rw");
        FileChannel toChannel = toFile.getChannel();
        long position = 0;
        long count = fromChannel.size();
        fromChannel.transferTo(position, count, toChannel);
        long endTime = System.currentTimeMillis();
        System.out.println("copyByNioTransferTo time consumed(buffer size no effect) : " + (endTime - startTime));
    }

    /**
     * 使用Channel, Buffer简单读写实现
     *
     * @throws IOException
     */
    private static void coypByBufferRead() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/TEMP/to3.zip");
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
        System.out.println("coypByBufferRead time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    /**
     * 使用连续内存的Buffer实现
     *
     * @throws IOException
     */
    private static void coypByFastBufferRead() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/TEMP/to4.zip");
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
        System.out.println("coypByFastBufferRead time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    /**
     * 使用文件内存映射实现
     *
     * @throws IOException
     */
    private static void coypByMbb() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        RandomAccessFile fout = new RandomAccessFile("G:/TEMP/to100.zip", "rw");
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        MappedByteBuffer mbbi = fcin.map(FileChannel.MapMode.READ_ONLY, 0, fcin.size());
        MappedByteBuffer mbbo = fcout.map(FileChannel.MapMode.READ_WRITE, 0, fcin.size());
        mbbo.put(mbbi);
        mbbi.clear();
        mbbo.clear();
        long endTime = System.currentTimeMillis();
        System.out.println("coypByMbb time consumed(buffer size no effect) : " + (endTime - startTime));
    }

    static void copyFile(String sFile, String dFile, int Max_Size) {
        long startTime = System.currentTimeMillis();
        int BufferSize = 1024 * 1024 * 20;
        if (Max_Size != 0) {
            BufferSize = Max_Size;
        }
        File sFile2 = new File(sFile);
        File dFile2 = new File(dFile);
        if (!sFile2.exists())
            return;
        if (!dFile2.exists()) {
            try {
                dFile2.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }
        try {
            RandomAccessFile sIn = new RandomAccessFile(sFile2, "r");
            try {
                RandomAccessFile sOut = new RandomAccessFile(dFile2, "rw");
                // 多次读，写
                // 根据文件大小来决定
                if (BufferSize > sFile2.length()) {
                    byte[] sByte = new byte[(int) sFile2.length()];
                    try {
                        sIn.read(sByte);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block

                        System.out.print("读源文件错误！\n");
                        return;
                    }
                    try {
                        sOut.write(sByte);
                        sOut.close();
                        sIn.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                        System.out.print("文件写入错误！\n");
                        return;
                    }
                } else {
                    // System.out.print("Source Size:"+sFile2.length()+"\n");
                    // System.out.print("(int) (sFile2.length() / BufferSize)"+(int)
                    // (sFile2.length() / BufferSize)+"\n");
                    for (int i = 0; i <= (int) (sFile2.length() / BufferSize); i++) {
                        // System.out.print("第"+i+"段数据\n");
                        if (i == (int) (sFile2.length() / BufferSize)) {

                            byte[] sByte = new byte[(int) (sFile2.length() - i * BufferSize + 1)];
                            try {
                                sIn.seek((int) i * BufferSize);
                                sIn.read(sByte);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block

                                System.out.print("读源文件错误！\n");
                                return;
                            }

                            try {
                                sOut.seek((int) i * BufferSize);
                                sOut.write(sByte);
                                sIn.close();
                                sOut.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block

                                System.out.print("写入目标文件错误！\n");
                                return;
                            }
                        } else {
                            byte[] sByte = new byte[BufferSize];
                            try {
                                sIn.seek((int) i * BufferSize);
                                sIn.read(sByte);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                System.out.print("读源文件错误！\n");
                                return;
                            }
                            try {
                                sOut.seek((int) i * BufferSize);
                                sOut.write(sByte);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                System.out.print("写入目标文件错误！\n");
                                return;
                            }
                        }

                    }
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("copyFile time consumed(buffer size no effect) : " + (endTime - startTime));
        }

    }

    /**
     * 使用传统IO的流读写方式实现
     *
     * @throws IOException
     */
    private static void coypByStream() throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(FROM_FILE);
        FileOutputStream fout = new FileOutputStream("G:/TEMP/to6.zip");
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int ins = fin.read(buffer);
            if (ins == -1) {
                fin.close();
                fout.flush();
                fout.close();
                break;
            } else {
                fout.write(buffer, 0, ins);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("coypByStream time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void buferReader() throws IOException {
        long startTime = System.currentTimeMillis();
        BufferedReader reader = new BufferedReader(new FileReader(new File(FROM_FILE)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("G:/TEMP/to7.zip")));
        char[] buffer = new char[BUFFER_SIZE];
        int i = 0;
        while ((i = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, i);
        }
        writer.flush();
        writer.close();
        reader.close();
        long endTime = System.currentTimeMillis();
        System.out.println("buferReader time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void charBuferReader() throws IOException {
        long startTime = System.currentTimeMillis();
        BufferedReader reader = new BufferedReader(new FileReader(new File(FROM_FILE)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("G:/TEMP/to8.zip")));
        int i = 0;
        CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
        while ((i = reader.read(buffer)) != -1) {
            buffer.flip();
            writer.write(buffer.array());
        }
        writer.flush();
        writer.close();
        reader.close();
        long endTime = System.currentTimeMillis();
        System.out.println("charBuferReader time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void charBuferFileReader() throws IOException {
        long startTime = System.currentTimeMillis();
        FileReader reader = (new FileReader(new File(FROM_FILE)));
        FileWriter writer = (new FileWriter(new File("G:/TEMP/to9.zip")));
        int i = 0;
        CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
        while ((i = reader.read(buffer)) != -1) {
            buffer.flip();
            writer.write(buffer.array());
        }
        writer.flush();
        writer.close();
        reader.close();
        long endTime = System.currentTimeMillis();
        System.out.println("charBuferFileReader time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void fileReader() throws IOException {
        long startTime = System.currentTimeMillis();
        FileReader reader = (new FileReader(new File(FROM_FILE)));
        FileWriter writer = (new FileWriter(new File("G:/TEMP/to10.zip")));
        char[] buffer = new char[BUFFER_SIZE];
        int i = 0;
        while ((i = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, i);
        }
        writer.flush();
        writer.close();
        reader.close();
        long endTime = System.currentTimeMillis();
        System.out.println("fileReader time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void guavaFilesCopy() throws IOException {
        long startTime = System.currentTimeMillis();
        Files.copy(new File(FROM_FILE), new File("G:/TEMP/to12.zip"));
        long endTime = System.currentTimeMillis();
        System.out.println("guavaFilesCopy time consumed(buffer size take effect) : " + (endTime - startTime));
    }

    private static void download(String url, String toPath) throws IOException {
        download(new URL(url), new File(toPath));
    }

    private static void download(URL url, File to) throws IOException {
        long startTime = System.currentTimeMillis();
        com.google.common.io.Files.copy(com.google.common.io.Resources.asByteSource(url), to);
        long endTime = System.currentTimeMillis();
        System.out.println("download time consumed(buffer size take effect) : " + (endTime - startTime));
    }
}

/*
 * Copyright (C) 2014 user
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.ibm.soatf.tool;

import com.ibm.soatf.gui.ProgressMonitor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This utility compresses a list of files to standard ZIP format file.
 * It is able to compress all sub files and sub directories, recursively.
 * @author user
 */
public class ZipUtils {
    private static ZipUtils instance;
    private ZipUtils() {}
    
    /**
     * A constants for buffer size used to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    
    public static ZipUtils getInstance() {
        if(instance == null) {
            instance = new ZipUtils();
        }
        return instance;
    }
    
    /**
     * Compresses a list of files to a destination zip file
     * @param files A collection of files and directories
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void zip(File[] files, File destZipFile) throws FileNotFoundException,
            IOException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(destZipFile));
            for (File file : files) {
                ProgressMonitor.increment("Zipping files...");
                if (file.isDirectory()) {
                    zipDirectory(file, file.getName(), zos);
                } else {
                    zipFile(file, null, zos);
                }
            }
        } finally {
            if (zos != null) {
                try {
                    zos.flush();
                } catch (IOException iOException) {
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException iOException) {
                }
            }
        }
    }
    /**
     * Compresses files represented in an array of paths
     * @param files a String array containing file paths
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void zip(String[] files, File destZipFile) throws FileNotFoundException, IOException {
        List<File> listFiles = new ArrayList<>();
        for (String file : files) {
            listFiles.add(new File(file));
        }
        zip(listFiles.toArray(new File[listFiles.size()]), destZipFile);
    }
    /**
     * Adds a directory to the current zip output stream
     * @param folder the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void zipDirectory(File folder, String parentFolder,
            ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zipFile(file, parentFolder, zos);
        }
    }
    /**
     * Adds a file to the current zip output stream
     * @param file the file to be added
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void zipFile(File file, String parentFolder, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry((Utils.isEmpty(parentFolder) ? "" : parentFolder + "/") + file.getName()));
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(
                    file));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
            }
            zos.closeEntry();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException iOException) {
                }
            }
        }
    }
}

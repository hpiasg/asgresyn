package de.uni_potsdam.hpi.asg.resyntool.io;

/*
 * Copyright (C) 2016 Norman Kluge
 * 
 * This file is part of ASGresyn.
 * 
 * ASGresyn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASGresyn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ASGresyn.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator;

public class SFTP {
    private static final Logger logger = LogManager.getLogger();

    public static boolean uploadFiles(Session session, Set<String> sourcefiles, String target) {
        try {
            ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            channel.mkdir(target);
            channel.cd(target);

            for(String filename : sourcefiles) {
                File file = new File(WorkingdirGenerator.getInstance().getWorkingdir(), filename);
                if(file.exists() && file.isFile()) {
                    if(file.getName().startsWith("__")) {
                        continue;
                    }
                    channel.put(new FileInputStream(file), file.getName());
                } else {
                    logger.warn("Omitting " + filename);
                }
            }
            channel.disconnect();
            return true;
        } catch(SftpException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(JSchException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }

    public static boolean downloadFiles(Session session, String source, String target) {
        try {
            ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            channel.cd(source);

            byte[] buffer = new byte[1024];
            BufferedInputStream bis = null;
            File newFile = null;
            OutputStream os = null;
            BufferedOutputStream bos = null;
            int readCount = -1;
            @SuppressWarnings("unchecked")
            Vector<LsEntry> files = (Vector<LsEntry>)channel.ls(".");
            for(LsEntry entry : files) {
                if(entry.getAttrs().getSize() > (1024 * 1024 * 50)) {
                    logger.info(entry.getFilename() + " is larger than 50MB. skipped");
                    continue;
                }
                if(!entry.getAttrs().isDir()) {
                    bis = new BufferedInputStream(channel.get(entry.getFilename()));
                    newFile = new File(target + entry.getFilename());
                    os = new FileOutputStream(newFile);
                    bos = new BufferedOutputStream(os);
                    while((readCount = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, readCount);
                    }
                    bis.close();
                    bos.close();
                }
            }
            channel.disconnect();
            return true;
        } catch(SftpException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(JSchException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } catch(IOException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }
}

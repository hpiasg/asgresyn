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

public class SFTP {
    private static final Logger logger = LogManager.getLogger();

    private Session             session;
    private String              directory;

    public SFTP(Session session) {
        this.session = session;
    }

    public boolean uploadFiles(Set<String> sourcefiles, String targetfolder, String tempname) {
        try {

            String newTargetBase = targetfolder;
            if(!targetfolder.endsWith("/")) {
                newTargetBase += "/";
            }
            newTargetBase += tempname;
            int tmpnum = 0;

            ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();

            String newTarget = newTargetBase + Integer.toString(tmpnum++);
            boolean mkdirsuccess = false;
            while(!mkdirsuccess) {
                try {
                    channel.mkdir(newTarget);
                    mkdirsuccess = true;
                } catch(SftpException e) {
                    newTarget = newTargetBase + Integer.toString(tmpnum++);
                }
            }
            directory = newTarget;
            channel.cd(newTarget);

            for(String filename : sourcefiles) {
                File file = new File(filename);
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

    public boolean downloadFiles(Session session, String target, boolean remove) {
        try {
            ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            channel.cd(directory);

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

            if(remove) {
                logger.debug("Removing temp dir");
                deleteFileRec(channel, directory);
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

    public void deleteFileRec(ChannelSftp channel, String filename) throws SftpException {
        if(channel.stat(filename).isDir()) {
            channel.cd(filename);
            @SuppressWarnings("unchecked")
            Vector<LsEntry> entries = (Vector<LsEntry>)channel.ls(".");
            for(LsEntry entry : entries) {
                if(entry.getFilename().equals(".") || entry.getFilename().equals("..")) {
                    continue;
                }
                deleteFileRec(channel, entry.getFilename());
            }
            channel.cd("..");
            channel.rmdir(filename);
        } else {
            channel.rm(filename);
        }
    }

    public String getDirectory() {
        return directory;
    }
}

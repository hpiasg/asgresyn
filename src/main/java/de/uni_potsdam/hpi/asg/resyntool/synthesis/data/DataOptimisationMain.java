package de.uni_potsdam.hpi.asg.resyntool.synthesis.data;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.resyntool.io.RunSHScript;
import de.uni_potsdam.hpi.asg.resyntool.io.SFTP;

public class DataOptimisationMain {
    private static final Logger logger = LogManager.getLogger();

    private String              remoteFolder;
    private Set<String>         files;

    private String              host;
    private String              username;
    private String              password;

    public DataOptimisationMain(String host, String username, String password, Set<String> files, String remotefolder) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.files = files;
        this.remoteFolder = remotefolder;
    }

    public boolean execute() {

        for(String filename : files) {
            FileHelper.getInstance().copyfile(filename, filename + ".bak");
        }

        //TODO: generate scripts
        return run();
    }

    private boolean run() {
        Session session = null;
        try {
            try {
                if(!InetAddress.getByName(host).isReachable(1000)) {
                    logger.error("Host Error 1");
                    return false;
                }
            } catch(UnknownHostException e) {
                logger.error("Host Error 2");
                return false;
            } catch(IOException e) {
                logger.error("Host Error 3");
                return false;
            }

            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setUserInfo(new MyUserInfo());
            session.setConfig("StrictHostKeyChecking", "yes");
            session.connect(30000);

            logger.info("Uploading files");
            if(!SFTP.uploadFiles(session, files, remoteFolder)) {
                logger.error("Upload failed");
                return false;
            }

            logger.info("Running scripts");
            int code = -1;
            for(String filename : files) {
                code = RunSHScript.run(session, filename + "_dataopt.sh", remoteFolder);
                if(code != 0) {
                    logger.warn("Optimisation of " + filename + " failed");
                    continue;
                }
            }

            logger.info("Downloading files");
            if(!SFTP.downloadFiles(session, remoteFolder, WorkingdirGenerator.getInstance().getWorkingdir())) {
                return false;
            }

            return true;

        } catch(JSchException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        } finally {
            if(session != null) {
                session.disconnect();
            }
        }
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return false;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return null;
        }
    }
}

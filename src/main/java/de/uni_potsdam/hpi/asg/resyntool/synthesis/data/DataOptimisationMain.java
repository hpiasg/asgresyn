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
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.resyntool.io.RunSHScript;
import de.uni_potsdam.hpi.asg.resyntool.io.SFTP;

public class DataOptimisationMain {
    private static final Logger logger = LogManager.getLogger();

    private String              remoteFolder;

    private String              host;
    private String              username;
    private String              password;

    public DataOptimisationMain(String host, String username, String password, String remotefolder) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.remoteFolder = remotefolder;
    }

    public boolean execute(Set<String> files) {

        Set<DataOptimisationPlan> plans = new HashSet<>();
        for(String filename : files) {
            ScriptGenerator gen = new ScriptGenerator(filename);
            plans.add(gen.generate());
        }

        return run(plans);
    }

    private boolean run(Set<DataOptimisationPlan> plans) {
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
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);

            SFTP sftpcon = new SFTP(session);

            logger.info("Uploading files");
            Set<String> uploadfiles = new HashSet<>();
            for(DataOptimisationPlan p : plans) {
                uploadfiles.addAll(p.getAllUploadFiles());
            }

            if(!sftpcon.uploadFiles(uploadfiles, remoteFolder)) {
                logger.error("Upload failed");
                return false;
            }

            logger.info("Running scripts");
            int code = -1;
            for(DataOptimisationPlan p : plans) {
                code = RunSHScript.run(session, p.getRemoteShScriptName(), sftpcon.getDirectory());
                if(code != 0) {
                    logger.warn("Optimisation of " + p.getLocalfilename() + " failed");
                    continue;
                }
            }

            logger.info("Downloading files");
            if(!sftpcon.downloadFiles(session, WorkingdirGenerator.getInstance().getWorkingdir())) {
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

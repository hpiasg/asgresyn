package de.uni_potsdam.hpi.asg.resyntool.gui;

/*
 * Copyright (C) 2017 Norman Kluge
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.gui.runner.IOStreamReader;
import de.uni_potsdam.hpi.asg.common.gui.runner.TerminalFrame;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.technology.TechnologyDirectory;
import de.uni_potsdam.hpi.asg.resyntool.ResynGuiMain;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.BooleanParam;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.EnumParam;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.TextParam;

public class ResynRunner {
    private static final Logger logger = LogManager.getLogger();

    private Parameters          params;

    public ResynRunner(Parameters params) {
        this.params = params;
    }

    public void run() {
        if(!checkParams()) {
            return;
        }
        List<String> cmd = buildCmd();

        StringBuilder str = new StringBuilder();
        for(String s : cmd) {
            str.append(s + " ");
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = null;
        try {
            process = pb.start();
        } catch(IOException e) {
            e.printStackTrace();
        }

        TerminalFrame tframe = new TerminalFrame("ASGresyn terminal", str.toString(), process);
        tframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tframe.setVisible(true);

        IOStreamReader ioreader = new IOStreamReader(process, tframe.getText());
        Thread streamThread = new Thread(ioreader);
        streamThread.start();
    }

    private boolean checkParams() {
        File breezefile = new File(params.getTextValue(TextParam.BreezeFile));
        if(!breezefile.exists()) {
            logger.error("Breezefile not found");
            return false;
        }
        if(!params.getBooleanValue(BooleanParam.TechLibDef)) {
            File techfile = getTechFile();
            if(!techfile.exists()) {
                logger.error("Techfile not found");
                return false;
            }
        }

        return true;
    }

    private File getTechFile() {
        String techName = params.getEnumValue(EnumParam.TechLib);
        String tech = ResynGuiMain.techdir + "/" + techName + TechnologyDirectory.techfileExtension;
        File techfile = FileHelper.getInstance().replaceBasedir(tech);
        return techfile;
    }

    private List<String> buildCmd() {
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("basedir") + "/bin/ASGresyn");

        addGeneralParams(cmd);
        addAdvancedParams(cmd);
        addDebugParams(cmd);

        cmd.add(params.getTextValue(TextParam.BreezeFile));

        return cmd;
    }

    private void addGeneralParams(List<String> cmd) {
        if(!params.getBooleanValue(BooleanParam.TechLibDef)) {
            cmd.add("-lib");
            File techfile = getTechFile();
            cmd.add(techfile.getAbsolutePath());
        }

        if(params.getBooleanValue(BooleanParam.OptDp)) {
            cmd.add("-odp");
        }

        String outDir = params.getTextValue(TextParam.OutDir);
        String outFile = params.getTextValue(TextParam.OutFile);
        if(outFile != null) {
            cmd.add("-sout");
            if(outDir == null) {
                cmd.add(outFile);
            } else {
                File file = new File(outDir, outFile);
                cmd.add(file.getAbsolutePath());
            }
        }

        String cfgFile = params.getTextValue(TextParam.CfgFile);
        if(cfgFile != null) {
            cmd.add("-cfg");
            cmd.add(cfgFile);
        }

        String workDir = params.getTextValue(TextParam.WorkingDir);
        if(workDir != null) {
            cmd.add("-w");
            cmd.add(workDir);
        }

        cmd.add("-o");
        if(params.getBooleanValue(BooleanParam.LogLvl0)) {
            cmd.add("0");
        } else if(params.getBooleanValue(BooleanParam.LogLvl1)) {
            cmd.add("1");
        } else if(params.getBooleanValue(BooleanParam.LogLvl2)) {
            cmd.add("2");
        } else if(params.getBooleanValue(BooleanParam.LogLvl3)) {
            cmd.add("3");
        }

        String logFile = params.getTextValue(TextParam.LogFile);
        if(logFile != null) {
            cmd.add("-log");
            if(outDir == null) {
                cmd.add(logFile);
            } else {
                File file = new File(outDir, logFile);
                cmd.add(file.getAbsolutePath());
            }
        }

        String zipFile = params.getTextValue(TextParam.TempFiles);
        if(zipFile != null) {
            cmd.add("-zip");
            if(outDir == null) {
                cmd.add(zipFile);
            } else {
                File file = new File(outDir, zipFile);
                cmd.add(file.getAbsolutePath());
            }
        }
    }

    private void addAdvancedParams(List<String> cmd) {
        cmd.add("-tc");
        StringBuilder tc = new StringBuilder();
        if(params.getBooleanValue(BooleanParam.tcS1)) {
            tc.append("S");
        } else if(params.getBooleanValue(BooleanParam.tcD1)) {
            tc.append("D");
        }
        if(params.getBooleanValue(BooleanParam.tcS2)) {
            tc.append("S");
        } else if(params.getBooleanValue(BooleanParam.tcD2)) {
            tc.append("D");
        }
        cmd.add(tc.toString());

        cmd.add("-d");
        cmd.add(params.getEnumValue(EnumParam.decoStrat));

        cmd.add("-p");
        cmd.add(params.getEnumValue(EnumParam.decoPart));

        cmd.add("-ls");
        StringBuilder ls = new StringBuilder();
        if(params.getBooleanValue(BooleanParam.cscP)) {
            ls.append("P");
        } else if(params.getBooleanValue(BooleanParam.cscM)) {
            ls.append("M");
        } else {
            System.err.println("No ls csc defined");
        }
        if(params.getBooleanValue(BooleanParam.synA)) {
            ls.append("A");
        } else if(params.getBooleanValue(BooleanParam.synP)) {
            ls.append("P");
        } else {
            System.err.println("No ls syn defined");
        }
        if(params.getBooleanValue(BooleanParam.tmA)) {
            ls.append("A");
        } else if(params.getBooleanValue(BooleanParam.tmP)) {
            ls.append("P");
        } else if(params.getBooleanValue(BooleanParam.tmN)) {
            ls.append("N");
        } else {
            System.err.println("No ls tm defined");
        }
        if(params.getBooleanValue(BooleanParam.rstA)) {
            ls.append("A");
        } else if(params.getBooleanValue(BooleanParam.rstP)) {
            ls.append("P");
        } else if(params.getBooleanValue(BooleanParam.rstI)) {
            ls.append("I");
        } else {
            System.err.println("No ls rst defined");
        }
        cmd.add(ls.toString());

        String asglogicParams = params.getTextValue(TextParam.Asglogic);
        if(asglogicParams != null) {
            cmd.add("-ASGlogicParams");
            cmd.add(asglogicParams);
        }
    }

    private void addDebugParams(List<String> cmd) {
        if(params.getBooleanValue(BooleanParam.debug)) {
            cmd.add("-debug");
        }

        if(params.getBooleanValue(BooleanParam.tooldebug)) {
            cmd.add("-tooldebug");
        }

        if(params.getBooleanValue(BooleanParam.ssc)) {
            cmd.add("-ssc");
        }

        if(params.getBooleanValue(BooleanParam.sdp)) {
            cmd.add("-sdp");
        }

        String breezeExprFile = params.getTextValue(TextParam.BreezeExprFile);
        if(breezeExprFile != null) {
            cmd.add("-breezeexpr");
            cmd.add(breezeExprFile);
        }
    }
}

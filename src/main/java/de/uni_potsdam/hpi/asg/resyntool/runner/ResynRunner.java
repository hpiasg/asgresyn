package de.uni_potsdam.hpi.asg.resyntool.runner;

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

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters.GeneralTextParam;
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractRunner;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.resyntool.ResynRunMain;
import de.uni_potsdam.hpi.asg.resyntool.runner.ResynParameters.BooleanParam;
import de.uni_potsdam.hpi.asg.resyntool.runner.ResynParameters.EnumParam;
import de.uni_potsdam.hpi.asg.resyntool.runner.ResynParameters.TextParam;

public class ResynRunner extends AbstractRunner {
    private static final Logger logger = LogManager.getLogger();

    private ResynParameters     params;

    public ResynRunner(ResynParameters params) {
        super(params);
        this.params = params;
    }

    public void run(TerminalMode mode) {
        run(mode, null);
    }

    public void run(TerminalMode mode, Window parent) {
        if(!checkParams()) {
            return;
        }
        List<String> cmd = buildCmd();
        exec(cmd, "ASGresyn terminal", mode, null, parent);
    }

    private boolean checkParams() {
        File breezefile = new File(params.getTextValue(TextParam.BreezeFile));
        if(!breezefile.exists()) {
            logger.error("Breezefile not found");
            return false;
        }
        if(!params.getBooleanValue(BooleanParam.DefTechLib) || params.isForceTech()) {
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
        return new File(CommonConstants.DEF_TECH_DIR_FILE, techName + CommonConstants.XMLTECH_FILE_EXTENSION);
    }

    private List<String> buildCmd() {
        List<String> cmd = new ArrayList<>();
        cmd.add(ResynRunMain.RESYN_BIN.getAbsolutePath());

        addGeneralParams(cmd);
        addAdvancedParams(cmd);
        addDebugParams(cmd);

        cmd.add(params.getTextValue(TextParam.BreezeFile));

        return cmd;
    }

    private void addGeneralParams(List<String> cmd) {
        if(!params.getBooleanValue(BooleanParam.DefTechLib) || params.isForceTech()) {
            cmd.add("-lib");
            File techfile = getTechFile();
            cmd.add(techfile.getAbsolutePath());
        }

        if(params.getBooleanValue(BooleanParam.OptDp)) {
            cmd.add("-odp");
        }

        String outDir = params.getTextValue(GeneralTextParam.OutDir);
        String stgFile = params.getTextValue(TextParam.STGout);
        if(stgFile != null) {
            cmd.add("-stgout");
            if(outDir == null) {
                cmd.add(stgFile);
            } else {
                File file = new File(outDir, stgFile);
                cmd.add(file.getAbsolutePath());
            }
        }

        addStandardIOParams(cmd, "-sout");
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

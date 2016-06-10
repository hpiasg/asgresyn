package de.uni_potsdam.hpi.asg.resyntool.synthesis.control;

/*
 * Copyright (C) 2012 - 2015 Norman Kluge
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

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.io.technology.Technology;
import de.uni_potsdam.hpi.asg.resyntool.ResynMain;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.LogicSynthesisParameter;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;

public class LogicSynthesis {
    private static final Logger     logger               = LogManager.getLogger();

    private static final String     petrifySynthesisLog  = ".petrify.log";
    private static final String     asglogicSynthesisLog = ".asglogic.log";
    private static final String     asglogicSynthesisZip = ".asglogic.zip";
    private static final String     tmpEqn               = ".tmp.eqn";
    private static final String     tmpSTG               = ".tmp.g";
    private static final String     solvedSTG            = ".csc.g";
    private static final String     solvedSTGlog         = ".csc.log";

    private LogicSynthesisParameter strategy;
    private Technology              tech;

    public LogicSynthesis(SynthesisParameter params) {
        this.strategy = params.getLogicSynthesisStrategy();
        this.tech = params.getTechnology();
    }

    public boolean synthesise(String gfile, String vfile) {
        String solvedCscFile = solveCSC(gfile);
        if(solvedCscFile == null) {
            logger.error("Could not solve csc of file " + gfile);
            return false;
        }
        logger.debug("CSC of " + gfile + " solved");
        String libfile = tech.getGenLib();
        switch(strategy.getSynthesisStrategy()) {
            case PPP: {
                String[] params = {"-no", "-rst1", "-tm", "-vl", vfile};
                String logfile = vfile + petrifySynthesisLog;
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logfile, libfile, params)) {
                    logger.error("PPP LogicSynthesis failed");
                    return false;
                }
                break;
            }
            case PNP: {
                String[] params = {"-no", "-rst1", "-vl", vfile};
                String logfile = vfile + petrifySynthesisLog;
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logfile, libfile, params)) {
                    logger.error("PNP LogicSynthesis failed");
                    return false;
                }
                break;
            }
            case PPI: {
                String eqnfile = vfile + tmpEqn;
                String stgfile = vfile + tmpSTG;

                String[] params = {"-o", stgfile, "-tm", "-eqn", eqnfile};
                String logfile = vfile + petrifySynthesisLog;
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logfile, libfile, params)) {
                    logger.error("PPI LogicSynthesis (petrify step) failed");
                    return false;
                }
                if(!ResynInvoker.getInstance().invokePetreset(eqnfile, stgfile, libfile, vfile)) {
                    logger.error("PPI LogicSynthesis (petreset step) failed");
                    return false;
                }
                List<String> lines = FileHelper.getInstance().readFile(vfile);
                int linenumber = 0;
                for(String line : lines) {
                    lines.set(linenumber++, line.replaceAll("RESET", "_reset"));
                }
                if(!FileHelper.getInstance().writeFile(vfile, lines)) {
                    logger.error("PPI LogicSynthesis (fixreset step) failed");
                    return false;
                }
                break;
            }
            case AAA: {
                String logfile = vfile + asglogicSynthesisLog;
                String zipfile = vfile + asglogicSynthesisZip;
                String workingdir = WorkingdirGenerator.getInstance().getWorkingdir() + "logic";

                String reset = null;

                if(ResynMain.config.toolconfig.asglogiccmd.contains("gC")) {
                    reset = "ondemand";
                } else {
                    for(String line : FileHelper.getInstance().readFile(solvedCscFile)) {
                        if(line.startsWith(".inputs")) {
                            if(parseTypeLine(line).contains("r1")) {
                                reset = "full";
                            } else {
                                reset = "ondemand";
                            }
                        }
                    }
                }
                if(reset == null) {
                    logger.error("resettype not set");
                    return false;
                }

                if(!ResynInvoker.getInstance().invokeASGLogic(solvedCscFile, vfile, workingdir, libfile, logfile, zipfile, reset)) {
                    logger.error("AAA LogicSynthesis failed");
                    return false;
                }
                break;
            }
        }
        logger.info("Logic synthesis of " + gfile + " finished successfully");
        return true;
    }

    private List<String> parseTypeLine(String line) {
        String[] arr = line.split(" ");
        arr = Arrays.copyOfRange(arr, 1, arr.length);
        return Arrays.asList(arr);
    }

    private String solveCSC(String gfile) {
        String outfile = gfile + solvedSTG;

        switch(strategy.getCsc()) {
            case petrify:
                String logfile = gfile + solvedSTGlog;
                if(!ResynInvoker.getInstance().invokePetrifyCSC(gfile, logfile, outfile)) {
                    logger.error("Could not solve csc with petrify");
                    return null;
                }
                break;
            case mpsat:
                if(!ResynInvoker.getInstance().invokePUNFandMPSAT(gfile, outfile)) {
                    logger.error("Could not solve csc with mpsat");
                    return null;
                }
                break;
            case dontcare:
                logger.error("CSC dontcaresolving is not yet implememted");
                return null;
        }
        return outfile;
    }
}

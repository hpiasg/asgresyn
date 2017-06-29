package de.uni_potsdam.hpi.asg.resyntool.synthesis.control;

/*
 * Copyright (C) 2012 - 2017 Norman Kluge
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

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
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

    private File                    workingDir;

    private LogicSynthesisParameter strategy;
    private Technology              tech;

    private String                  asglogicparams;

    public LogicSynthesis(SynthesisParameter params) {
        this.strategy = params.getLogicSynthesisStrategy();
        this.tech = params.getTechnology();
        this.asglogicparams = params.getAsglogicparams();
        this.workingDir = WorkingdirGenerator.getInstance().getWorkingDir();
    }

    public boolean synthesise(File gFile, File vFile) {
        File solvedCscFile = solveCSC(gFile);
        if(solvedCscFile == null) {
            logger.error("Could not solve csc of file " + gFile.getName());
            return false;
        }
        logger.debug("CSC of " + gFile.getName() + " solved");
        File libFile = tech.getGenLib();
        switch(strategy.getSynthesisStrategy()) {
            case PPP: {
                String[] params = {"-no", "-rst1", "-tm", "-vl", vFile.getAbsolutePath()};
                File logFile = new File(workingDir, vFile.getName() + petrifySynthesisLog);
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logFile, libFile, params)) {
                    logger.error("PPP LogicSynthesis failed");
                    return false;
                }
                break;
            }
            case PNP: {
                String[] params = {"-no", "-rst1", "-vl", vFile.getAbsolutePath()};
                File logFile = new File(workingDir, vFile.getName() + petrifySynthesisLog);
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logFile, libFile, params)) {
                    logger.error("PNP LogicSynthesis failed");
                    return false;
                }
                break;
            }
            case PPI: {
                File eqnFile = new File(workingDir, vFile.getName() + tmpEqn);
                File stgFile = new File(workingDir, vFile.getName() + tmpSTG);

                String[] params = {"-o", stgFile.getAbsolutePath(), "-tm", "-eqn", eqnFile.getAbsolutePath()};
                File logFile = new File(workingDir, vFile.getName() + petrifySynthesisLog);
                if(!ResynInvoker.getInstance().invokePetrifySynthesis(solvedCscFile, logFile, libFile, params)) {
                    logger.error("PPI LogicSynthesis (petrify step) failed");
                    return false;
                }
                if(!ResynInvoker.getInstance().invokePetreset(eqnFile, stgFile, libFile, vFile)) {
                    logger.error("PPI LogicSynthesis (petreset step) failed");
                    return false;
                }
                List<String> lines = FileHelper.getInstance().readFile(vFile);
                int linenumber = 0;
                for(String line : lines) {
                    lines.set(linenumber++, line.replaceAll("RESET", "_reset"));
                }
                if(!FileHelper.getInstance().writeFile(vFile, lines)) {
                    logger.error("PPI LogicSynthesis (fixreset step) failed");
                    return false;
                }
                break;
            }
            case AAA: {
                File logFile = new File(workingDir, vFile.getName() + asglogicSynthesisLog);
                File zipfile = new File(workingDir, vFile.getName() + asglogicSynthesisZip);
                File logicWorkingDir = new File(WorkingdirGenerator.getInstance().getWorkingDir(), "logic");

                String reset = null;

                if(asglogicparams.contains("-arch sC")) {
                    for(String line : FileHelper.getInstance().readFile(solvedCscFile)) {
                        if(line.startsWith(".inputs")) {
                            if(parseTypeLine(line).contains("r1")) {
                                reset = "full";
                            } else {
                                reset = "ondemand";
                            }
                        }
                    }
                } else {
                    reset = "ondemand";
                }
                if(reset == null) {
                    logger.error("resettype not set");
                    return false;
                }

                if(!ResynInvoker.getInstance().invokeASGLogic(solvedCscFile, vFile, logicWorkingDir, libFile, logFile, zipfile, reset, asglogicparams)) {
                    logger.error("AAA LogicSynthesis failed");
                    return false;
                }
                break;
            }
        }
        logger.info("Logic synthesis of " + gFile.getName() + " finished successfully");
        return true;
    }

    private List<String> parseTypeLine(String line) {
        String[] arr = line.split(" ");
        arr = Arrays.copyOfRange(arr, 1, arr.length);
        return Arrays.asList(arr);
    }

    private File solveCSC(File gFile) {
        File outFile = new File(workingDir, gFile.getName() + solvedSTG);

        switch(strategy.getCsc()) {
            case petrify:
                File logfile = new File(workingDir, gFile.getName() + solvedSTGlog);
                if(!ResynInvoker.getInstance().invokePetrifyCSC(gFile, logfile, outFile)) {
                    logger.error("Could not solve csc with petrify");
                    return null;
                }
                break;
            case mpsat:
                if(!ResynInvoker.getInstance().invokePUNFandMPSAT(gFile, outFile)) {
                    logger.error("Could not solve csc with mpsat");
                    return null;
                }
                break;
            case dontcare:
                logger.error("CSC dontcaresolving is not yet implememted");
                return null;
        }
        return outFile;
    }
}

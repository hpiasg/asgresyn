package de.uni_potsdam.hpi.asg.resyntool.io;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.Invoker;
import de.uni_potsdam.hpi.asg.common.iohelper.ProcessReturn;
import de.uni_potsdam.hpi.asg.resyntool.ResynMain;

public class ResynInvoker extends Invoker {
    private final static Logger logger = LogManager.getLogger();

    private static ResynInvoker instance;

    private ResynInvoker() {
    }

    public static ResynInvoker getInstance() {
        if(ResynInvoker.instance == null) {
            ResynInvoker.instance = new ResynInvoker();
            if(Invoker.instance == null) {
                Invoker.instance = ResynInvoker.instance;
            } else {
                logger.warn("Logger instance already set");
            }
        }
        return ResynInvoker.instance;
    }

    public boolean invokeBalsaNetlist(String technology, File outfile, String component, List<String> params) {
        String[] cmd = convertCmd(ResynMain.config.toolconfig.balsanetlistcmd);
        if(cmd == null) {
            logger.error("Could not read balsanetlist cmd String");
            return false;
        }
        String[] params1 = {"-X", technology, "-o", outfile.getAbsolutePath(), "-t", component};
        List<String> params2 = new ArrayList<String>();
        params2.addAll(Arrays.asList(params1));
        for(String str : params) {
            params2.add(str.replaceAll("\"", ""));
        }
        ProcessReturn ret = invoke(cmd, params2);
        return errorHandling(ret);
    }

    public boolean invokeDesijBreeze(File outfile, File infile, boolean withdeco, File breezeExprFile) {
        String[] params = null;
        if(withdeco) {
            params = new String[]{"-Y", "-g", "operation=breeze"};
        } else {
            params = new String[]{"-Y", "operation=breeze"};
        }
        List<String> params2 = new ArrayList<>();
        params2.addAll(Arrays.asList(params));

        if(breezeExprFile != null) {
            try {
                params2.add("breezeexpressionsfile=" + breezeExprFile.getCanonicalPath());
            } catch(IOException e) {
            }
        }

        params2.add("outfile=" + outfile.getAbsolutePath());
        params2.add(infile.getAbsolutePath());

        return invokeDesij(params2);
    }

    public boolean invokeDesijKilldummies(File outfile, File infile) {
        String[] params = {"-Y", "-t", "operation=killdummiesrelaxed", "outfile=" + outfile.getAbsolutePath(), infile.getAbsolutePath()}; //"-t"
        return invokeDesij(Arrays.asList(params));
    }

    public boolean invokeDesijDecomposition(String decompositionStrategy, String partitionStrategy, File infile) {
        String[] params = {/*"-k",*/ "-Y", "-t", "operation=decompose", "version=" + decompositionStrategy, "partition=" + partitionStrategy, infile.getAbsolutePath()}; //"-t"
        return invokeDesij(Arrays.asList(params));
    }

    private boolean invokeDesij(List<String> params) {
        //TODO: timeout for desij

        String[] cmd = convertCmd(ResynMain.config.toolconfig.desijcmd);
        if(cmd == null) {
            logger.error("Could not read desij cmd String");
            return false;
        }
        ProcessReturn ret = invoke(cmd, params, ResynMain.tooldebug);
        return errorHandling(ret);
    }

    public boolean invokePUNFandMPSAT(File infile, File outfile) {
        String mcifile = infile.getAbsolutePath().replace(".g", "") + ".mci";

        String[] cmd = convertCmd(ResynMain.config.toolconfig.punfcmd);
        if(cmd == null) {
            logger.error("Could not read PUNF cmd string");
            return false;
        }
        String[] params = {"-m=" + mcifile, "-f=" + infile};
        ProcessReturn ret = invoke(cmd, params);

        File mci = new File(workingDir, mcifile);
        if(!mci.exists()) { // punf returns != 0 even if there are only warnings
            if(!errorHandling(ret)) {
                logger.error("PUNF Error with " + infile);
                return false;
            }
        }

        File tmpfolder = new File(workingDir, infile.getName() + "_tmp");
        if(!tmpfolder.mkdir()) {
            logger.error("Could not create tmp folder for MPSAT: " + tmpfolder.getName());
            return false;
        }

        String[] cmd2 = convertCmd(ResynMain.config.toolconfig.mpsatcmd);
        if(cmd2 == null) {
            logger.error("Could not read MPSAT cmd string");
            return false;
        }
        String[] params2 = {"-R", "-f", "-@", "-p0", "-cl", mcifile};
        ProcessReturn ret2 = invoke(cmd2, params2, tmpfolder);
        if(!errorHandling(ret2)) {
            logger.error("MPSAT Error with " + mcifile);
            return false;
        }

        String mpsatresult = tmpfolder.getAbsolutePath() + File.separator + "mpsat.g";
        if(!FileHelper.getInstance().copyfile(new File(mpsatresult), outfile)) {
            logger.error("Could not copy MPSAT result file from " + mpsatresult);
            return false;
        }

        return true;
    }

    public boolean invokePetrifyCSC(File infile, File logfile, File outfile) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petrifycmd);
        if(command == null) {
            logger.error("Could not read petrify cmd String");
            return false;
        }
        String[] params = {"-csc", "-dead", "-o", outfile.getAbsolutePath(), "-log", logfile.getAbsolutePath(), infile.getAbsolutePath()};
        ProcessReturn ret = invoke(command, params);
        return errorHandling(ret);
    }

    public boolean invokePetrifySynthesis(File infile, File logfile, File libfile, String[] params) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petrifycmd);
        if(command == null) {
            logger.error("Could not read petrify cmd String");
            return false;
        }
        String[] last = {"-dead", "-log", logfile.getAbsolutePath(), "-lib", libfile.getAbsolutePath(), infile.getAbsolutePath()};
        String[] newparams = new String[params.length + last.length];
        System.arraycopy(params, 0, newparams, 0, params.length);
        System.arraycopy(last, 0, newparams, params.length, last.length);
        ProcessReturn ret = invoke(command, newparams);
        return errorHandling(ret);
    }

    public boolean invokePetreset(File eqnfile, File stgfile, File libfile, File outfile) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petresetcmd);
        if(command == null) {
            logger.error("Could not read petreset cmd String");
            return false;
        }
        String[] params = new String[]{"-lib", libfile.getAbsolutePath(), "-i", eqnfile.getAbsolutePath(), "-stg", stgfile.getAbsolutePath(), "-verilog", outfile.getAbsolutePath()};
        ProcessReturn ret = invoke(command, params, 60000);
        return errorHandling(ret);
    }

    public boolean invokeASGLogic(File gfile, File vfile, File workingdir, File libfile, File logfile, File zipfile, String resettype, String additionalParams) {
        String[] command = convertCmd(ResynMain.config.toolconfig.asglogiccmd);
        if(command == null) {
            logger.error("Could not read asglogic cmd String");
            return false;
        }

        //@formatter:off
        String[] params = {"-debug", 
            "-w", workingdir.getAbsolutePath(),
            "-out", vfile.getAbsolutePath(),
            "-lib", libfile.getAbsolutePath(), 
            "-log", logfile.getAbsolutePath(), 
            "-zip", zipfile.getAbsolutePath(), 
            "-rst", resettype};
        //@formatter:on
        List<String> params2 = new ArrayList<>(Arrays.asList(params));

        if(additionalParams != null && !additionalParams.equals("")) {
            params2.addAll(Arrays.asList(additionalParams.split(" ")));
        }
        params2.add(gfile.getAbsolutePath());

        ProcessReturn ret = invoke(command, params2); //, 600000); //10min
        return errorHandling(ret);
    }

    public boolean invokeCommand(String cmd) {
        String[] shcommand = convertCmd("sh -c");
        String[] command = new String[]{"$(" + cmd + ")"};
        ProcessReturn ret = invoke(shcommand, command);
        return errorHandling(ret);
    }
}

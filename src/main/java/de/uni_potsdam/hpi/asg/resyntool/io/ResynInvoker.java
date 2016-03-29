package de.uni_potsdam.hpi.asg.resyntool.io;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.Invoker;
import de.uni_potsdam.hpi.asg.common.io.ProcessReturn;
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

//    public boolean invokeBalsaC(String outfile, String infile) {
//        String[] cmd = convertCmd(ResynMain.config.toolconfig.balsaccmd);
//        if(cmd == null) {
//            logger.error("Could not read balsac cmd String");
//        }
//        String[] params = {"-o", ".", infile};
//        ProcessReturn ret = invoke(cmd, params);
//        return errorHandling(ret);
//    }

    public boolean invokeBalsaNetlist(String technology, String filename, String component, List<String> params) {
        String[] cmd = convertCmd(ResynMain.config.toolconfig.balsanetlistcmd);
        if(cmd == null) {
            logger.error("Could not read balsanetlist cmd String");
        }
        String[] params1 = {"-X", technology, "-o", filename, "-t", component};
        List<String> params2 = new ArrayList<String>();
        params2.addAll(Arrays.asList(params1));
        for(String str : params) {
            params2.add(str.replaceAll("\"", ""));
        }
        ProcessReturn ret = invoke(cmd, params2);
        return errorHandling(ret);
    }

    public boolean invokeDesijBreeze(String outfile, String infile, boolean withdeco) {
        String[] params = null;
        if(withdeco) {
            params = new String[]{"-Y", "-g", "operation=breeze", "outfile=" + outfile, infile};
        } else {
            params = new String[]{"-Y", "operation=breeze", "outfile=" + outfile, infile};
        }
        return invokeDesij(Arrays.asList(params));
    }

    public boolean invokeDesijKilldummies(String outfile, String infile) {
        String[] params = {"-Y", "-t", "operation=killdummiesrelaxed", "outfile=" + outfile, infile}; //"-t"
        return invokeDesij(Arrays.asList(params));
    }

    public boolean invokeDesijDecomposition(String decompositionStrategy, String partitionStrategy, String infile) {
        String[] params = {/*"-k",*/ "-Y", "-t", "operation=decompose", "version=" + decompositionStrategy, "partition=" + partitionStrategy, infile}; //"-t"
        return invokeDesij(Arrays.asList(params));
    }

    private boolean invokeDesij(List<String> params) {
        //TODO: timeout for desij

        String[] cmd = convertCmd(ResynMain.config.toolconfig.desijcmd);
        if(cmd == null) {
            logger.error("Could not read desij cmd String");
        }
        ProcessReturn ret = invoke(cmd, params);
        return errorHandling(ret);
    }

    public boolean invokePUNFandMPSAT(String infile, String outfile) {
        String mcifile = infile + ".mci";

        String[] cmd = convertCmd(ResynMain.config.toolconfig.punfcmd);
        if(cmd == null) {
            logger.error("Could not read punf cmd String");
        }
        String[] params = {"-m=" + mcifile, "-f=" + infile};
        ProcessReturn ret = invoke(cmd, params);

        File mci = new File(workingdir, mcifile);
//		logger.debug("punf: " + ret.getCode());
        if(!mci.exists()) { // punf returns != 0 even if there are only warnings
            if(!errorHandling(ret)) {
                logger.error("PUNF Error with " + infile);
                return false;
            }
        }

        File tmpfolder = new File(workingdir + infile + ".tmp");
        if(!tmpfolder.mkdir()) {
            logger.error("Could not create tmp folder for mpsat: " + tmpfolder.getName());
            return false;
        }

        String[] cmd2 = convertCmd(ResynMain.config.toolconfig.mpsatcmd);
        if(cmd2 == null) {
            logger.error("Could not read mpsat cmd String");
        }
        String[] params2 = {"-R", "-f", "-@", "-p0", "-cl", "../" + mcifile};
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

    public boolean invokePetrifyCSC(String infile, String logfile, String outfile) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petrifycmd);
        if(command == null) {
            logger.error("Could not read petrify cmd String");
        }
        String[] params = {"-csc", "-dead", "-o", outfile, "-log", logfile, infile};
        ProcessReturn ret = invoke(command, params);
        return errorHandling(ret);
    }

    public boolean invokePetrifySynthesis(String infile, String logfile, String libfile, String[] params) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petrifycmd);
        if(command == null) {
            logger.error("Could not read petrify cmd String");
        }
        String[] last = {"-dead", "-log", logfile, "-lib", libfile, infile};
        String[] newparams = new String[params.length + last.length];
        System.arraycopy(params, 0, newparams, 0, params.length);
        System.arraycopy(last, 0, newparams, params.length, last.length);
        ProcessReturn ret = invoke(command, newparams);
        return errorHandling(ret);
    }

    public boolean invokePetreset(String eqnfile, String stgfile, String libfile, String outfile) {
        String[] command = convertCmd(ResynMain.config.toolconfig.petresetcmd);
        if(command == null) {
            logger.error("Could not read petreset cmd String");
        }
        String[] params = new String[]{"-lib", libfile, "-i", eqnfile, "-stg", stgfile, "-verilog", outfile};
        ProcessReturn ret = invoke(command, params, 60000);
        return errorHandling(ret);
    }

    public boolean invokeASGLogic(String gfile, String vfile, String workingdir, String libfile, String logfile, String zipfile, String resettype) {
        String[] command = convertCmd(ResynMain.config.toolconfig.asglogiccmd);
        if(command == null) {
            logger.error("Could not read asglogic cmd String");
        }

        String[] params = {"-debug", "-out", vfile, "-w", workingdir, "-lib", libfile, "-log", logfile, "-zip", zipfile, "-rst", resettype, gfile};
        ProcessReturn ret = invoke(command, params); //, 600000); //10min
        return errorHandling(ret);
    }
}

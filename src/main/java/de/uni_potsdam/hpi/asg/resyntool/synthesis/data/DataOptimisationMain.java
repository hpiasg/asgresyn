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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.remote.RemoteInformation;
import de.uni_potsdam.hpi.asg.common.io.technology.SyncTool;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;

public class DataOptimisationMain {
    private static final Logger logger = LogManager.getLogger();

    private RemoteInformation   rinfo;
    private SyncTool            synclib;

    public DataOptimisationMain(String host, String username, String password, String remotefolder, SyncTool synclib) {
        this.rinfo = new RemoteInformation(host, username, password, remotefolder);
        this.synclib = synclib;
    }

    public Set<String> execute(Set<String> files) {

        Set<DataOptimisationPlan> plans = new HashSet<>();
        for(String filename : files) {
            ScriptGenerator gen = new ScriptGenerator(filename, synclib);
            plans.add(gen.generate());
        }

        if(!run(plans)) {
            logger.warn("Data optimisation failed");
            return null;
        }

        Set<String> retVal = new HashSet<>();
        for(DataOptimisationPlan p : plans) {
            if(p.wasOptmisationSuccessfil()) {
                doPostCompileOperation(p.getOptimisedfilename());
                retVal.add(p.getOptimisedfilename());
            } else {
                retVal.add(p.getUnoptimisedfilename());
            }
        }

        return retVal;
    }

    private void doPostCompileOperation(String optimisedfilename) {
        for(String str : synclib.getPostCompileCmds()) {
            String cmd = str.replace("#*VFILE*#", optimisedfilename);
            ResynInvoker.getInstance().invokeCommand(cmd);
        }
    }

    private boolean run(Set<DataOptimisationPlan> plans) {
        Set<String> uploadfiles = new HashSet<>();
        List<String> execScripts = new ArrayList<>();
        for(DataOptimisationPlan p : plans) {
            uploadfiles.addAll(p.getAllUploadFiles());
            execScripts.add(p.getRemoteShScriptName());
        }

        ResynRemoteOperationWorkflow wf = new ResynRemoteOperationWorkflow(rinfo, "dataopt", plans);
        if(!wf.run(uploadfiles, execScripts)) {
            return false;
        }

        return true;
    }
}

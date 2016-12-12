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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.remote.RemoteInformation;
import de.uni_potsdam.hpi.asg.common.remote.SimpleRemoteOperationWorkflow;

public class ResynRemoteOperationWorkflow extends SimpleRemoteOperationWorkflow {
    private static final Logger               logger = LogManager.getLogger();

    private Map<String, DataOptimisationPlan> plansmap;

    public ResynRemoteOperationWorkflow(RemoteInformation rinfo, String subdir, Set<DataOptimisationPlan> plans) {
        super(rinfo, subdir);
        plansmap = new HashMap<>();
        for(DataOptimisationPlan p : plans) {
            plansmap.put(p.getRemoteShScriptName(), p);
        }
    }

    @Override
    protected boolean executeCallBack(String script, int code) {

        DataOptimisationPlan p = plansmap.get(script);
        if(p == null) {
            logger.error("Unknown DataOptPlan");
            return false;
        }

        if(code != 0) {
            logger.warn("Optimisation of " + p.getLocalfilename() + " failed");
            return true;
        }
        p.setOptmisationSuccessfil(true);

        return true;
    }

}

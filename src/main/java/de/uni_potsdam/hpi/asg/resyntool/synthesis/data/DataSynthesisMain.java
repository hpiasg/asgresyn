package de.uni_potsdam.hpi.asg.resyntool.synthesis.data;

/*
 * Copyright (C) 2012 - 2016 Norman Kluge
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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.resyntool.ResynMain;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeProjectResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.ResynType;
import de.uni_potsdam.hpi.asg.resyntool.io.RemoteInvocation;

public class DataSynthesisMain {
    private static final Logger logger    = LogManager.getLogger();

    private static final String opwending = "_data";

    private String              technology;
    private String              file;
    private BreezeProjectResyn  proj;

    public DataSynthesisMain(BreezeProjectResyn proj, String technology) {
        this.technology = technology;
        this.proj = proj;
    }

    public boolean generate() {
        String opwfilename = opwending + FileHelper.getFileEx(Filetype.verilog);

        Set<String> filelist = new HashSet<String>();
        for(ResynType type : proj.getAllResynTypes()) {
            logger.info("Generating " + type.getDef());
            filelist.add(type.generate(technology));
        }

        RemoteInvocation dc = ResynMain.config.toolconfig.designCompilerCmd;
        DataOptimisationMain opt = new DataOptimisationMain(dc.hostname, dc.username, dc.password, dc.workingdir);
        opt.execute(filelist);

        String text = FileHelper.getInstance().mergeFileContents(new ArrayList<>(filelist));
        if(text != null) {
            if(FileHelper.getInstance().writeFile(opwfilename, text)) {
                file = opwfilename;
                return true;
            } else {
                logger.error("Could not export merged datapath file");
                return false;
            }
        } else {
            logger.error("Coult not merge data parts");
            return false;
        }
    }

    public String getFile() {
        return file;
    }
}

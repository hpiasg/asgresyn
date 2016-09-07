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

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.common.technology.Balsa;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.resyntool.ResynMain;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeProjectResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.HSComponentResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.ResynType;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ComponentResyn;
import de.uni_potsdam.hpi.asg.resyntool.io.RemoteInvocation;

public class DataSynthesisMain {
    private static final Logger logger    = LogManager.getLogger();

    private static final String opwending = "_data";

    private Technology          technology;
    private String              file;
    private BreezeProjectResyn  proj;
    private boolean             optimise;

    public DataSynthesisMain(BreezeProjectResyn proj, Technology technology, boolean optimise) {
        this.technology = technology;
        this.proj = proj;
        this.optimise = optimise;
    }

    public boolean generate() {
        String opwfilename = opwending + FileHelper.getFileEx(Filetype.verilog);

        Set<String> filelist = new HashSet<>();
        Set<String> filelist_opt = new HashSet<>();

        Balsa balsatech = technology.getBalsa();
        for(ResynType type : proj.getAllResynTypes()) {
            logger.info("Generating " + type.getDef());
            switch(((ComponentResyn)((HSComponentResyn)type.getType().getComp()).getComp()).getDatapathType()) {
                case DataPath:
                    filelist_opt.add(type.generate(balsatech.getTech() + "/" + balsatech.getStyle()));
                    break;
                case DataPathDoNotOptimise:
                    filelist.add(type.generate(balsatech.getTech() + "/" + balsatech.getStyle()));
                    break;
                case DataPathNotYetImplemented:
                    break;
                case NoDataPath:
                    break;
            }
        }

        if(optimise) {
            RemoteInvocation dc = ResynMain.config.toolconfig.designCompilerCmd;
            if(dc != null && technology.getSynctool() != null) {
                logger.info("Running data path optimsation");
                DataOptimisationMain opt = new DataOptimisationMain(dc.hostname, dc.username, dc.password, dc.workingdir, technology.getSynctool());
                Set<String> optfilelist = opt.execute(filelist_opt);
                if(optfilelist != null) {
                    filelist_opt.clear();
                    filelist_opt = optfilelist;
                }
            } else {
                logger.warn("No data path optimisation tool config found. Omitting optimisation");
            }
        }

        filelist.addAll(filelist_opt);

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

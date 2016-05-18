package de.uni_potsdam.hpi.asg.resyntool.synthesis;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractBreezeNetlist;
import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeNetlistResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeProjectResyn;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.control.ControlSynthesis;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.control.DecompositionBasedSynthesis;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.control.DirectSynthesis;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.control.STWInformation;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.data.DataSynthesisMain;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter.TackleComplexityType;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.wiring.WiringMain;

public class SynthesisMain {
    private static final Logger logger = LogManager.getLogger();

    private BreezeProjectResyn  proj;
    private SynthesisParameter  params;

    private String              file;

    public SynthesisMain(BreezeProjectResyn proj, SynthesisParameter params) {
        this.proj = proj;
        this.params = params;
    }

    public boolean generate() {
        Set<TackleComplexityType> order = params.getTackleComplexityOrder();
        DataSynthesisMain data = null;
        logger.info("------------------------------");
        logger.info("Generate datapath");
        logger.info("------------------------------");
        if(!params.isSkipdatapath()) {
            data = new DataSynthesisMain(proj, params.getTechnology().getBalsa().toString());
            if(!data.generate()) {
                logger.error("Could not generate datapath");
                return false;
            }
        } else {
            logger.info("skipped");
        }
        logger.info("------------------------------");
        logger.info("Generate control");
        logger.info("------------------------------");
        Map<BreezeNetlistResyn, STWInformation> stwmap = new HashMap<BreezeNetlistResyn, STWInformation>();
        for(AbstractBreezeNetlist anetlist : proj.getSortedNetlists()) {
            BreezeNetlistResyn netlist = (BreezeNetlistResyn)anetlist;
            if(netlist.getAllHSInstances().size() == 0) {
                continue;
            }
            if(!netlist.writeOut()) {
                logger.error(netlist.getName() + ": Could not create netlist for desij");
            }
            boolean controlSuccess = false;
            ControlSynthesis control = null;
            for(TackleComplexityType type : order) {
                switch(type) {
                    case straight:
                        logger.info(netlist.getName() + ": Direct synthesis");
                        control = new DirectSynthesis(netlist.getName(), params);
                        break;
                    case decomposition:
                        logger.info(netlist.getName() + ": Synthesis with decomposition");
                        control = new DecompositionBasedSynthesis(netlist.getName(), params);
                        break;
                    case clustering:
                        logger.info("Synthesis with clustering .. not implemented");
                        continue;
                    default:
                        logger.warn("Could not determine Synthesis algorithm: " + type);
                        continue;
                }
                if(control.generate()) {
                    controlSuccess = true;
                    stwmap.put(netlist, control.getStwInfo());
                    logger.info("");
                    break;
                } else {
                    logger.warn("Could not generate controlpath of " + netlist.getName() + " with " + type);
                }
            }
            if(!controlSuccess) {
                logger.error(netlist.getName() + ": None of the methods worked. Could not generate control");
                return false;
            }
        }
        logger.info("------------------------------");
        logger.info("Generate wiring");
        logger.info("------------------------------");
        Map<BreezeNetlistResyn, String> wiringfiles = new HashMap<BreezeNetlistResyn, String>();
        for(AbstractBreezeNetlist anetlist : proj.getSortedNetlists()) {
            BreezeNetlistResyn netlist = (BreezeNetlistResyn)anetlist;
            WiringMain wiring = new WiringMain(netlist, stwmap.get(netlist));
            if(!wiring.generate(params.isSkipdatapath())) {
                logger.error("Could not generate wiring");
                return false;
            }
            wiringfiles.put(netlist, wiring.getFile());
        }

        List<String> files = new ArrayList<String>();
        if(!params.isSkipdatapath()) {
            files.add(data.getFile());
        }
        for(AbstractBreezeNetlist anetlist : proj.getSortedNetlists()) {
            BreezeNetlistResyn netlist = (BreezeNetlistResyn)anetlist;
            if(stwmap.containsKey(netlist)) {
                files.add(stwmap.get(netlist).getStwfile());
            }
            files.add(wiringfiles.get(netlist));
        }
        if(!mergeAll(files)) {
            logger.error("Could not merge files");
            return false;
        }

        return true;
    }

    private boolean mergeAll(List<String> files) {
        String filename = "_all" + FileHelper.getFileEx(Filetype.verilog);

        String text = FileHelper.getInstance().mergeFileContents(files);
        if(text != null) {
            if(FileHelper.getInstance().writeFile(filename, text)) {
                this.file = filename;
                return true;
            } else {
                logger.error("Could not export merged file");
            }
        } else {
            logger.error("Could not merge all");
        }
        return false;
    }

    public String getFile() {
        return file;
    }
}

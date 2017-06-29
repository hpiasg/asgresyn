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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.LogicSynthesisParameter;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;

public class DirectSynthesis extends ControlSynthesis {
    private static final Logger logger = LogManager.getLogger();

    public DirectSynthesis(String name, SynthesisParameter params) {
        super(name, params);
    }

    @Override
    public boolean generate() {
        File workingDir = WorkingdirGenerator.getInstance().getWorkingDir();
        String filename_breeze = name + CommonConstants.BREEZE_FILE_EXTENSION;
        File breezeFile = new File(workingDir, filename_breeze);
        String filename_gfull = name + "_full" + CommonConstants.STG_FILE_EXTENSION;
        File gFullFile = new File(workingDir, filename_gfull);
        String filename_g = name + CommonConstants.STG_FILE_EXTENSION;
        File gFile = new File(workingDir, filename_g);
        balsaSTGFile = gFile;
        String filename_v = name + stwending + CommonConstants.VERILOG_FILE_EXTENSION;
        File vFile = new File(workingDir, filename_v);

        if(ResynInvoker.getInstance().invokeDesijBreeze(gFullFile, breezeFile, false, params.getDesijBreezeExprFile())) {
            if(ResynInvoker.getInstance().invokeDesijKilldummies(gFile, gFullFile)) {
                LogicSynthesis synthesis = new LogicSynthesis(params);
                if(synthesis.synthesise(gFile, vFile)) {
                    String stwInterface = generateSTWInterface(filename_v);
                    if(stwInterface != null) {
                        List<String> stwInterfaces = new ArrayList<String>();
                        stwInterfaces.add(stwInterface);
                        if(params.getLogicSynthesisStrategy().getLogicSynthesis() == LogicSynthesisParameter.LogicSynthesisType.petrify) {
                            if(PetrifyInstanceNameFixer.fix(filename_v)) {
                                stwInfo = STWInformation.create(filename_v, stwInterfaces);
                                return true;
                            } else {
                                logger.error("Fixing of petrify instance names failed");
                                return false;
                            }
                        } else {
                            stwInfo = STWInformation.create(filename_v, stwInterfaces);
                            return true;
                        }
                    } else {
                        logger.error("Interface creation failed");
                        return false;
                    }
                } else {
                    logger.error("Logic synthesis failed with " + filename_g);
                    return false;
                }
            } else {
                logger.error("Killdummies failed with " + filename_gfull);
                return false;
            }
        } else {
            logger.error("Breeze2stg failed with " + filename_breeze);
            return false;
        }
    }
}

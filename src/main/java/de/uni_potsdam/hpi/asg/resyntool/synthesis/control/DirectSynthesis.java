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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
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
        String filename_breeze = name + FileHelper.getFileEx(Filetype.breeze);
        String filename_gfull = name + "_full" + FileHelper.getFileEx(Filetype.stg);
        String filename_g = name + FileHelper.getFileEx(Filetype.stg);
        String filename_v = name + stwending + FileHelper.getFileEx(Filetype.verilog);

        if(ResynInvoker.getInstance().invokeDesijBreeze(filename_gfull, filename_breeze, false, params.getDesijBreezeExprFile())) {
            if(ResynInvoker.getInstance().invokeDesijKilldummies(filename_g, filename_gfull)) {
                LogicSynthesis synthesis = new LogicSynthesis(params);
                if(synthesis.synthesise(filename_g, filename_v)) {
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
                    logger.error("Petrify synthesis failed with " + filename_g);
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

package de.uni_potsdam.hpi.asg.resyntool.synthesis.control;

/*
 * Copyright (C) 2012 - 2014 Norman Kluge
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

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.resyntool.stg.STGInternalSignalFixer;

public class DecompositionSynthesisFuture implements Callable<String> {
    private static final Logger logger = LogManager.getLogger();

    private String              name;
    private LogicSynthesis      synthesis;

    public DecompositionSynthesisFuture(String name, LogicSynthesis synthesis) {
        this.name = name;
        this.synthesis = synthesis;
    }

    @Override
    public String call() {
        try {
            String filename_v = name + FileHelper.getFileEx(Filetype.verilog);
            String filename_g = name + FileHelper.getFileEx(Filetype.stg);

            if(STGInternalSignalFixer.fix(filename_g)) {
                if(synthesis.synthesise(filename_g, filename_v)) {
                    return filename_v;
                } else {
                    logger.error("Logic Synthesis of " + filename_g + " failed");
                    return null;
                }
            } else {
                logger.error("Fixing of " + filename_g + " failed");
                return null;
            }
        } catch(Exception e) {
            //TODO:debug only
            e.printStackTrace();
            return null;
        }
    }
}

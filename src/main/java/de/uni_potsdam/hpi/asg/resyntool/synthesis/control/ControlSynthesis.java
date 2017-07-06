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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.LogicSynthesisParameter.ResetInsertion;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;

public abstract class ControlSynthesis {
    private static final Logger   logger    = LogManager.getLogger();

    protected static final String stwending = "_control";

    private int                   stwid;
    protected String              name;
    protected SynthesisParameter  params;
    protected File                balsaSTGFile;

    protected STWInformation      stwInfo;

    public ControlSynthesis(String name, SynthesisParameter params) {
        this.stwid = 0;
        this.name = name;
        this.params = params;
    }

    public abstract boolean generate();

    protected String generateSTWInterface(String stwfile) {
        return generateSTWInterface(stwfile, "");
    }

    protected String generateSTWInterface(String stwfile, int id, int part) {
        return generateSTWInterface(stwfile, "_" + id + "_" + part);
    }

    protected String generateSTWInterface(String stwfile, String postfix) {

        if(params.getLogicSynthesisStrategy().getReset() == ResetInsertion.petreset) {
            String retVal = "ResynControl_" + name + postfix;

            File file = new File(stwfile);
            if(file.exists()) {
                final Pattern modulePattern = Pattern.compile("module .*(\\(.*\\));");
                List<String> lines = FileHelper.getInstance().readFile(stwfile);
                int linenumber = 0;
                Matcher matcher = null;
                for(String line : lines) {
                    matcher = modulePattern.matcher(line);
                    if(matcher.matches()) {
                        String newline = "module " + retVal + matcher.group(1) + ";";
                        lines.set(linenumber, newline);
                        retVal += " S" + stwid++ + " " + matcher.group(1) + ";";
                        break;
                    }
                    linenumber++;
                }
                if(!FileHelper.getInstance().writeFile(stwfile, lines)) {
                    return null;
                }
                return retVal;
            } else {
                logger.error(stwfile + " not found");
                return "";
            }
        } else if(params.getLogicSynthesisStrategy().getReset() == ResetInsertion.petrify || params.getLogicSynthesisStrategy().getReset() == ResetInsertion.asglogic) {
            String retVal = "ResynControl_" + name + postfix;

            List<String> lines = FileHelper.getInstance().readFile(stwfile);
            if(lines == null) {
                logger.error("Could not read " + stwfile);
                return null;
            }
            StringBuilder text = new StringBuilder();
            boolean headermode = false;
            for(String line : lines) {
                if(line.startsWith("module ")) {
                    headermode = true;
                    text.append("module " + retVal + " (" + FileHelper.getNewline());
                    retVal += " S" + stwid++ + " (";
                } else if(line.startsWith(");")) {
                    headermode = false;
                    text.append(line + FileHelper.getNewline());
                } else if(headermode) {
                    text.append(line + FileHelper.getNewline());
                    retVal += line.trim();
                } else {
                    text.append(line + FileHelper.getNewline());
                }
            }
            retVal += ");";
            if(!FileHelper.getInstance().writeFile(stwfile, text.toString())) {
                return null;
            }

            return retVal;
        } else {
            logger.error("Unknown reset insertion strategy");
            return null;
        }
    }

    public STWInformation getStwInfo() {
        return stwInfo;
    }

    public File getBalsaSTGFile() {
        return balsaSTGFile;
    }
}

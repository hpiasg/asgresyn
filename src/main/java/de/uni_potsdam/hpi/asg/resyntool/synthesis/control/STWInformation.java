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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.Signal;
import de.uni_potsdam.hpi.asg.common.breeze.model.Signal.Direction;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public class STWInformation {
    private static final Logger  logger     = LogManager.getLogger();

    private String               stwfile;
    private List<String>         stwInterfaces;
    private List<Signal>         signals;

    private static final Pattern inpPattern = Pattern.compile("input (.*);.*");
    private static final Pattern outPattern = Pattern.compile("output (.*);.*");

    private STWInformation(String stwfile, List<String> stwInterfaces) {
        this.stwfile = stwfile;
        this.stwInterfaces = stwInterfaces;

        this.signals = new ArrayList<Signal>();
    }

    public static STWInformation create(String stwfile, List<String> stwInterfaces) {
        STWInformation retVal = new STWInformation(stwfile, stwInterfaces);
        if(retVal.extractInterface()) {
            return retVal;
        }
        return null;
    }

    private boolean extractInterface() {
        List<String> text = FileHelper.getInstance().readFile(stwfile);
        if(text == null) {
            logger.error("Could not read file " + stwfile);
            return false;
        }
        for(String line : text) {
            line = line.trim();
            Matcher inpMatcher = inpPattern.matcher(line);
            if(inpMatcher.matches()) {
                signals.add(new Signal(inpMatcher.group(1).trim(), 0, Direction.in));
                continue;
            }
            Matcher outMatcher = outPattern.matcher(line);
            if(outMatcher.matches()) {
                signals.add(new Signal(outMatcher.group(1).trim(), 0, Direction.out));
            }
        }

        return true;
    }

    public String getStwfile() {
        return stwfile;
    }

    public List<Signal> getSignals() {
        return signals;
    }

    public List<String> getStwInterfaces() {
        return stwInterfaces;
    }
}

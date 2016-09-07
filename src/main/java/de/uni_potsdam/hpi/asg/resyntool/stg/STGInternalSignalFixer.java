package de.uni_potsdam.hpi.asg.resyntool.stg;

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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public class STGInternalSignalFixer {
    private static final Logger logger = LogManager.getLogger();

    public static boolean fix(String filename) {
        if(filename != null) {
            return true;
        }

        List<String> lines = FileHelper.getInstance().readFile(filename);
        if(lines == null) {
            logger.error("Could not read file " + filename);
            return false;
        }
        int linenumber = 0;
        int internalline = -1;
        int outline = -1;
        for(String str : lines) {
            if(str.startsWith(".outputs")) {
                outline = linenumber;
            } else if(str.startsWith(".internal")) {
                internalline = linenumber;
            } else if(str.startsWith(".graph")) {
                break;
            }
            linenumber++;
        }
        if(internalline == -1) {
            return true;
        }
        if(outline == -1) {
            outline = internalline + 1;
            lines.add(outline, ".outputs ");
        }
        String signals = lines.get(internalline).substring(10);
        String newoutlinestr = lines.get(outline) + signals;
        lines.set(outline, newoutlinestr);
        lines.remove(internalline);
        return FileHelper.getInstance().writeFile(filename, lines);
    }
}

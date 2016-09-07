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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public class PetrifyInstanceNameFixer {
    private static final Logger  logger           = LogManager.getLogger();

    private static final Pattern endmodulePattern = Pattern.compile("endmodule");
    private static final Pattern badLinePattern   = Pattern.compile("buf _U([0-9]+)(.*)");

    public static boolean fix(String filename) {
        List<String> lines = FileHelper.getInstance().readFile(filename);
        if(lines == null) {
            logger.error("Could not read file " + filename);
            return false;
        }

        Map<Integer, Integer> idnum = new HashMap<Integer, Integer>();
        int linenumber = -1;
        Matcher matcher = null;
        for(String line : lines) {
            linenumber++;
            matcher = endmodulePattern.matcher(line);
            if(matcher.matches()) {
                idnum.clear();
                continue;
            }
            matcher = badLinePattern.matcher(line);
            if(matcher.matches()) {
                int id = Integer.parseInt(matcher.group(1));
                int num = 0;
                if(idnum.containsKey(id)) {
                    num = idnum.get(id) + 1;
                }
                idnum.put(id, num);
                String newline = "buf _U" + id + "_" + num + matcher.group(2);
                lines.set(linenumber, newline);
            }
        }
        if(!FileHelper.getInstance().writeFile(filename, lines)) {
            logger.error("Could not write file " + filename);
            return false;
        }
        return true;

    }
}

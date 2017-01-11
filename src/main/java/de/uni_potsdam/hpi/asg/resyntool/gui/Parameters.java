package de.uni_potsdam.hpi.asg.resyntool.gui;

/*
 * Copyright (C) 2017 Norman Kluge
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

public class Parameters {
    private RunResynFrame frame;

    public enum TextParam {
        BreezeFile, TechLib, OutDir, OutFile, CfgFile, WorkingDir, LogFile, TempFiles
    }

    public enum BooleanParam {
        OptDp, LogLvl0, LogLvl1, LogLvl2, LogLvl3
    }

    public void setFrame(RunResynFrame frame) {
        this.frame = frame;
    }

    private String replaceBasedir(String str) {
        String basedir = System.getProperty("basedir");
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            basedir = basedir.replaceAll("\\\\", "/");
        }
        return str.replaceAll("\\$BASEDIR", basedir);
    }
}

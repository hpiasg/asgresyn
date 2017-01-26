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

import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractBooleanParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractEnumParam;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel.AbstractTextParam;

public class Parameters {
    private RunResynFrame frame;

    //@formatter:off
    public enum TextParam implements AbstractTextParam {
        /*general*/ BreezeFile, TechLib, OutDir, OutFile, CfgFile, WorkingDir, LogFile, TempFiles,
        /*adv*/ Asglogic,
        /*debug*/ BreezeExprFile
    }

    public enum BooleanParam implements AbstractBooleanParam {
        /*general*/ OptDp, LogLvl0, LogLvl1, LogLvl2, LogLvl3,
        /*adv*/ tcS0, tcS1, tcS2, tcD0, tcD1, tcD2, cscP, cscM, synA, synP, tmA, tmP, tmN, rstA, rstP, rstI,
        /*debug*/ debug, tooldebug, sdp, ssc
    }
    
    public enum EnumParam implements AbstractEnumParam {
        /*general*/
        /*adv*/ decoStrat, decoPart
    }
    //@formatter:on

    public static String[] decoStrategies  = {"breeze", "irr-csc-aware", "csc-aware", "tree", "basic", "lazy-multi", "lazy-single"};
    public static String[] partHeuristics  = {"common-cause", "finest", "roughest", "multisignaluse", "avoidcsc", "reduceconc", "lockedsignals", "best"};

    public static String   unsetStr        = "$UNSET";
    public static String   userDirStr      = "$USER-DIR";
    public static String   basedirStr      = "$BASEDIR";
    public static String   outfilebaseName = "$OUTFILE";

    public static String   basedir         = System.getProperty("basedir");

    public void setFrame(RunResynFrame frame) {
        this.frame = frame;
    }

    public String getTextValue(TextParam param) {
        String str = frame.getTextValue(param);
        if(str.equals(Parameters.unsetStr)) {
            return null;
        }
        if(str.equals(Parameters.userDirStr)) {
            return System.getProperty("user.dir");
        }
        String retVal = str.replaceAll("\\" + Parameters.basedirStr, basedir);
        retVal = retVal.replaceAll("\\" + Parameters.outfilebaseName, frame.getTextValue(TextParam.OutFile).replaceAll(".v", ""));
        return retVal;
    }

    public boolean getBooleanValue(BooleanParam param) {
        return frame.getBooleanValue(param);
    }

    public String getEnumValue(EnumParam param) {
        int index = frame.getEnumValue(param);
        switch(param) {
            case decoPart:
                return partHeuristics[index];
            case decoStrat:
                return decoStrategies[index];
            default:
                return null;
        }
    }
}

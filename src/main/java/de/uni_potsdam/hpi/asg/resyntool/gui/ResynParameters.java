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
import de.uni_potsdam.hpi.asg.common.gui.runner.AbstractParameters;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.common.technology.TechnologyDirectory;

public class ResynParameters extends AbstractParameters {
    //@formatter:off
    public enum TextParam implements AbstractTextParam {
        /*general*/ BreezeFile,
        /*adv*/ Asglogic,
        /*debug*/ BreezeExprFile
    }

    public enum BooleanParam implements AbstractBooleanParam {
        /*general*/ TechLibDef,
        /*adv*/ OptDp, tcS0, tcS1, tcS2, tcD0, tcD1, tcD2, cscP, cscM, synA, synP, tmA, tmP, tmN, rstA, rstP, rstI,
        /*debug*/ debug, tooldebug, sdp, ssc
    }
    
    public enum EnumParam implements AbstractEnumParam {
        /*general*/ TechLib,
        /*adv*/ decoStrat, decoPart
    }
    //@formatter:on

    public static String[] decoStrategies = {"breeze", "irr-csc-aware", "csc-aware", "tree", "basic", "lazy-multi", "lazy-single"};
    public static String[] partHeuristics = {"common-cause", "finest", "roughest", "multisignaluse", "avoidcsc", "reduceconc", "lockedsignals", "best"};

    private String         defTech;
    private String[]       techs;

    public ResynParameters(String defTech, TechnologyDirectory techDir) {
        super(CommonConstants.VERILOG_FILE_EXTENSION);
        this.defTech = defTech;
        this.techs = techDir.getTechNames();
    }

    @Override
    public String getEnumValue(AbstractEnumParam param) {
        int index = frame.getEnumValue(param);
        if(param == EnumParam.decoPart) {
            return partHeuristics[index];
        } else if(param == EnumParam.decoStrat) {
            return decoStrategies[index];
        } else if(param == EnumParam.TechLib) {
            return techs[index];
        } else {
            return null;
        }
    }

    public String getDefTech() {
        return defTech;
    }

    public String[] getAvailableTechs() {
        return techs;
    }
}

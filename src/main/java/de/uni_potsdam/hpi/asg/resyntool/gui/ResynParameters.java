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

    public static final String[] DECO_STRATEGIES       = {"breeze", "irr-csc-aware", "csc-aware", "tree", "basic", "lazy-multi", "lazy-single"};
    public static final String[] PARTIONING_HEURISTICS = {"common-cause", "finest", "roughest", "multisignaluse", "avoidcsc", "reduceconc", "lockedsignals", "best"};

    private static final String  DEF_BREEZE_FILE_NAME  = "";
    private static final String  DEF_OUT_FILE_NAME     = "resyn.v";

    //@formatter:off
    public enum TextParam implements AbstractTextParam {
        /*general*/ BreezeFile,
        /*adv*/ Asglogic,
        /*debug*/ BreezeExprFile
    }

    public enum BooleanParam implements AbstractBooleanParam {
        /*general*/ DefTechLib,
        /*adv*/ OptDp, tcS0, tcS1, tcS2, tcD0, tcD1, tcD2, cscP, cscM, synA, synP, tmA, tmP, tmN, rstA, rstP, rstI,
        /*debug*/ debug, tooldebug, sdp, ssc
    }
    
    public enum EnumParam implements AbstractEnumParam {
        /*general*/ TechLib,
        /*adv*/ decoStrat, decoPart
    }
    //@formatter:on

    private String   defTech;
    private String[] techs;
    private boolean  forceTech;
    private String   defBreezeFileName;
    private String   defOutDirName;
    private String   defOutFileName;

    public ResynParameters(String defTech, TechnologyDirectory techDir) {
        super(CommonConstants.VERILOG_FILE_EXTENSION);
        this.defTech = defTech;
        this.techs = techDir.getTechNames();
        this.forceTech = false;
        this.defBreezeFileName = DEF_BREEZE_FILE_NAME;
        this.defOutDirName = AbstractParameters.DEF_OUT_DIR;
        this.defOutFileName = DEF_OUT_FILE_NAME;
    }

    public ResynParameters(String defTech, TechnologyDirectory techDir, boolean forceTech, String breezeFile, String outDir, String outFile) {
        super(CommonConstants.VERILOG_FILE_EXTENSION);
        this.defTech = defTech;
        this.techs = techDir.getTechNames();
        this.forceTech = true;
        this.defBreezeFileName = breezeFile;
        this.defOutDirName = outDir;
        this.defOutFileName = outFile;
    }

    @Override
    public String getEnumValue(AbstractEnumParam param) {
        int index = mainpanel.getEnumValue(param);
        if(param == EnumParam.decoPart) {
            return PARTIONING_HEURISTICS[index];
        } else if(param == EnumParam.decoStrat) {
            return DECO_STRATEGIES[index];
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

    public String getDefBreezeFileName() {
        return defBreezeFileName;
    }

    public String getDefOutFileName() {
        return defOutFileName;
    }

    public String getDefOutDirName() {
        return defOutDirName;
    }

    public boolean isForceTech() {
        return forceTech;
    }
}

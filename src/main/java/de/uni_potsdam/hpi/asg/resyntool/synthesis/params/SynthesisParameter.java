package de.uni_potsdam.hpi.asg.resyntool.synthesis.params;

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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.technology.Technology;

public class SynthesisParameter {

    public enum TackleComplexityType {
        straight, decomposition, clustering
    }

    private static final Logger       logger = LogManager.getLogger();

    private String                    decoStrategy;
    private String                    partitionHeuristics;
    private LogicSynthesisParameter   logicSynthesisStrategy;
    private Technology                technology;
    private Set<TackleComplexityType> tackleComplexityOrder;
    private boolean                   skipdatapath;
    private boolean                   optimisedatapath;
    private String                    asglogicparams;
    private File                      desijBreezeExprFile;

    public static SynthesisParameter create(Technology tech, String tackleComplexityOrder, String logicSynthesisStrategy, String decoStrategy, String partitionHeuristics, boolean skipdatapath, String asglogiparams, boolean optimisedatapath, File desijBreezeExprFile) {

        SynthesisParameter retVal = new SynthesisParameter();
        retVal.decoStrategy = decoStrategy;
        retVal.partitionHeuristics = partitionHeuristics;
        retVal.logicSynthesisStrategy = LogicSynthesisParameter.parse(logicSynthesisStrategy);
        if(retVal.logicSynthesisStrategy == null) {
            logger.error("LogicSynthesisStrategy null");
            return null;
        }
        retVal.technology = tech;
        retVal.tackleComplexityOrder = evalTackleComplexityOrder(tackleComplexityOrder);
        if(retVal.tackleComplexityOrder == null) {
            logger.error("Could not parse TackleComplexityOrder");
            return null;
        }
        retVal.skipdatapath = skipdatapath;
        retVal.optimisedatapath = optimisedatapath;
        retVal.asglogicparams = asglogiparams;
        retVal.desijBreezeExprFile = desijBreezeExprFile;
        return retVal;
    }

    private static Set<TackleComplexityType> evalTackleComplexityOrder(String tackleComplexityOrder) {
        final int cap = 3;
        Set<TackleComplexityType> retVal = new LinkedHashSet<TackleComplexityType>(cap);
        StringBuilder retValStr = new StringBuilder(cap);
        boolean isValid = true;
        if(tackleComplexityOrder != null) {
            for(char c : tackleComplexityOrder.toCharArray()) {
                if((c == 'S') || (c == 's')) {
                    if(!retVal.contains(TackleComplexityType.straight)) {
                        retVal.add(TackleComplexityType.straight);
                        retValStr.append("S");
                    } else {
                        isValid = false;
                    }
                } else if((c == 'D') || (c == 'd')) {
                    if(!retVal.contains(TackleComplexityType.decomposition)) {
                        retVal.add(TackleComplexityType.decomposition);
                        retValStr.append("D");
                    } else {
                        isValid = false;
                    }
                } else if((c == 'C') || (c == 'c')) {
                    if(!retVal.contains(TackleComplexityType.clustering)) {
                        retVal.add(TackleComplexityType.clustering);
                        retValStr.append("C");
                    } else {
                        isValid = false;
                    }
                } else {
                    isValid = false;
                }
            }
        }

        if(retVal.size() == 0) {
            isValid = false;
            retValStr.append("SD");
            retVal.add(TackleComplexityType.straight);
            retVal.add(TackleComplexityType.decomposition);
        }

        if(!isValid) {
            logger.warn("Synthesis order String not valid: \"" + tackleComplexityOrder + "\". Using: " + retValStr.toString());
        }
        return retVal;
    }

    public String getDecoStrategy() {
        return decoStrategy;
    }

    public String getPartitionHeuristics() {
        return partitionHeuristics;
    }

    public Technology getTechnology() {
        return technology;
    }

    public Set<TackleComplexityType> getTackleComplexityOrder() {
        return tackleComplexityOrder;
    }

    public LogicSynthesisParameter getLogicSynthesisStrategy() {
        return logicSynthesisStrategy;
    }

    public boolean isSkipdatapath() {
        return skipdatapath;
    }

    public boolean isOptimisedatapath() {
        return optimisedatapath;
    }

    public String getAsglogicparams() {
        return asglogicparams;
    }

    public File getDesijBreezeExprFile() {
        return desijBreezeExprFile;
    }
}

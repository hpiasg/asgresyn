package de.uni_potsdam.hpi.asg.resyntool.components;

/*
 * Copyright (C) 2015 Norman Kluge
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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractBreezeProject;
import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractHSComponent;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ComponentResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ComponentsResyn;

public class BreezeProjectResyn extends AbstractBreezeProject {
    private static final Logger logger = LogManager.getLogger();

    public static BreezeProjectResyn create(File rootfile, String componentconfig, boolean skipUndefinedComponents, boolean skipSubComponents) {
        BreezeProjectResyn retVal = new BreezeProjectResyn();
        if(!retVal.readComponentsList(componentconfig)) {
            return null;
        }
        if(!BreezeNetlistResyn.create(rootfile, skipUndefinedComponents, skipSubComponents, retVal)) {
            logger.error("Could not create Breeze netlist for " + rootfile);
            return null;
        }
        return retVal;
    }

    private boolean readComponentsList(String componentconfig) {
        ComponentsResyn components = ComponentsResyn.readIn(componentconfig);
        if(components != null) {
            for(ComponentResyn comp : components.getComponents()) {
                componentList.put(comp.getBreezename(), new HSComponentResyn(comp));
            }
            return true;
        }
        logger.error("Componentlist not found");
        return false;
    }

    public List<ResynType> getAllResynTypes() {
        List<ResynType> retVal = new ArrayList<ResynType>();
        for(AbstractHSComponent comp : componentList.values()) {
            retVal.addAll(((HSComponentResyn)comp).getResynTypes());
        }
        return retVal;
    }
}

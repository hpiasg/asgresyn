package de.uni_potsdam.hpi.asg.resyntool.components;

/*
 * Copyright (C) 2012 - 2015 Norman Kluge
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractBreezeNetlist;
import de.uni_potsdam.hpi.asg.common.breeze.model.AbstractHSComponent;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentInst;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentType;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Parameter;
import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Parameter.ParameterType;
import de.uni_potsdam.hpi.asg.common.breeze.parser.breezefile.BreezeComponentElement;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ComponentResyn;

public class HSComponentResyn extends AbstractHSComponent {
    private static final Logger                      logger = LogManager.getLogger();

    private Map<String, ResynType>                   resynTypeMap;
    private Map<BreezeNetlistResyn, List<ResynInst>> resynInstances;
    private int                                      resyntypeid;

    public HSComponentResyn(ComponentResyn comp) {
        super(comp);
        resyntypeid = 0;
        resynTypeMap = new HashMap<String, ResynType>();
        resynInstances = new HashMap<BreezeNetlistResyn, List<ResynInst>>();
    }

    @Override
    public boolean createInstance(BreezeComponentElement be, AbstractBreezeNetlist netlist) {
        HSComponentType type = internalCreateInstanceType(be);
        if(type == null) {
            return false;
        }
        HSComponentInst inst = internalCreateInstanceInst(be, netlist, type);
        if(inst == null) {
            return false;
        }

        ResynType resyntype = null;
        ComponentResyn comp2 = (ComponentResyn)comp;

        if(resynTypeMap.isEmpty()) {
            if(comp2.isDatapathmissing()) {
                logger.warn("Datapath defition missing for component " + comp.getBreezename());
            }
        }

        if(comp2.getInterfacesignals() != null) {
            String resynidstr = getResynID(be);
            if(resynTypeMap.containsKey(resynidstr)) {
                resyntype = (ResynType)resynTypeMap.get(resynidstr);
            }
            if(resyntype == null) {
                resyntype = ResynType.create(be.parameters, comp.getBreezename(), type, comp2.getRemovesignals(), resyntypeid++);
                if(resyntype == null) {
                    return false;
                }
                resynTypeMap.put(resynidstr, resyntype);
            }
        }

        if(comp2.getInterfacesignals() != null) {
            ResynInst resyninst = ResynInst.create(inst, type, resyntype, comp2.getInterfacesignals());
            if(resyninst == null) {
                return false;
            }

            if(!resynInstances.containsKey(netlist)) {
                resynInstances.put((BreezeNetlistResyn)netlist, new ArrayList<ResynInst>());
            }
            resynInstances.get(netlist).add(resyninst);
        }

        return true;
    }

    private String getResynID(BreezeComponentElement be) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        if(comp.getParameters() != null) {
            for(Object o : be.parameters) {
                Parameter param = comp.getParameters().getParameter(i);
                if(param != null) {
                    if(param.getType() != ParameterType.name) {
                        str.append(o.toString());
                    }
                }
                i++;
            }
        }
        return str.toString();
    }

    public List<ResynInst> getResynInstances(BreezeNetlistResyn netlist) {
        if(!resynInstances.containsKey(netlist)) {
            resynInstances.put(netlist, new ArrayList<ResynInst>());
        }
        return resynInstances.get(netlist);
    }

    public List<ResynType> getResynTypes() {
        return new ArrayList<ResynType>(resynTypeMap.values());
    }
}

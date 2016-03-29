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
import java.util.List;

import de.uni_potsdam.hpi.asg.common.breeze.model.HSChannel;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentInst;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentType;
import de.uni_potsdam.hpi.asg.common.breeze.model.Signal;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSChannel.DataType;
import de.uni_potsdam.hpi.asg.common.breeze.model.Signal.Direction;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.IGroup;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ISignal;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.InterfaceSignals;

public class ResynInst {
    private List<Signal>     signals;
    private HSComponentInst  inst;
    private HSComponentType  type;
    private ResynType        resyntype;
    private InterfaceSignals isignals;

    private ResynInst(HSComponentInst inst, HSComponentType type, ResynType resyntype, InterfaceSignals isignals) {
        this.inst = inst;
        this.type = type;
        this.resyntype = resyntype;
        this.isignals = isignals;
    }

    public static ResynInst create(HSComponentInst inst, HSComponentType type, ResynType resyntype, InterfaceSignals isignals) {
        ResynInst retVal = new ResynInst(inst, type, resyntype, isignals);
        return retVal;
    }

    private void generateSignals() {
        signals = new ArrayList<Signal>();
        if(isignals != null) {
            int i = 0;
            boolean found = false;
            do {
                found = false;
                ISignal isig = isignals.getSignal(i);
                if(isig != null) {
                    found = true;
                    signals.addAll(convert(isig));
                } else {
                    IGroup igroup = isignals.getGroup(i);
                    if(igroup != null) {
                        found = true;
                        signals.addAll(convert(igroup));
                    }
                }
                i++;
            } while(found);
        }
    }

    private List<Signal> convert(IGroup igroup) {
        List<List<Signal>> temp = new ArrayList<List<Signal>>();
        List<Signal> retVal = new ArrayList<Signal>();
        boolean found = false;
        int i = 0;
        do {
            found = false;
            ISignal isig = igroup.getSignal(i++);
            if(isig != null) {
                found = true;
                temp.add(convert(isig));
            }
        } while(found);

        for(i = 0; i < temp.get(0).size(); i++) {
            for(List<Signal> list : temp) {
                retVal.add(list.get(i));
            }
        }

        return retVal;
    }

    private List<Signal> convert(ISignal isig) {
        List<Signal> retVal = new ArrayList<Signal>();
        String name;
        int width;
        Direction direction;

        switch(isig.getReftype()) {
            case channel:
                List<HSChannel> chanlist = inst.getChan(isig.getRef());
                int j = 0;
                for(HSChannel chan : chanlist) {
                    String jstr = (j++ == 0) ? "" : Integer.toString(j);
                    name = isig.getName().replace("#", jstr);
                    name = name.replace("$", Integer.toString(chan.getId()));
                    width = chan.getDatawidth();
                    if(chan.getActive() == inst) {
                        if(chan.getDatatype() == DataType.pull) {
                            direction = Direction.in;
                        } else {
                            direction = Direction.out;
                        }
                    } else {
                        if(chan.getDatatype() == DataType.pull) {
                            direction = Direction.out;
                        } else {
                            direction = Direction.in;
                        }
                    }
                    retVal.add(new Signal(name, width, direction));
                }
                break;
            case parameter:
                int value = (Integer)type.getParamValue(isig.getRef());
                width = 0;
                direction = (isig.getDirection() == ISignal.Direction.in) ? Direction.in : Direction.out;
                for(j = 0; j < value; j++) {
                    String jstr = (j == 0) ? "" : Integer.toString(j);
                    name = isig.getName().replace("#", jstr);
                    name = name.replace("$", Integer.toString(inst.getNewId()));
                    retVal.add(new Signal(name, width, direction));
                }
                break;
            case component:
                name = isig.getName().replace("$", Integer.toString(inst.getNewId()));
                ;
                width = 0;
                direction = (isig.getDirection() == ISignal.Direction.in) ? Direction.in : Direction.out;
                retVal.add(new Signal(name, width, direction));
                break;
            case reset:
                retVal.add(new Signal("_reset", 0, Direction.in));
        }
        return retVal;
    }

    public List<Signal> getSignals() {
        if(signals == null) {
            generateSignals();
        }
        return signals;
    }

    public ResynType getResyntype() {
        return resyntype;
    }

    public HSComponentInst getInst() {
        return inst;
    }
}

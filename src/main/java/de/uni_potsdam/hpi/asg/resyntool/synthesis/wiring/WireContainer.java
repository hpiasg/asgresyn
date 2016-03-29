package de.uni_potsdam.hpi.asg.resyntool.synthesis.wiring;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.Signal;
import de.uni_potsdam.hpi.asg.common.breeze.model.Signal.Direction;

public class WireContainer {
    private static final Logger logger = LogManager.getLogger();

    private Map<String, Wire>   wires;

    public enum Connector {
        writer, reader
    }

    public WireContainer() {
        wires = new HashMap<String, Wire>();
    }

    public boolean addSignal(Signal sig) {
        Wire wire = null;
        if(wires.containsKey(sig.getName())) {
            wire = wires.get(sig.getName());
        } else {
            wire = new Wire(sig.getName());
            wires.put(sig.getName(), wire);
        }
        if(wire != null) {
            if(wire.getWidth() == -1) {
                wire.setWidth(sig.getWidth());
            } else if(wire.getWidth() != sig.getWidth()) {
                logger.error("Signal " + sig.getName() + ": There are two different widths: " + wire.getWidth() + " and " + sig.getWidth());
                return false;
            }
            setConnector(wire, sig.getDirection());
            return true;

        } else {
            logger.error("Could not add/find Signal " + sig.getName());
            return false;
        }
    }

    private void setConnector(Wire wire, Direction dir) {
        switch(dir) {
            case in:
                wire.addReader();
                break;
            case out:
                wire.addWriter();
                break;
        }
    }

    public Collection<Wire> getWires() {
        return wires.values();
    }
}

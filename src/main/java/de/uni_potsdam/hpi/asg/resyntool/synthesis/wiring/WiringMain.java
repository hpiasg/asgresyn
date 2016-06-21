package de.uni_potsdam.hpi.asg.resyntool.synthesis.wiring;

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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.BreezeNetlistInst;
import de.uni_potsdam.hpi.asg.common.breeze.model.HSChannel;
import de.uni_potsdam.hpi.asg.common.breeze.model.PortComponent;
import de.uni_potsdam.hpi.asg.common.breeze.model.Signal;
import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeNetlistResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.ResynInst;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.control.STWInformation;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.wiring.WireName.Type;

public class WiringMain {
    private static final Logger logger    = LogManager.getLogger();

    private static final String stkending = "_wiring";

    private STWInformation      stwInfo;
    private BreezeNetlistResyn  netlist;

    private String              file;

    public WiringMain(BreezeNetlistResyn netlist, STWInformation stwInfo) {
        this.stwInfo = stwInfo;
        this.netlist = netlist;
    }

    public boolean generate(boolean skipdatapath) {
        List<Signal> allSignals = new ArrayList<Signal>();
        if(stwInfo != null) {
            allSignals.addAll(stwInfo.getSignals());
        }
        if(!skipdatapath) {
            for(ResynInst comp : netlist.getAllResynInstances()) {
                allSignals.addAll(comp.getSignals());
            }
        }
        for(BreezeNetlistInst inst : netlist.getSubBreezeInst()) {
            allSignals.addAll(inst.getSignals());
        }

        List<Integer> interfaceChannels = new ArrayList<Integer>();
        for(PortComponent port : netlist.getAllPorts()) {
            for(HSChannel chan : port.getControlIn()) {
                interfaceChannels.add(chan.getId());
            }
            for(HSChannel chan : port.getControlOut()) {
                interfaceChannels.add(chan.getId());
            }
            for(HSChannel chan : port.getDataIn()) {
                interfaceChannels.add(chan.getId());
            }
            for(HSChannel chan : port.getDataOut()) {
                interfaceChannels.add(chan.getId());
            }
        }

        SortedSet<WireName> interfaceSet = new TreeSet<WireName>();
        SortedSet<WireName> inputControl = new TreeSet<WireName>();
        SortedSet<WireName> inputData = new TreeSet<WireName>();
        SortedSet<WireName> outputControl = new TreeSet<WireName>();
        SortedSet<WireName> outputData = new TreeSet<WireName>();
        SortedSet<WireName> controlWires = new TreeSet<WireName>();
        SortedSet<WireName> dataWires = new TreeSet<WireName>();

        WireContainer con = new WireContainer();
        for(Signal sig : allSignals) {
            con.addSignal(sig);
        }
        for(Wire wire : con.getWires()) {
            if((interfaceChannels.contains(wire.getWireName().getChan()) && wire.getWireName().getId() == -1 && (wire.getWireName().getType() == Type.r || wire.getWireName().getType() == Type.a || wire.getWireName().getType() == Type.d)) || wire.getWireName().getType() == Type.reset) {
                int width = wire.getWidth();
                if(wire.getWriter() == 0) {
                    if(width == 0) {
                        inputControl.add(wire.getWireName());
                    } else if(width > 0) {
                        inputData.add(wire.getWireName());
                    } else {
                        logger.error("Could not determine width of input " + netlist.getName() + ":" + wire.getName());
                        return false;
                    }
                } else {
                    if(width == 0) {
                        outputControl.add(wire.getWireName());
                    } else if(width > 0) {
                        outputData.add(wire.getWireName());
                    } else {
                        logger.error("Could not determine width of output " + netlist.getName() + ":" + wire.getName());
                        return false;
                    }
                }
                interfaceSet.add(wire.getWireName());
            } else {
                if(wire.getWriter() > 1) {
                    logger.error("Wire has more than one driver: " + netlist.getName() + ":" + wire.getName());
                    return false;
                } else if(wire.getWriter() == 0) {
                    logger.warn("Non-input with zero drivers: " + netlist.getName() + ":" + wire.getName());
                } else if(wire.getReader() == 0) {
                    if(wire.getWireName().getType() == Type.internal) {
                        controlWires.add(wire.getWireName());
                    } else {
                        controlWires.add(wire.getWireName());
                        logger.warn("Non-output with no readers: " + netlist.getName() + ":" + wire.getName());
                    }
                } else {
                    int width = wire.getWidth();
                    if(width == 0) {
                        controlWires.add(wire.getWireName());
                    } else if(width > 0) {
                        dataWires.add(wire.getWireName());
                    } else {
                        logger.error("Could not determine width of wire " + wire.getName());
                        return false;
                    }
                }
            }
        }

        //header
        StringBuilder text = new StringBuilder();
        text.append("module Balsa_" + netlist.getName() + " (");
        for(WireName str : interfaceSet) {
            text.append(str.getStr() + ", ");
        }
        text = new StringBuilder(text.substring(0, text.length() - 2));
        text.append(");" + FileHelper.getNewline() + FileHelper.getNewline());

        //interface
        text.append("  input ");
        for(WireName wirename : inputControl) {
            text.append(wirename.getStr() + ", ");
        }
        text = new StringBuilder(text.substring(0, text.length() - 2));
        text.append(";" + FileHelper.getNewline());
        for(WireName wirename : inputData) {
            Wire wire = wirename.getWire();
            text.append("  input [" + (wire.getWidth() - 1) + ":0] " + wire.getName() + ";" + FileHelper.getNewline());
        }
        text.append("  output ");
        for(WireName wirename : outputControl) {
            text.append(wirename.getStr() + ", ");
        }
        text = new StringBuilder(text.substring(0, text.length() - 2));
        text.append(";" + FileHelper.getNewline());
        for(WireName wirename : outputData) {
            Wire wire = wirename.getWire();
            text.append("  output [" + (wire.getWidth() - 1) + ":0] " + wire.getName() + ";" + FileHelper.getNewline());
        }
        text.append(FileHelper.getNewline());

        //wire
        if(controlWires.size() > 0) {
            text.append("  // Control wires" + FileHelper.getNewline());
            text.append("  wire ");
            WireName prev = controlWires.first();
            for(WireName wirename : controlWires) {
                if(wirename.getChan() != prev.getChan()) {
                    text = new StringBuilder(text.substring(0, text.length() - 2));
                    text.append(";" + FileHelper.getNewline() + "  wire ");
                }
                text.append(wirename.getStr() + ", ");
                prev = wirename;
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            text.append(";" + FileHelper.getNewline());
        }
        if(!skipdatapath) {
            text.append(FileHelper.getNewline() + "  // Data wires" + FileHelper.getNewline());
            for(WireName wirename : dataWires) {
                Wire wire = wirename.getWire();
                if(wire.getWidth() == 1) {
                    text.append("  wire " + wire.getName() + ";" + FileHelper.getNewline());
                } else {
                    text.append("  wire [" + (wire.getWidth() - 1) + ":0] " + wire.getName() + ";" + FileHelper.getNewline());
                }
            }
        }

        // Subcomponents
        if(netlist.getSubBreezeInst().size() > 0) {
            text.append(FileHelper.getNewline() + "  // Sub components" + FileHelper.getNewline());
            int id = 0;
            for(BreezeNetlistInst inst : netlist.getSubBreezeInst()) {
                text.append("  " + "resyn_" + inst.getInstantiatedNetlist().getName() + " X" + (id++) + " (");
                SortedSet<WireName> wirenames = new TreeSet<WireName>();
                for(Signal sig : inst.getSignals()) {
                    Wire w = new Wire(sig.getName());
                    wirenames.add(w.getWireName());
                }
                for(WireName wn : wirenames) {
                    text.append(wn.getStr() + ",");
                }
                text.replace(text.length() - 1, text.length(), ");" + FileHelper.getNewline());
            }
        }

        // stw
        if(stwInfo != null) {
            text.append(FileHelper.getNewline() + "  // Control" + FileHelper.getNewline());
            for(String stwInterface : stwInfo.getStwInterfaces()) {
                text.append("  " + stwInterface + FileHelper.getNewline());
            }
        }

        // opw
        if(!skipdatapath) {
            text.append(FileHelper.getNewline() + "  // Data" + FileHelper.getNewline());
            for(ResynInst comp : netlist.getAllResynInstances()) {
                text.append("  " + comp.getResyntype().getDef() + " I" + comp.getInst().getNewId() + " (");
                for(Signal sig : comp.getSignals()) {
                    text.append(sig.getName() + ", ");
                }
                text = new StringBuilder(text.substring(0, text.length() - 2));
                text.append(");" + FileHelper.getNewline());
            }
        }

        text.append(FileHelper.getNewline() + "endmodule");

        String filename = netlist.getName() + stkending + FileHelper.getFileEx(Filetype.verilog);
        if(!FileHelper.getInstance().writeFile(filename, text.toString())) {
            logger.error("Could not write wiremainfile");
            return false;
        }

        this.file = filename;
        return true;
    }

    public String getFile() {
        return file;
    }
}

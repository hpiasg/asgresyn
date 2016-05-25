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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.breeze.model.HSComponentType;
import de.uni_potsdam.hpi.asg.common.io.FileHelper;
import de.uni_potsdam.hpi.asg.common.io.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.resyntool.components.HSSignal.Signaltype;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.ComponentResyn;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.RSignal;
import de.uni_potsdam.hpi.asg.resyntool.components.xml.RemoveSignals;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;

public class ResynType {
    private static final Logger logger = LogManager.getLogger();
    private static final String prefix = "Resyn";

    private String              def;
    private String              balsaNetlistStr;
    private List<String>        balsaNetlistParams;
    private List<HSSignal>      removeSignals;
    private HSComponentType     type;

    private ResynType() {
        balsaNetlistParams = new ArrayList<String>();
    }

    public static ResynType create(LinkedList<Object> beparams, String breezeStr, HSComponentType type, RemoveSignals rsignals, int typeid) {
        ResynType retVal = new ResynType();
        retVal.type = type;

        retVal.balsaNetlistStr = breezeStr.replace("Brz", "");
        retVal.def = prefix + retVal.balsaNetlistStr + "_" + typeid;

        for(Object o : beparams) {
            retVal.balsaNetlistParams.add(o.toString());
        }
        retVal.removeSignals = new ArrayList<HSSignal>();
        if(rsignals != null) {
            if(rsignals.getRsignals() != null) {
                for(RSignal sig : rsignals.getRsignals()) {
                    Signaltype sigtype = null;
                    switch(sig.getType()) {
                        case ack:
                            sigtype = Signaltype.acknowledge;
                            break;
                        case node:
                            sigtype = Signaltype.node;
                            break;
                        case req:
                            sigtype = Signaltype.request;
                            break;
                        default:
                            logger.error("Unknown Signaltype " + sig.getType());
                            return null;
                    }
                    retVal.removeSignals.add(new HSSignal(sig.getName(), sigtype));
                }
            }
        }
        return retVal;

    }

    public String generate(String technology) {
        String filename = def + FileHelper.getFileEx(Filetype.verilog);
        ResynInvoker.getInstance().invokeBalsaNetlist(technology, filename, balsaNetlistStr, balsaNetlistParams);
        List<String> text = FileHelper.getInstance().readFile(filename);
        if(text == null) {
            logger.error("Could not read file " + filename);
            return null;
        }
        renameModule(def, text);
        removeSignals(text, removeSignals);
        resetHack(text);
        checkHeaderSyntax(text);
        if(!FileHelper.getInstance().writeFile(filename, text)) {
            logger.error("Could not write file " + filename);
            return null;
        }
        return filename;
    }

    private void resetHack(List<String> text) {
        ComponentResyn comp = (ComponentResyn)this.type.getComp().getComp();
        String resetNode = comp.getResetNode();
        if(resetNode != null) {
            boolean headermode = false;
            boolean inputlinefound = false;
            int i = 0;
            while(i < text.size()) {
                String line = text.remove(i);
                if(line.startsWith("module Brz") || line.startsWith("module " + prefix)) {
                    headermode = true;
                } else if(headermode == true) {
                    if(line.startsWith(");")) {
                        headermode = false;
                        String line2 = text.remove(i - 1);
                        text.add(i - 1, line2 + ", " + resetNode + "_0n");
                    }
                } else {
                    if(line.trim().equals("wire " + resetNode + "_0n;")) {
                        line = line.replace("wire", "input");
                        inputlinefound = true;
                    }
                }
                if(line != null) {
                    text.add(i++, line);
                }
                if(inputlinefound) {
                    break;
                }
            }
        }
    }

    protected List<String> removeSignals(List<String> text, List<HSSignal> signals) {
        boolean headermode = false;
        int i = 0;
        while(i < text.size()) {
            String line = text.remove(i);
            if(line.startsWith("module Brz") || line.startsWith("module " + prefix)) {
                headermode = true;
            } else if(headermode == true) {
                if(line.startsWith(");")) {
                    headermode = false;
                } else {
                    for(HSSignal sig : signals) {
                        line = line.replaceFirst(sig.getRegExp(), "");
                    }
                }
            } else {
                for(HSSignal sig : signals) {
                    if(line.matches(".*" + sig.getRegExp() + ".*")) {
                        line = null;
                        break;
                    }
                }
            }
            if(line != null) {
                text.add(i++, line);
            }
        }
        return text;
    }

    protected List<String> renameSignals(List<String> text, Map<HSSignal, String> signals) {
        int i = 0;
        while(i < text.size()) {
            String line = text.remove(i);
            for(Entry<HSSignal, String> entry : signals.entrySet()) {
                boolean found;
                Pattern pattern = Pattern.compile(entry.getKey().getRegExp());
                do {
                    found = false;
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.find()) {
                        found = true;
                        int id = Integer.parseInt(matcher.group(1));
                        String idStr = (id == 0) ? "" : String.valueOf(id);
                        line = matcher.replaceFirst(entry.getValue() + idStr);
                    }
                } while(found);
            }
            text.add(i++, line);
        }
        return text;
    }

    protected List<String> renameModule(String name, List<String> text) {
        for(int i = 0; i < text.size(); i++) {
            String line = text.get(i);
            if(line.startsWith("module Brz")) {
                line = "module " + name + " (";
                text.remove(i);
                text.add(i, line);
                break;
            }
        }
        return text;
    }

    protected List<String> checkHeaderSyntax(List<String> text) {
        boolean headermode = false;
        int i = 0;
        while(i < text.size()) {
            String line = text.remove(i);
            if(line.startsWith("module Brz") || line.startsWith("module " + prefix)) {
                headermode = true;
            } else if(headermode == true) {
                if(line.startsWith(");")) {
                    String prevline = text.remove(i - 1).replaceAll("\\s+$", "");
                    if(prevline.endsWith(",")) {
                        prevline = prevline.substring(0, prevline.length() - 1);
                    }
                    text.add(i - 1, prevline);
                    text.add(i, line);
                    break;
                } else {
                    while(line.matches("\\s*, .*")) {
                        line = line.replaceFirst(", ", "");
                    }
                    Pattern pattern = Pattern.compile(", , ");
                    boolean found;
                    do {
                        found = false;
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()) {
                            line = matcher.replaceFirst(", ");
                            found = true;
                        }
                    } while(found);
                    line = line.replaceAll("\\s+$", "");
                    while(line.matches(".*, ,")) {
                        line = line.replaceFirst(", ,", ",");
                    }

                    if(line.matches("\\s*,*")) {
                        line = null;
                    }
                }
            }
            if(line != null) {
                text.add(i++, line);
            }
        }
        return text;
    }

    public String getDef() {
        return def;
    }

    public HSComponentType getType() {
        return type;
    }
}

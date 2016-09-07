package de.uni_potsdam.hpi.asg.resyntool.stg;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;

public class STGSplitter {
    private static final Logger        logger      = LogManager.getLogger();

    private List<String>               inputs      = null;
    private List<String>               outputs     = null;
    private List<String>               dummies     = null;
    private List<String>               internals   = null;
    private List<Marking>              markings    = null;
    private List<List<GraphElement>>   graphlines  = null;
    private Map<Integer, List<String>> clusterlist = null;

    private List<String>               newFiles    = null;

    public static STGSplitter create(String filename) {
        STGSplitter retVal = new STGSplitter();
        retVal.newFiles = new ArrayList<String>();
        if(retVal.readGFile(filename)) {
            retVal.split();
            if(retVal.clusterlist.size() > 1) {
                if(retVal.writeGFiles(filename.substring(0, filename.length() - 2))) {
                    return retVal;
                } else {
                    logger.error("Could not write new g files");
                }
            } else {
                retVal.newFiles.add(filename);
                return retVal;
            }
        } else {
            logger.error("Could not read file: " + filename);
        }
        return null;
    }

    private boolean readGFile(String filename) {
        boolean graphmode = false;
        boolean coordmode = false;
        List<String> lines = FileHelper.getInstance().readFile(filename);
        if(lines == null) {
            logger.error("File not found: " + filename);
            return false;
        }
        for(String line : lines) {
            if(line.startsWith("#") || line.startsWith(".model") || line.equals("")) {
            } else if(line.startsWith(".inputs")) {
                graphmode = false;
                coordmode = false;
                if(inputs == null) {
                    inputs = parseTypeLine(line);
                } else {
                    logger.error("Input section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".outputs")) {
                graphmode = false;
                coordmode = false;
                if(outputs == null) {
                    outputs = parseTypeLine(line);
                } else {
                    logger.error("Output section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".dummy")) {
                graphmode = false;
                coordmode = false;
                if(dummies == null) {
                    dummies = parseTypeLine(line);
                } else {
                    logger.error("Dummy section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".internal")) {
                graphmode = false;
                coordmode = false;
                if(internals == null) {
                    internals = parseTypeLine(line);
                } else {
                    logger.error("Internal section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".graph")) {
                if(graphlines == null) {
                    graphmode = true;
                    coordmode = false;
                    graphlines = new ArrayList<List<GraphElement>>();
                } else {
                    logger.error("Graph section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".marking")) {
                graphmode = false;
                coordmode = false;
                if(markings == null) {
                    markings = parseMarking(line);
                } else {
                    logger.error("Marking section doubled in " + filename);
                    return false;
                }
            } else if(line.startsWith(".coordinates")) {
                coordmode = true;
                graphmode = false;
            } else if(line.startsWith(".end")) {
                break;
            } else if(graphmode) {
                graphlines.add(parseGraphLine(line));
            } else if(coordmode) {
            } else {
                logger.warn("Not interpreted: " + line);
            }
        }
        return true;
    }

    private void split() {
        Map<String, Integer> signalmap = new HashMap<String, Integer>();
        clusterlist = new TreeMap<Integer, List<String>>();
        int nextid = 0;
        boolean gotid = false;
        Set<Integer> merge = new HashSet<Integer>();
        for(List<GraphElement> line : graphlines) {
            gotid = false;
            merge.clear();
            for(GraphElement element : line) {
                if(signalmap.containsKey(element.getStr())) {
                    merge.add(signalmap.get(element.getStr()));
                } else {
                    if(!gotid) {
                        gotid = true;
                        nextid++;
                    }
                    signalmap.put(element.getStr(), nextid);
                    if(clusterlist.containsKey(nextid)) {
                        clusterlist.get(nextid).add(element.getStr());
                    } else {
                        List<String> temp = new ArrayList<String>();
                        temp.add(element.getStr());
                        clusterlist.put(nextid, temp);
                    }
                    merge.add(nextid);
                }
            }

            if(merge.size() > 1) {
                int id = ++nextid;
                List<String> newcluster = new ArrayList<String>();
                for(Integer i : merge) {
                    for(String str : signalmap.keySet()) {
                        if(signalmap.get(str).equals(i)) {
                            signalmap.put(str, id);
                        }
                    }
                    newcluster.addAll(clusterlist.get(i));
                    clusterlist.remove(i);
                }
                clusterlist.put(id, newcluster);
            }
        }
    }

    private boolean writeGFiles(String basename) {
        int i = 0;
        StringBuilder text;
        String newline = System.getProperty("line.separator");
        for(Entry<Integer, List<String>> entry : clusterlist.entrySet()) {
            text = new StringBuilder();
            text.append("#Generated by STGSplitter " + new Date().toString() + newline + newline);

            text.append(".inputs");
            for(String str : entry.getValue()) {
                if(inputs.contains(str)) {
                    text.append(" " + str);
                }
            }
            text.append(newline);
            text.append(".outputs");
            for(String str : entry.getValue()) {
                if(outputs.contains(str)) {
                    text.append(" " + str);
                }
            }
            text.append(newline);
            boolean interalsStrWritten = false;
            if(internals != null) {
                for(String str : entry.getValue()) {
                    if(internals.contains(str)) {
                        if(!interalsStrWritten) {
                            text.append(".internal");
                            interalsStrWritten = true;
                        }
                        text.append(" " + str);
                    }
                }
                text.append(newline);
            }
            boolean dummiesStrWritten = false;
            if(dummies != null) {
                for(String str : entry.getValue()) {
                    if(dummies.contains(str)) {
                        if(!dummiesStrWritten) {
                            text.append(newline + ".dummy");
                            dummiesStrWritten = true;
                        }
                        text.append(" " + str);
                    }
                }
                text.append(newline);
            }

            text.append(newline + ".graph" + newline);
            for(List<GraphElement> line : graphlines) {
                GraphElement element = line.get(0);
                if(entry.getValue().contains(element.getStr())) {
                    for(GraphElement element2 : line) {
                        text.append(element2.toString() + " ");
                    }
                    text = text.deleteCharAt(text.length() - 1);
                    text.append(newline);
                }
            }

            text.append(newline + ".marking { ");
            for(Marking mark : markings) {
                for(String str : entry.getValue()) {
                    if(mark.contains(str)) {
                        text.append(mark.toString() + " ");
                        break;
                    }
                }
            }
            text.append("}" + newline + ".end" + newline);

            String filename = basename + "_" + i++ + ".g";
            if(!FileHelper.getInstance().writeFile(filename, text.toString())) {
                return false;
            }
            newFiles.add(filename);
        }
        return true;
    }

    private List<Marking> parseMarking(String line) {
        String[] splitted = line.split(" ");
        List<Marking> retVal = new ArrayList<Marking>();
        for(String str : Arrays.copyOfRange(splitted, 2, splitted.length - 1)) {
            retVal.add(new Marking(str));
        }
        return retVal;
    }

    private List<GraphElement> parseGraphLine(String line) {
        String[] splitted = line.split(" ");
        List<GraphElement> list = new ArrayList<GraphElement>();
        for(String str : splitted) {
            list.add(GraphElement.parse(str));
        }
        return list;
    }

    private List<String> parseTypeLine(String line) {
        String[] arr = line.split(" ");
        arr = Arrays.copyOfRange(arr, 1, arr.length);
        return Arrays.asList(arr);
    }

    public List<String> getNewFiles() {
        return newFiles;
    }
}

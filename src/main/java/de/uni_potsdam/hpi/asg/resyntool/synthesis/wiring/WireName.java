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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WireName implements Comparable<WireName> {
    private static final Logger logger = LogManager.getLogger();

    public enum Type {
        r, a, d, reset, ic, unknown, internal
    }

    private static final Pattern controlSig  = Pattern.compile("([ra])[A-Z]([0-9]*)_([0-9]+)");
    private static final Pattern icSig       = Pattern.compile("ic([0-9]+)");
    private static final Pattern hsSig       = Pattern.compile("([rad])([0-9]+)");
    private static final Pattern internalSig = Pattern.compile("o[AC]([0-9]*)_([0-9]+)");

    private String               str;
    private Type                 type;
    private int                  id;
    private int                  chan;
    private Wire                 wire;

    public WireName(String str, Wire wire) {
        this.wire = wire;
        this.str = str;
        if(str.equals("_reset")) {
            type = Type.reset;
            id = 0;
            chan = -1;
        } else {
            Matcher matcher = controlSig.matcher(str);
            if(matcher.matches()) {
                type = Type.valueOf(matcher.group(1));
                id = matcher.group(2).equals("") ? 0 : Integer.parseInt(matcher.group(2));
                chan = Integer.parseInt(matcher.group(3));
            } else {
                matcher = icSig.matcher(str);
                if(matcher.matches()) {
                    type = Type.ic;
                    id = Integer.parseInt(matcher.group(1));
                    chan = Integer.MAX_VALUE;
                } else {
                    matcher = hsSig.matcher(str);
                    if(matcher.matches()) {
                        type = Type.valueOf(matcher.group(1));
                        id = -1;
                        chan = Integer.parseInt(matcher.group(2));
                    } else {
                        matcher = internalSig.matcher(str);
                        if(matcher.matches()) {
                            type = Type.internal;
                            id = matcher.group(1).equals("") ? 0 : Integer.parseInt(matcher.group(1));
                            chan = Integer.parseInt(matcher.group(2));
                        } else {
                            logger.warn("Unknown signaltype: " + str);
                            type = Type.unknown;
                            chan = -2;
                            id = 0;
                        }
                    }
                }
            }
        }
        //System.out.println(str + " type: " + type + ", chan: " + chan);
    }

    public String getStr() {
        return str;
    }

    @Override
    public int compareTo(WireName other) {
        if(this.type == Type.reset) {
            return 1;
        } else if(other.type == Type.reset) {
            return -1;
        }

        if(this.chan < other.chan) {
            return -1;
        } else if(this.chan > other.chan) {
            return 1;
        } else {
            if(this.type == other.type) {
                return this.id < other.id ? -1 : 1;
            } else {
                if(this.type == Type.r) {
                    return -1;
                } else if(this.type == Type.d) {
                    return 1;
                } else if(other.type == Type.r) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    public Wire getWire() {
        return wire;
    }

    public int getChan() {
        return chan;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}

package de.uni_potsdam.hpi.asg.resyntool.components;

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

public class HSSignal {
    private String channame;
    private String signalStr;

    public HSSignal(String channame, Signaltype signal) {
        this.channame = channame;
        switch(signal) {
            case request:
                this.signalStr = "r";
                break;
            case acknowledge:
                this.signalStr = "a";
                break;
            case data:
                this.signalStr = "d";
                break;
            case node:
                this.signalStr = "n";
                break;
        }
    }

    public String getRegExp() {
        return channame + "_([0-9]+)" + signalStr;
    }

    public enum Signaltype {
        request, acknowledge, data, node
    }
}

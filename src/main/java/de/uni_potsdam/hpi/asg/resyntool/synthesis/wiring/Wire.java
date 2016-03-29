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

public class Wire implements Comparable<Wire> {
    private WireName name;
    private int      width;
    private int      reader;
    private int      writer;

    public Wire(String name) {
        this.name = new WireName(name, this);
        this.width = -1;
        this.reader = 0;
        this.writer = 0;
    }

    public String getName() {
        return name.getStr();
    }

    public WireName getWireName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public void addReader() {
        reader++;
    }

    public void addWriter() {
        writer++;
    }

    public int getReader() {
        return reader;
    }

    public int getWriter() {
        return writer;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int compareTo(Wire arg0) {
        return this.name.compareTo(arg0.name);
    }
}

package de.uni_potsdam.hpi.asg.resyntool.synthesis.control;

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

public class DecompositionFutureInfo {

    private String filename;
    private int    id;

    public DecompositionFutureInfo(String filename, int id) {
        this.filename = filename;
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public int getId() {
        return id;
    }
}

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphElement {

    private static final Pattern pattern = Pattern.compile("(\\w+)([+-~]?/?\\d*)");

    private String               str;
    private String               remainder;

    public static GraphElement parse(String all) {
        Matcher matcher = pattern.matcher(all);
        String str = null;
        String remainder = null;
        if(matcher.matches()) {
            str = matcher.group(1);
            remainder = matcher.group(2);
            return new GraphElement(str, remainder);
        } else {
            System.out.println("Could not parse: " + all);
            return null;
        }
    }

    private GraphElement(String str, String remainder) {
        this.str = str;
        this.remainder = remainder;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return str + remainder;
    }
}

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

public class Marking {

    private static final Pattern transpattern = Pattern.compile("<(.*),(.*)>");

    private GraphElement         trans1;
    private GraphElement         trans2;
    private GraphElement         place;
    private boolean              transmode;

    public Marking(String str) {
        Matcher matcher = transpattern.matcher(str);
        if(matcher.matches()) {
            trans1 = GraphElement.parse(matcher.group(1));
            trans2 = GraphElement.parse(matcher.group(2));
            transmode = true;
        } else {
            place = GraphElement.parse(str);
            transmode = false;
        }
    }

    public boolean contains(String str) {
        if(transmode) {
            if(trans1.getStr().equals(str)) {
                return true;
            } else if(trans2.getStr().equals(str)) {
                return true;
            } else {
                return false;
            }
        } else {
            if(place.getStr().equals(str)) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        if(transmode) {
            return "<" + trans1.toString() + "," + trans2.toString() + ">";
        } else {
            return place.toString();
        }
    }
}

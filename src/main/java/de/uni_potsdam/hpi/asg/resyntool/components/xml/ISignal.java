package de.uni_potsdam.hpi.asg.resyntool.components.xml;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ISignal {
    public enum RefType {
        channel, parameter, component, reset
    }

    public enum Direction {
        in, out
    }

    //@formatter:off
    
    @XmlAttribute(name = "id", required = true)
    private int id;
    @XmlAttribute(name = "name", required = true)
    private String name;
    @XmlAttribute(name = "reftype", required = true)
    private RefType reftype;
    @XmlAttribute(name = "ref")
    private int ref;
    @XmlAttribute(name = "direction")
    private Direction direction;

    //@formatter:on

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRef() {
        return ref;
    }

    public RefType getReftype() {
        return reftype;
    }

    public Direction getDirection() {
        return direction;
    }
}

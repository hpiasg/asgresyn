package de.uni_potsdam.hpi.asg.resyntool.components.xml;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import de.uni_potsdam.hpi.asg.common.breeze.model.xml.Component;

@XmlAccessorType(XmlAccessType.NONE)
public class ComponentResyn extends Component {

    //@formatter:off
    
    @XmlElement(name = "resetNode", required = false)
    private String resetNode;
    @XmlElement(name = "removesignals")
    private RemoveSignals removesignals;
    @XmlElement(name = "interfacesignals")
    private InterfaceSignals interfacesignals;
    @XmlElement(name = "datapathmissing")
    private boolean datapathmissing;

    //@formatter:on

    public InterfaceSignals getInterfacesignals() {
        return interfacesignals;
    }

    public RemoveSignals getRemovesignals() {
        return removesignals;
    }

    public String getResetNode() {
        return resetNode;
    }

    public boolean isDatapathmissing() {
        return datapathmissing;
    }
}

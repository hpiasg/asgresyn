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

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlRootElement(name = "components")
@XmlAccessorType(XmlAccessType.NONE)
public class ComponentsResyn {
    protected static final Logger logger        = LogManager.getLogger();
    protected static final String injarfilename = "/components.xml";

    @XmlElement(name = "component")
    private List<ComponentResyn>  components;

    protected ComponentsResyn() {
    }

    public static ComponentsResyn readIn(String filename) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ComponentsResyn.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            if(filename == null || filename.equals("")) {
                URL url = ComponentsResyn.class.getResource(injarfilename);
                return (ComponentsResyn)jaxbUnmarshaller.unmarshal(url);
            } else {
                File file = new File(filename);
                if(file.exists()) {
                    return (ComponentsResyn)jaxbUnmarshaller.unmarshal(file);
                } else {
                    logger.error("File " + filename + " not found");
                    return null;
                }
            }
        } catch(JAXBException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public List<ComponentResyn> getComponents() {
        return components;
    }
}

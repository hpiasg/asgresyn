package de.uni_potsdam.hpi.asg.resyntool.synthesis.params;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogicSynthesisParameter {
    private static final Logger logger = LogManager.getLogger();

    public enum CscSolving {
        petrify, mpsat, dontcare
    }

    public enum LogicSynthesisType {
        petrify, asglogic
    }

    public enum TechMapping {
        petrify, none, asglogic
    }

    public enum ResetInsertion {
        petrify, petreset, asglogic
    }

    public enum SynthesisStrategy {
        PPP, PNP, PPI, AAA
    }

    private CscSolving         csc;
    private LogicSynthesisType logicSynthesis;
    private TechMapping        techmapping;
    private ResetInsertion     reset;
    private SynthesisStrategy  synthesisStrategy;

    public static LogicSynthesisParameter parse(String str) {
        if(str.length() != 4) {
            logger.error("str != length 4");
            return null;
        }
        LogicSynthesisParameter retVal = new LogicSynthesisParameter();
        retVal.csc = parseCscSolving(str.charAt(0));
        if(retVal.csc == null) {
            return null;
        }
        String remaining = str.substring(1, 4).toUpperCase();
        if(remaining.equals("PPP")) {
            retVal.synthesisStrategy = SynthesisStrategy.PPP;
            retVal.logicSynthesis = LogicSynthesisType.petrify;
            retVal.techmapping = TechMapping.petrify;
            retVal.reset = ResetInsertion.petrify;
        } else if(remaining.equals("PNP")) {
            retVal.synthesisStrategy = SynthesisStrategy.PNP;
            retVal.logicSynthesis = LogicSynthesisType.petrify;
            retVal.techmapping = TechMapping.none;
            retVal.reset = ResetInsertion.petrify;
        } else if(remaining.equals("PPI")) {
            retVal.synthesisStrategy = SynthesisStrategy.PPI;
            retVal.logicSynthesis = LogicSynthesisType.petrify;
            retVal.techmapping = TechMapping.petrify;
            retVal.reset = ResetInsertion.petreset;
        } else if(remaining.equals("AAA")) {
            retVal.synthesisStrategy = SynthesisStrategy.AAA;
            retVal.logicSynthesis = LogicSynthesisType.asglogic;
            retVal.techmapping = TechMapping.asglogic;
            retVal.reset = ResetInsertion.asglogic;
        } else {
            logger.error("Illegal SynthesisStrategy String: " + remaining);
            return null;
        }
        return retVal;
    }

    private static CscSolving parseCscSolving(char cscchar) {
        switch(cscchar) {
            case 'P':
            case 'p':
                return CscSolving.petrify;
            case 'M':
            case 'm':
                return CscSolving.mpsat;
            case 'X':
            case 'x':
            case '*':
                return CscSolving.dontcare;
            default:
                logger.error("Illegal cscSolving Parameter: " + cscchar);
                return null;
        }
    }

    public CscSolving getCsc() {
        return csc;
    }

    public LogicSynthesisType getLogicSynthesis() {
        return logicSynthesis;
    }

    public ResetInsertion getReset() {
        return reset;
    }

    public TechMapping getTechmapping() {
        return techmapping;
    }

    public SynthesisStrategy getSynthesisStrategy() {
        return synthesisStrategy;
    }
}

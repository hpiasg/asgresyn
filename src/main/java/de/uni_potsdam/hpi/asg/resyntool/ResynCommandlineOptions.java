package de.uni_potsdam.hpi.asg.resyntool;

/*
 * Copyright (C) 2012 - 2017 Norman Kluge
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

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import de.uni_potsdam.hpi.asg.common.iohelper.CommandlineOptions;

public class ResynCommandlineOptions extends CommandlineOptions {

    public boolean parseCmdLine(String[] args) {
        return super.parseCmdLine(args, "Usage: ASGresyn [options] <breeze file>\nOptions:");
    }

    //@formatter:off
    
    @Option(name = "-lib", metaVar = "<technologyfile>", usage = "technology description for implementation")
    private File technology;
    
    //@Option(name="-c", metaVar="<algorithm>") //, usage="clustering algorithm [finest, roughest, rule], default is rule")
    //private String  clustering              = "rule";
    //@Option(name="-cr", metaVar="<configfile>") //, usage="config file for rule clustering, default is clusterconfig.xml")
    //private File    ruleclusteringconfig    = new File("clusterconfig.xml");

    @Option(name = "-tc", metaVar = "<order>", usage = "tackle complexity order [S=Straight, D=Decomposition], default order is SD") //usage="tackle complexity order [S=Straight, D=Decomposition, C=Clustering], default order is SDC")
    private String tackleComplexityOrder = "SD";
    
    @Option(name = "-ls", metaVar = "<strategy>", usage = "Logic synthesis parameter: default is PAAA\n" + 
        "1st: CSC solving: P=petrify, M=mpsat" + /*, *=dontcare*/ "\n" + 
        "2nd: Logic synthesis: P=petrify, A=ASGlogic\n" + 
        "3rd: Technology mapping: P=petrify, N=no technology mapping, A=ASGlogic\n" + 
        "4th: Reset insertion: P=petrify, I=petreset, A=ASGlogic\n" + 
        "Allowed combinations for 2nd-4th: [PPP, PNP, PPI, AAA]")
    private String logicSynthesisParameter = "PAAA";

    // DesiJ Options
    @Option(name = "-d", metaVar = "<decostrategy>", usage = "strategy for decomposition [irr-csc-aware, csc-aware, tree, basic, lazy-multi, lazy-single, breeze], default is breeze")
    private String  decoStrategy = "breeze";
    @Option(name = "-p", metaVar = "<heuristic>", usage = "heuristic for partitioning [finest, roughest, multisignaluse, avoidcsc, reduceconc, lockedsignals, best, common-cause], default is common-cause")
    private String partitionHeuristic = "common-cause";
    @Option(name = "-breezeexpr", metaVar = "<breeze expressions file>", usage = "Breeze expressions file for DesiJ")
    private File desijbreezeexpr = null;
    
    // ASGlogic Option
    @Option(name = "-ASGlogicParams")
    private String asglogicParams = "";
    
    @Option(name = "-o", metaVar = "<level>", usage = "Outputlevel: 0:nothing\n1:errors\n[2:+warnings]\n3:+info")
    private int outputlevel             = 2;
    @Option(name = "-log", metaVar = "<logfile>", usage = "Define output Logfile, default is resyn.log")
    private File logfile = new File("resyn.log");
    @Option(name = "-sout", metaVar = "<file>", usage = "synthesis outfile, default is resyn.v")
    private File synthesisOutfile = new File(System.getProperty("user.dir") + File.separator + "resyn.v");
    @Option(name = "-zip", metaVar = "<zipfile>", usage = "Define the zip file with all temp files, default is resyn.zip")
    private File workfile = new File(System.getProperty("user.dir") + File.separator + "resyn.zip");
    
    @Option(name = "-cfg", metaVar = "<configfile>", usage = "Config file, default is resynconfig.xml")
    private File configfile = new File("resynconfig.xml");
    @Option(name = "-w", metaVar = "<workingdir>", usage = "Working directory. If not given, the value in configfile is used. If there is no entry, 'resynwork*' in the os default tmp dir is used.")
    private File workingdir = null;

    @Argument(metaVar = "breeze File", required = true)
    private File hsfile;

    @Option(name = "-debug")
    private boolean debug = false;
    @Option(name = "-tooldebug")
    private boolean tooldebug = false;
    @Option(name = "-sdp")
    private boolean skipdatapath = false;
    @Option(name = "-ssc")
    private boolean skipSubComponents = false;
    
    @Option(name = "-odp", usage = "Optimise data path (Default: false)")
    private boolean optimisedatapath = false;
    
    //@formatter:on

//    public String getClustering() {
//        return clustering;
//    }

    public File getHsfile() {
        return hsfile;
    }

    public File getTechnology() {
        return technology;
    }

    public int getOutputlevel() {
        return outputlevel;
    }

    public String getDecoStrategy() {
        return decoStrategy;
    }

    public String getPartitionHeuristic() {
        return partitionHeuristic;
    }

    public String getTackleComplexityOrder() {
        return tackleComplexityOrder;
    }

//    public File getRuleclusteringconfig() {
//        return ruleclusteringconfig;
//    }

    public File getLogfile() {
        return logfile;
    }

    public File getSynthesisOutfile() {
        return synthesisOutfile;
    }

    public String getLogicSynthesisParameter() {
        return logicSynthesisParameter;
    }

    public File getConfigfile() {
        return configfile;
    }

    public File getWorkfile() {
        return workfile;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isSkipdatapath() {
        return skipdatapath;
    }

    public File getWorkingdir() {
        return workingdir;
    }

    public boolean isOptimisedatapath() {
        return optimisedatapath;
    }

    public String getAsglogicParams() {
        return asglogicParams;
    }

    public boolean isSkipSubComponents() {
        return skipSubComponents;
    }

    public File getDesijbreezeexpr() {
        return desijbreezeexpr;
    }

    public boolean isTooldebug() {
        return tooldebug;
    }
}

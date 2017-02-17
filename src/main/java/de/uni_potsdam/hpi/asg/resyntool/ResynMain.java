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
import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.LoggerHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.common.iohelper.Zipper;
import de.uni_potsdam.hpi.asg.common.misc.CommonConstants;
import de.uni_potsdam.hpi.asg.common.technology.ReadTechnologyHelper;
import de.uni_potsdam.hpi.asg.common.technology.Technology;
import de.uni_potsdam.hpi.asg.resyntool.components.BreezeProjectResyn;
import de.uni_potsdam.hpi.asg.resyntool.io.Config;
import de.uni_potsdam.hpi.asg.resyntool.io.ConfigFile;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;
import de.uni_potsdam.hpi.asg.resyntool.io.ShutdownThread;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.SynthesisMain;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;

/**
 * Main class for the Resynthesis tool
 * 
 */
public class ResynMain {

    public static final String             DEF_CONFIG_FILE_NAME    = "resynconfig.xml";
    public static final File               DEF_CONFIG_FILE         = new File(CommonConstants.DEF_CONFIG_DIR_FILE, DEF_CONFIG_FILE_NAME);

    private static Logger                  logger;
    private static ResynCommandlineOptions options;

    public static Config                   config;
    public static boolean                  tooldebug;

    private static boolean                 skipUndefinedComponents = false;

    /**
     * Program entrance (with return code as <code>System.exit</code>)
     * 
     * @param args
     *            Commandline arguments (See {@link ResynCommandlineOptions})
     * @throws Exception
     */
    public static void main(String[] args) {
        int status = main2(args);
        System.exit(status);
    }

    /**
     * Progam entrance (with return code as int)
     * 
     * @param args
     *            Commandline arguments (See {@link ResynCommandlineOptions})
     * @return Program return code (<code>0</code>: ok, <code>1</code>:
     *         something went wrong)
     * @throws Exception
     */
    public static int main2(String[] args) {
        try {
            long start = System.currentTimeMillis();
            int status = -1;
            options = new ResynCommandlineOptions();
            if(options.parseCmdLine(args)) {
                logger = LoggerHelper.initLogger(options.getOutputlevel(), options.getLogfile(), options.isDebug());
                logger.debug("Args: " + Arrays.asList(args).toString());
                logger.debug("Using config file " + options.getConfigfile());
                config = ConfigFile.readIn(options.getConfigfile());
                if(config == null) {
                    logger.error("Could not read config");
                    return 1;
                }
                WorkingdirGenerator.getInstance().create(options.getWorkingdir(), config.workdir, "resynwork", ResynInvoker.getInstance());
                tooldebug = options.isTooldebug();
                addShutdownHook();
                status = execute();
                zipWorkfile();
                WorkingdirGenerator.getInstance().delete();
            }
            long end = System.currentTimeMillis();
            if(logger != null) {
                logger.info("Runtime: " + LoggerHelper.formatRuntime(end - start, false));
            }
            return status;
        } catch(Exception | Error e) {
            System.out.println("An error occurred: " + e.getLocalizedMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Main program. Does all the execution stuff
     * 
     * @return Program return code (<code>0</code>: ok, <code>1</code>:
     *         something went wrong)
     */
    private static int execute() {
        Technology tech = ReadTechnologyHelper.read(options.getTechnology(), config.defaultTech);
        if(tech == null) {
            logger.error("No technology found");
            return 1;
        }

        logger.info("------------------------------");
        logger.info("Parse Breeze");
        logger.info("------------------------------");

        BreezeProjectResyn proj = BreezeProjectResyn.create(options.getHsfile(), config.componentconfig, skipUndefinedComponents, options.isSkipSubComponents());
        if(proj == null) {
            logger.error("Could not create Netlist");
            return 1;
        }

        int synresult = doSynthesis(proj, tech);
        return synresult;
    }

    /**
     * Doing the main (Re)synthesis step
     * 
     * @return return code (<code>0</code>: ok, <code>1</code>: something went
     *         wrong)
     */
    private static int doSynthesis(BreezeProjectResyn proj, Technology tech) {
        //@formatter:off
        SynthesisParameter sparams = SynthesisParameter.create(
            tech, 
            options.getTackleComplexityOrder(), 
            options.getLogicSynthesisParameter(), 
            options.getDecoStrategy(), 
            options.getPartitionHeuristic(), 
            options.isSkipdatapath(),
            options.getAsglogicParams(),
            options.isOptimisedatapath(),
            options.getDesijbreezeexpr());
        //@formatter:on

        if(sparams == null) {
            logger.error("SynthesisParameter incomplete");
            return 1;
        }

        SynthesisMain smain = new SynthesisMain(proj, sparams);
        if(smain.generate()) {
            if(options.getSynthesisOutfile() != null) {
                if(!options.isSkipdatapath()) {
                    if(FileHelper.getInstance().copyfile(smain.getFile(), options.getSynthesisOutfile())) {
                        logger.info("Synthesis successful: " + options.getSynthesisOutfile());
                    } else {
                        logger.warn("Synthesis successful: " + smain.getFile() + ", Could not copy it into outfile");
                    }
                }
            } else {
                logger.warn("Synthesis successful: No outfile " + smain.getFile());
            }
        } else {
            logger.error("Synthesis failed");
            return 1;
        }
        return 0;
    }

    /**
     * Zips all the files in the working directory (for debugging)
     * 
     * @return <code>true</code>: ok, <code>false</code>: something went wrong
     */
    private static boolean zipWorkfile() {
        if(options.getWorkfile() != null) {
            if(!Zipper.getInstance().zip(options.getWorkfile())) {
                logger.warn("Could not zip temp files");
                return false;
            }
        } else {
            logger.warn("No zip outfile");
            return false;
        }
        return true;
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }
}
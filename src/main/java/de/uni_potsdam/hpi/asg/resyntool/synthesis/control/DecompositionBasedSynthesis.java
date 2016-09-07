package de.uni_potsdam.hpi.asg.resyntool.synthesis.control;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper;
import de.uni_potsdam.hpi.asg.common.iohelper.FileHelper.Filetype;
import de.uni_potsdam.hpi.asg.common.iohelper.WorkingdirGenerator;
import de.uni_potsdam.hpi.asg.resyntool.io.ResynInvoker;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.LogicSynthesisParameter;
import de.uni_potsdam.hpi.asg.resyntool.synthesis.params.SynthesisParameter;

public class DecompositionBasedSynthesis extends ControlSynthesis {
    private static final Logger logger = LogManager.getLogger();

    public DecompositionBasedSynthesis(String name, SynthesisParameter params) {
        super(name, params);
    }

    @Override
    public boolean generate() {
        String filename_breeze = name + FileHelper.getFileEx(Filetype.breeze);
        String filename_g = name + FileHelper.getFileEx(Filetype.stg);
        Pattern filepattern = Pattern.compile("(" + filename_g + "__final_.*)\\.g");

        if(ResynInvoker.getInstance().invokeDesijBreeze(filename_g, filename_breeze, true, params.getDesijBreezeExprFile())) {
            if(ResynInvoker.getInstance().invokeDesijDecomposition(params.getDecoStrategy(), params.getPartitionHeuristics(), filename_g)) {
                List<String> decofiles = new ArrayList<String>();
                File f = new File(WorkingdirGenerator.getInstance().getWorkingdir());
                Matcher matcher = null;
                for(File f2 : f.listFiles()) {
                    matcher = filepattern.matcher(f2.getName());
                    if(matcher.matches()) {
                        decofiles.add(matcher.group(1));
                    }
                }

                int components = decofiles.size();
                if(components == 0) {
                    logger.error("No Decomposition result found");
                    return false;
                }
                logger.info("Decomposition Components: " + components);

                Map<Future<String>, DecompositionFutureInfo> futureInfo = new HashMap<Future<String>, DecompositionFutureInfo>();
                List<String> stwInterfaces = new ArrayList<String>();
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(executor);
                LogicSynthesis synthesis = new LogicSynthesis(params); //TODO: same object for all?!
                int id = 0;
                for(String decofile : decofiles) {
                    Callable<String> worker = new DecompositionSynthesisFuture(decofile, synthesis);
                    Future<String> submit = ecs.submit(worker);
                    futureInfo.put(submit, new DecompositionFutureInfo(decofile, id++));
                }
                executor.shutdown();

                int componentFailed = 0, componentSucceed = 0;
                List<String> filelist = new ArrayList<String>();
                try {
                    while(true) {
                        Future<String> stwfilefuture = ecs.take();
                        if(stwfilefuture.get() == null) {
                            logger.error("Synthesis of " + futureInfo.get(stwfilefuture).getFilename() + " failed");
                            componentFailed++;
                        } else {
                            filelist.add(stwfilefuture.get());
                            String stwInterface = generateSTWInterface(stwfilefuture.get(), String.format("%d", futureInfo.get(stwfilefuture).getId()));
                            if(stwInterface == null) {
                                logger.error("Interface creation of " + futureInfo.get(stwfilefuture).getFilename() + " failed");
                                componentFailed++;
                            } else {
                                stwInterfaces.add(stwInterface);
                                componentSucceed++;
                            }
                        }
                        logger.debug("Components: " + components + ", Succeed: " + componentSucceed + ", Failed: " + componentFailed + ", Todo: " + (components - componentFailed - componentSucceed));
                        futureInfo.remove(stwfilefuture);
                        if(futureInfo.size() == 0) {
                            break;
                        }
                    }
                } catch(InterruptedException e) {
                    logger.error(e.getMessage());
                    return false;
                } catch(ExecutionException e) {
                    logger.error(e.getMessage());
                    return false;
                }

                if(componentFailed == 0) {
                    String filename_v = name + stwending + FileHelper.getFileEx(Filetype.verilog);
                    if(!FileHelper.getInstance().writeFile(filename_v, FileHelper.getInstance().mergeFileContents(filelist))) {
                        logger.error("Could not write Netlist");
                        return false;
                    }
                    if(params.getLogicSynthesisStrategy().getLogicSynthesis() == LogicSynthesisParameter.LogicSynthesisType.petrify) {
                        if(PetrifyInstanceNameFixer.fix(filename_v)) {
                            stwInfo = STWInformation.create(filename_v, stwInterfaces);
                            return true;
                        } else {
                            logger.error("Fixing of petrify instance names failed");
                            return false;
                        }
                    } else {
                        stwInfo = STWInformation.create(filename_v, stwInterfaces);
                        return true;
                    }
                } else {
                    logger.error(Integer.toString(componentFailed) + " of " + components + " components failed");
                    return false;
                }
            } else {
                logger.error("Decomposition failed with " + filename_g);
                return false;
            }
        } else {
            logger.error("Breeze2stg failed with " + filename_breeze);
            return false;
        }
    }
}

package de.uni_potsdam.hpi.asg.resyntool.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownThread extends Thread {
    private final static Logger logger = LogManager.getLogger();

    @Override
    public void run() {
        if(logger != null) {
            logger.debug("Killing subprocesses");
        }
        ResynInvoker inv = ResynInvoker.getInstance();
        if(inv != null) {
            inv.killSubprocesses();
        }
    }
}

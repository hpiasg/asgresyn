package de.uni_potsdam.hpi.asg.resyntool.synthesis.data;

/*
 * Copyright (C) 2016 Norman Kluge
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

import java.util.HashSet;
import java.util.Set;

public class DataOptimisationPlan {

    private String  localfilename;
    private String  localshfilename;
    private String  remoteshfilename;
    private String  localtclfilename;

    private boolean optmisationSuccessfil;
    private String  optimisedfilename;
    private String  unoptimisedfilename;

    public DataOptimisationPlan(String filename, String orgfile) {
        this.unoptimisedfilename = orgfile;
        this.localfilename = filename;
        this.optmisationSuccessfil = false;
    }

    public void setLocalshfilename(String localshfilename) {
        this.localshfilename = localshfilename;
    }

    public void setLocaltclfilename(String localtclfilename) {
        this.localtclfilename = localtclfilename;
    }

    public void setRemoteshfilename(String remoteshfilename) {
        this.remoteshfilename = remoteshfilename;
    }

    public void setOptimisedfilename(String optimisedfilename) {
        this.optimisedfilename = optimisedfilename;
    }

    public void setOptmisationSuccessfil(boolean optmisationSuccessfil) {
        this.optmisationSuccessfil = optmisationSuccessfil;
    }

    public Set<String> getAllUploadFiles() {
        Set<String> retVal = new HashSet<>();
        retVal.add(localfilename);
        retVal.add(localshfilename);
        retVal.add(localtclfilename);
        return retVal;
    }

    public String getRemoteShScriptName() {
        return remoteshfilename;
    }

    public String getLocalfilename() {
        return localfilename;
    }

    public String getOptimisedfilename() {
        return optimisedfilename;
    }

    public boolean wasOptmisationSuccessfil() {
        return optmisationSuccessfil;
    }

    public String getUnoptimisedfilename() {
        return unoptimisedfilename;
    }

}

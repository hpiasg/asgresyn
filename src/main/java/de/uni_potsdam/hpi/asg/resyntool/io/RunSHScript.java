package de.uni_potsdam.hpi.asg.resyntool.io;

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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RunSHScript {

    public static int run(Session session, String script, String folder) {
        try {
            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand("cd " + folder + "; sh " + script);

            int x = 0;
            channel.connect();
            while(channel.isConnected()) {
                Thread.sleep(1000);
                x++;
                if(x >= 300) { // 5min
                    channel.disconnect();
                    return -2;
                }
            }
            channel.disconnect();
            return channel.getExitStatus();
        } catch(JSchException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

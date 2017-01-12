package de.uni_potsdam.hpi.asg.resyntool.gui;

/*
 * Copyright (C) 2017 Norman Kluge
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

import java.io.IOException;

import javax.swing.JFrame;

public class ResynRunner {

    private Parameters params;

    public ResynRunner(Parameters params) {
        this.params = params;
    }

    public void run() {

        //@formatter:off
        String[] cmd = {
              
        };
        //@formatter:on

        StringBuilder str = new StringBuilder();
        for(String s : cmd) {
            str.append(s + " ");
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = null;
        try {
            process = pb.start();
        } catch(IOException e) {
            e.printStackTrace();
        }

        TerminalFrame tframe = new TerminalFrame(str.toString(), new TerminalWindowAdapter(process));
        tframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tframe.setVisible(true);

        IOStreamReader ioreader = new IOStreamReader(process, tframe.getText());
        Thread streamThread = new Thread(ioreader);
        streamThread.start();
    }
}

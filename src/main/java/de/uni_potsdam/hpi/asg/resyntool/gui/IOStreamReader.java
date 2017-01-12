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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JTextArea;

public class IOStreamReader implements Runnable {
    private Process      p;
    private StringBuffer out;
    private JTextArea    text;

    public IOStreamReader(Process p, JTextArea text) {
        this.p = p;
        this.out = new StringBuffer();
        this.text = text;
    }

    @Override
    public void run() {
        getOutAndErrStream();
    }

    private class InnerReader implements Runnable {
        private InputStream  stream;
        private StringBuffer out;

        public InnerReader(InputStream stream, StringBuffer out) {
            this.stream = stream;
            this.out = out;
        }

        @Override
        public void run() {
            BufferedReader is = new BufferedReader(new InputStreamReader(stream));
            String buf = "";
            try {
                while((buf = is.readLine()) != null) {
                    out.append(buf);
                    out.append(System.getProperty("line.separator"));
                }
                is.close();
            } catch(IOException e) {
                ;
            } catch(Exception e) {
                return;
            }
            return;
        }
    }

    private class CopyAgent implements Runnable {

        private StringBuffer buf;
        private JTextArea    text;

        public CopyAgent(StringBuffer buf, JTextArea text) {
            this.buf = buf;
            this.text = text;
        }

        @Override
        public void run() {
            while(true) {
                text.setText(buf.toString());
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    text.setText(buf.toString());
                    return;
                }
            }
        }
    }

    private void getOutAndErrStream() {
        if(p != null) {
            Thread tcopy = new Thread(new CopyAgent(out, text));
            Thread tout = new Thread(new InnerReader(p.getInputStream(), out));
            Thread terr = new Thread(new InnerReader(p.getErrorStream(), out));
            tcopy.start();
            tout.start();
            terr.start();
            try {
                tout.join();
                terr.join();
                tcopy.interrupt();
            } catch(InterruptedException e) {
                return;
            }
        }
        return;
    }
}

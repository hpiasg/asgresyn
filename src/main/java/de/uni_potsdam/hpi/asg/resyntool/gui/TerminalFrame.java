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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class TerminalFrame extends JFrame {
    private static final long serialVersionUID = 7172520905059189886L;

    private JTextArea         text;

    public TerminalFrame(String commandline, WindowAdapter adapt) {
        super("ASGresyn terminal");

        this.getContentPane().setLayout(new BorderLayout());

        JTextField cmdField = new JTextField(commandline);
        cmdField.setEditable(false);
        this.getContentPane().add(cmdField, BorderLayout.PAGE_START);

        text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane spane = new JScrollPane(text);
        DefaultCaret caret = (DefaultCaret)text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.getContentPane().add(spane, BorderLayout.CENTER);

        this.pack();
        this.setSize(new Dimension(900, 500));
        this.setLocationRelativeTo(null);

        this.addWindowListener(adapt);
    }

    public JTextArea getText() {
        return text;
    }
}

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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.BooleanParam;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.TextParam;

public class RunResynFrame extends JFrame {
    private static final long                 serialVersionUID = -2928503560193350216L;
    private JFrame                            parent;

    private Map<TextParam, JTextField>        textfields;
    private Map<BooleanParam, AbstractButton> buttons;

    private Parameters                        params;

    public RunResynFrame(Parameters params, WindowAdapter adapt) {
        super("ASGresyn runner");
        this.params = params;
        this.params.setFrame(this);
        textfields = new HashMap<>();
        buttons = new HashMap<>();
        parent = this;
        this.addWindowListener(adapt);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        constructGeneralPanel(tabbedPane);

    }

    private void constructGeneralPanel(JTabbedPane tabbedPane) {
        JPanel panel = new JPanel();
        tabbedPane.addTab("General", null, panel, null);
        GridBagLayout gbl_generalpanel = new GridBagLayout();
        gbl_generalpanel.columnWidths = new int[]{150, 300, 30, 80, 0};
        gbl_generalpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_generalpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_generalpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_generalpanel);

        constructTextEntry(panel, 0, TextParam.BreezeFile, "Breeze file", "", true, JFileChooser.FILES_ONLY, false);
        constructTextEntry(panel, 1, TextParam.TechLib, "Technology library", "", true, JFileChooser.FILES_ONLY, false);
        constructCheckboxEntry(panel, 2, BooleanParam.OptDp, "Optimise data path", false);
        constructTextEntry(panel, 3, TextParam.OutDir, "Output directory", "$USER-DIR", true, JFileChooser.DIRECTORIES_ONLY, true);
        constructTextEntry(panel, 4, TextParam.OutFile, "Output file name", "resyn.v", false, null, false);
        // 5: blank
        constructTextEntry(panel, 6, TextParam.CfgFile, "Configuration file", "$BASEDIR/config/resynconfig.xml", true, JFileChooser.FILES_ONLY, true);
        constructTextEntry(panel, 7, TextParam.WorkingDir, "Working directory", "$NULL", true, JFileChooser.DIRECTORIES_ONLY, true);
        // 8: loglevel
        constructLoglevelEntry(panel, 8, "Log level");
        // 9: loglevel
        constructTextEntry(panel, 9, TextParam.LogFile, "Log file name", "$OUTFILE.log", false, null, true);
        constructTextEntry(panel, 10, TextParam.TempFiles, "Temp files file name", "$OUTFILE.zip", false, null, true);
    }

    private void constructLoglevelEntry(JPanel panel, int row, String labelStr) {
        constructLabelCell(panel, row, labelStr);

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new FlowLayout());

        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.NORTHWEST;
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.gridx = 1;
        gbc_panel.gridy = row;
        panel.add(internalPanel, gbc_panel);

        JRadioButton nothingButton = new JRadioButton("Nothing");
        buttons.put(BooleanParam.LogLvl0, nothingButton);
        internalPanel.add(nothingButton);

        JRadioButton errorButton = new JRadioButton("Errors");
        buttons.put(BooleanParam.LogLvl1, errorButton);
        internalPanel.add(errorButton);

        JRadioButton warnButton = new JRadioButton("+Warnings");
        buttons.put(BooleanParam.LogLvl2, warnButton);
        internalPanel.add(warnButton);

        JRadioButton infoButton = new JRadioButton("+Info");
        infoButton.setSelected(true);
        buttons.put(BooleanParam.LogLvl3, infoButton);
        internalPanel.add(infoButton);

        ButtonGroup group = new ButtonGroup();
        group.add(nothingButton);
        group.add(errorButton);
        group.add(warnButton);
        group.add(infoButton);
    }

    private void constructCheckboxEntry(JPanel panel, int row, BooleanParam paramName, String labelStr, boolean defaultvalue) {
        constructLabelCell(panel, row, labelStr);

        JCheckBox checkbox = new JCheckBox("");
        buttons.put(paramName, checkbox);

        GridBagConstraints gbc_checkbox = new GridBagConstraints();
        gbc_checkbox.anchor = GridBagConstraints.NORTHWEST;
        gbc_checkbox.insets = new Insets(0, 0, 5, 0);
        gbc_checkbox.gridx = 1;
        gbc_checkbox.gridy = row;
        panel.add(checkbox, gbc_checkbox);
        checkbox.setSelected(defaultvalue);
    }

    private void constructLabelCell(JPanel panel, int row, String labelStr) {
        JLabel label = new JLabel(labelStr);
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.gridx = 0;
        gbc_label.gridy = row;
        panel.add(label, gbc_label);
    }

    private void constructTextEntry(JPanel panel, int row, TextParam paramName, String labelStr, final String defaultvalue, boolean hasPathButton, final Integer filemode, boolean hasdefaultcheckbox) {
        constructLabelCell(panel, row, labelStr);

        final JTextField textfield = new JTextField();
        textfields.put(paramName, textfield);

        GridBagConstraints gbc_text = new GridBagConstraints();
        gbc_text.fill = GridBagConstraints.HORIZONTAL;
        gbc_text.insets = new Insets(0, 0, 5, 5);
        gbc_text.gridx = 1;
        gbc_text.gridy = row;
        panel.add(textfield, gbc_text);
        textfield.setColumns(10);
        textfield.setText(defaultvalue);
        if(hasdefaultcheckbox) {
            textfield.setEnabled(false);
        }

        final JButton pathbutton = new JButton("..."); //dirty
        if(hasPathButton) {
            pathbutton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(filemode);
                    int result = fileChooser.showOpenDialog(parent);
                    if(result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        textfield.setText(selectedFile.getAbsolutePath());
                    }
                }
            });
            GridBagConstraints gbc_pathbutton = new GridBagConstraints();
            gbc_pathbutton.insets = new Insets(0, 0, 5, 5);
            gbc_pathbutton.gridx = 2;
            gbc_pathbutton.gridy = row;
            panel.add(pathbutton, gbc_pathbutton);
            if(hasdefaultcheckbox) {
                pathbutton.setEnabled(false);
            }
        }

        if(hasdefaultcheckbox) {
            JCheckBox defaultcheckbox = new JCheckBox("Default");
            defaultcheckbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        pathbutton.setEnabled(false);
                        textfield.setText(defaultvalue);
                        textfield.setEnabled(false);
                    } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                        pathbutton.setEnabled(true);
                        textfield.setText("");
                        textfield.setEnabled(true);
                    } else {
                        System.err.println("error");
                    }
                }
            });

            GridBagConstraints gbc_defaultcheckbox = new GridBagConstraints();
            gbc_defaultcheckbox.anchor = GridBagConstraints.NORTHWEST;
            gbc_defaultcheckbox.insets = new Insets(0, 0, 5, 0);
            gbc_defaultcheckbox.gridx = 3;
            gbc_defaultcheckbox.gridy = row;
            panel.add(defaultcheckbox, gbc_defaultcheckbox);
            defaultcheckbox.setSelected(true);
        }
    }
}
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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import de.uni_potsdam.hpi.asg.common.gui.PropertiesFrame;
import de.uni_potsdam.hpi.asg.common.gui.PropertiesPanel;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.BooleanParam;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.EnumParam;
import de.uni_potsdam.hpi.asg.resyntool.gui.Parameters.TextParam;

public class RunResynFrame extends PropertiesFrame {
    private static final long serialVersionUID = -2928503560193350216L;

    private Parameters        params;
    private boolean           errorOccured;

    public RunResynFrame(final Parameters params, WindowAdapter adapt, boolean isDebug) {
        super("ASGresyn runner");
        this.params = params;
        this.params.setFrame(this);
        this.addWindowListener(adapt);
        this.errorOccured = false;

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        constructGeneralPanel(tabbedPane);
        constructAdvancedPanel(tabbedPane);
        constructDebugPanel(tabbedPane, isDebug);

        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResynRunner run = new ResynRunner(params);
                run.run();
            }
        });
        getContentPane().add(runBtn, BorderLayout.PAGE_END);
    }

    private void constructGeneralPanel(JTabbedPane tabbedPane) {
        PropertiesPanel panel = new PropertiesPanel(this);
        tabbedPane.addTab("General", null, panel, null);
        GridBagLayout gbl_generalpanel = new GridBagLayout();
        gbl_generalpanel.columnWidths = new int[]{150, 300, 30, 80, 0};
        gbl_generalpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_generalpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_generalpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_generalpanel);

        panel.addTextEntry(0, TextParam.BreezeFile, "Breeze file", "", true, JFileChooser.FILES_ONLY, false);

        String[] techs = params.getAvailableTechs();
        String defTech = params.getDefTech();
        if(techs.length == 0) {
            JOptionPane.showMessageDialog(this, "No technologies installed. Please run ASGtechmngr", "Error", JOptionPane.ERROR_MESSAGE);
            errorOccured = true;
        }
        panel.addTechnologyChooserWithDefaultEntry(1, "Technology library", techs, defTech, EnumParam.TechLib, BooleanParam.TechLibDef, "Use default");

        panel.addCheckboxEntry(2, BooleanParam.OptDp, "Optimise data path", false);
        panel.addTextEntry(3, TextParam.OutDir, "Output directory", Parameters.userDirStr, true, JFileChooser.DIRECTORIES_ONLY, true);
        panel.addTextEntry(4, TextParam.OutFile, "Output file name", "resyn.v", false, null, false);
        // 5: blank
        panel.addTextEntry(6, TextParam.CfgFile, "Configuration file", Parameters.basedirStr + "/config/resynconfig.xml", true, JFileChooser.FILES_ONLY, true);
        panel.addTextEntry(7, TextParam.WorkingDir, "Working directory", Parameters.unsetStr, true, JFileChooser.DIRECTORIES_ONLY, true);
        panel.addSingleRadioButtonGroupEntry(8, "Log level", new String[]{"Nothing", "Errors", "+Warnings", "+Info"}, new BooleanParam[]{BooleanParam.LogLvl0, BooleanParam.LogLvl1, BooleanParam.LogLvl2, BooleanParam.LogLvl3}, 3);
        panel.addTextEntry(9, TextParam.LogFile, "Log file name", Parameters.outfilebaseName + ".log", false, null, true);
        panel.addTextEntry(10, TextParam.TempFiles, "Temp files file name", Parameters.outfilebaseName + ".zip", false, null, true);

        getDataFromPanel(panel);
    }

    private void constructAdvancedPanel(JTabbedPane tabbedPane) {
        PropertiesPanel panel = new PropertiesPanel(this);
        tabbedPane.addTab("Advanced", null, panel, null);
        GridBagLayout gbl_advpanel = new GridBagLayout();
        gbl_advpanel.columnWidths = new int[]{200, 300, 30, 80, 0};
        gbl_advpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_advpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_advpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_advpanel);

        panel.addLabelEntry(0, "<html><body><b>Tackle complexity</b></body></html>");
        constructTcEntries(panel, 1);
        // 2: also constructTcEntries
        panel.addComboBoxEntry(3, EnumParam.decoStrat, "Decomposition strategy", Parameters.decoStrategies);
        panel.addComboBoxEntry(4, EnumParam.decoPart, "Partitioning heuristic ", Parameters.partHeuristics);
        // 5: spacer
        panel.addLabelEntry(6, "<html><body><b>Logic synthesis</b></body></html>");
        panel.addSingleRadioButtonGroupEntry(7, "Solve CSC", new String[]{"Petrify", "MPSAT"}, new BooleanParam[]{BooleanParam.cscP, BooleanParam.cscM}, 0);
        panel.addSingleRadioButtonGroupEntry(8, "Synthesis", new String[]{"ASGlogic", "Petrify"}, new BooleanParam[]{BooleanParam.synA, BooleanParam.synP}, 0);
        panel.addSingleRadioButtonGroupEntry(9, "Technology mapping", new String[]{"ASGlogic", "Petrify", "No"}, new BooleanParam[]{BooleanParam.tmA, BooleanParam.tmP, BooleanParam.tmN}, 0);
        panel.addSingleRadioButtonGroupEntry(10, "Reset insertion", new String[]{"ASGlogic", "Petrify", "Petreset"}, new BooleanParam[]{BooleanParam.rstA, BooleanParam.rstP, BooleanParam.rstI}, 0);
        panel.addTextEntry(11, TextParam.Asglogic, "Additional ASGlogic parameters", Parameters.unsetStr, false, null, true);

        getDataFromPanel(panel);
        establishFeasibilityEnforcement();
    }

    private void constructDebugPanel(JTabbedPane tabbedPane, boolean isDebug) {
        PropertiesPanel panel = new PropertiesPanel(this);
        if(isDebug) {
            tabbedPane.addTab("Debug", null, panel, null);
        }
        GridBagLayout gbl_advpanel = new GridBagLayout();
        gbl_advpanel.columnWidths = new int[]{200, 300, 30, 80, 0};
        gbl_advpanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_advpanel.rowHeights = new int[]{15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 0};
        gbl_advpanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_advpanel);

        panel.addCheckboxEntry(0, BooleanParam.debug, "Debug", isDebug);
        panel.addCheckboxEntry(1, BooleanParam.tooldebug, "Tool debug", false);
        panel.addCheckboxEntry(2, BooleanParam.sdp, "Skip data path", false);
        panel.addCheckboxEntry(3, BooleanParam.ssc, "Skip subcomponents", false);
        panel.addTextEntry(4, TextParam.BreezeExprFile, "Breeze Expression", Parameters.unsetStr, true, JFileChooser.FILES_ONLY, true);

        getDataFromPanel(panel);
    }

    private void establishFeasibilityEnforcement() {
        // Allowed: PPP, PNP, PPI, AAA
        final JRadioButton synA = (JRadioButton)buttons.get(BooleanParam.synA);
        final JRadioButton synP = (JRadioButton)buttons.get(BooleanParam.synP);
        final JRadioButton tmA = (JRadioButton)buttons.get(BooleanParam.tmA);
        final JRadioButton tmP = (JRadioButton)buttons.get(BooleanParam.tmP);
        final JRadioButton tmN = (JRadioButton)buttons.get(BooleanParam.tmN);
        final JRadioButton rstA = (JRadioButton)buttons.get(BooleanParam.rstA);
        final JRadioButton rstP = (JRadioButton)buttons.get(BooleanParam.rstP);
        final JRadioButton rstI = (JRadioButton)buttons.get(BooleanParam.rstI);

        tmP.setEnabled(false);
        tmN.setEnabled(false);
        rstP.setEnabled(false);
        rstI.setEnabled(false);

        synA.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    tmP.setEnabled(false);
                    tmN.setEnabled(false);
                    tmA.setEnabled(true);
                    tmA.setSelected(true);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {

                } else {
                    System.out.println("error");
                }
            }
        });
        synP.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    tmP.setEnabled(true);
                    tmN.setEnabled(true);
                    tmA.setEnabled(false);
                    if(tmA.isSelected()) {
                        tmA.setSelected(false);
                        tmP.setSelected(true);
                    }
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {

                } else {
                    System.out.println("error");
                }
            }
        });
        tmA.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    rstP.setEnabled(false);
                    rstI.setEnabled(false);
                    rstA.setEnabled(true);
                    rstA.setSelected(true);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {

                } else {
                    System.out.println("error");
                }
            }
        });
        tmP.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    rstP.setEnabled(true);
                    rstI.setEnabled(true);
                    rstA.setEnabled(false);
                    if(rstA.isSelected()) {
                        rstA.setSelected(false);
                        rstP.setSelected(true);
                    }
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {

                } else {
                    System.out.println("error");
                }
            }
        });
        tmN.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    rstP.setEnabled(true);
                    rstI.setEnabled(false);
                    rstA.setEnabled(false);
                    rstP.setSelected(true);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {

                } else {
                    System.out.println("error");
                }
            }
        });
    }

    private void constructTcEntries(PropertiesPanel panel, int row) {
        panel.addLabelCell(row, "Direct");

        JPanel directInternalPanel = new JPanel();
        directInternalPanel.setLayout(new FlowLayout());

        GridBagConstraints gbc_directpanel = new GridBagConstraints();
        gbc_directpanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_directpanel.insets = new Insets(0, 0, 5, 0);
        gbc_directpanel.gridx = 1;
        gbc_directpanel.gridy = row;
        panel.add(directInternalPanel, gbc_directpanel);

        final JRadioButton directFirstButton = new JRadioButton("Try first");
        buttons.put(BooleanParam.tcS1, directFirstButton);
        directInternalPanel.add(directFirstButton);

        final JRadioButton directSecondButton = new JRadioButton("Try second");
        buttons.put(BooleanParam.tcS2, directSecondButton);
        directInternalPanel.add(directSecondButton);

        final JRadioButton directDontButton = new JRadioButton("Don't try");
        buttons.put(BooleanParam.tcS0, directDontButton);
        directInternalPanel.add(directDontButton);

        ButtonGroup directBtnGr = new ButtonGroup();
        directBtnGr.add(directFirstButton);
        directBtnGr.add(directSecondButton);
        directBtnGr.add(directDontButton);

        //--

        panel.addLabelCell(row + 1, "Decomposition");

        JPanel decoInternalPanel = new JPanel();
        decoInternalPanel.setLayout(new FlowLayout());

        GridBagConstraints gbc_decopanel = new GridBagConstraints();
        gbc_decopanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_decopanel.insets = new Insets(0, 0, 5, 0);
        gbc_decopanel.gridx = 1;
        gbc_decopanel.gridy = row + 1;
        panel.add(decoInternalPanel, gbc_decopanel);

        final JRadioButton decoFirstButton = new JRadioButton("Try first");
        buttons.put(BooleanParam.tcD1, decoFirstButton);
        decoInternalPanel.add(decoFirstButton);

        final JRadioButton decoSecondButton = new JRadioButton("Try second");
        buttons.put(BooleanParam.tcD2, decoSecondButton);
        decoInternalPanel.add(decoSecondButton);

        final JRadioButton decoDontButton = new JRadioButton("Don't try");
        buttons.put(BooleanParam.tcD0, decoDontButton);
        decoInternalPanel.add(decoDontButton);

        ButtonGroup decoBtnGr = new ButtonGroup();
        decoBtnGr.add(decoFirstButton);
        decoBtnGr.add(decoSecondButton);
        decoBtnGr.add(decoDontButton);

        //--

        directDontButton.setSelected(true);
        decoDontButton.setEnabled(false);
        decoFirstButton.setSelected(true);
        directFirstButton.setEnabled(false);

        //--

        directFirstButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    decoFirstButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    decoFirstButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
        directSecondButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    decoSecondButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    decoSecondButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
        directDontButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    decoDontButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    decoDontButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
        decoFirstButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    directFirstButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    directFirstButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
        decoSecondButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    directSecondButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    directSecondButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
        decoDontButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    directDontButton.setEnabled(false);
                } else if(e.getStateChange() == ItemEvent.DESELECTED) {
                    directDontButton.setEnabled(true);
                } else {
                    System.out.println("error");
                }
            }
        });
    }

    public boolean hasErrorOccured() {
        return errorOccured;
    }
}
/*
 JGammon: A Backgammon client with nice graphics written in Java
 Copyright (C) 2005 Mattias Ulbrich

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.game.backgammon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * This dialog is showed before starting a new game.
 *
 * Some pictures are taken from: iconarchive.com
 * @author Aviv
 */
public class NewGameDialog extends JDialog {PropertyResourceBundle r = null;
    private JPanel panel1 = new JPanel();
    private ResourceBundle msg = ResourceBundle.getBundle(
            "com.game.backgammon.msg.NewGameDialog");
    private JRadioButton RBlocal = new JRadioButton();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private Component component1 = Box.createHorizontalStrut(8);
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JButton cancel = new JButton();
    private JButton OK = new JButton();
    private JLabel jLabel6 = new JLabel();
    private ButtonGroup topGroup = new ButtonGroup();
    private ButtonGroup remoteGroup = new ButtonGroup();

    private ImageIcon local = new ImageIcon(NewGameDialog.class.getResource(
            "/com/game/backgammon/img/local.png"));

    private boolean okPressed = false;
    private JGammon jgam;
    private JLabel fileNameLabel = new JLabel();


    public NewGameDialog(JGammon jgam) {
        super(jgam.getFrame(), true);
        this.jgam = jgam;
        setTitle(msg.getString("newgame"));
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            getRootPane().setDefaultButton(OK);
            pack();
            JGammon.centerFrame(this);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public NewGameDialog() {
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                enableDisable();
            }
        };

        panel1.setLayout(gridBagLayout1);
        RBlocal.setSelected(true);
        RBlocal.setText(msg.getString("local"));
        RBlocal.addChangeListener(changeListener);
        jLabel1.setText(msg.getString("whitename"));
        jLabel2.setText(msg.getString("bluename"));
        cancel.setText(msg.getString("cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        OK.setText(msg.getString("OK"));
        OK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                setVisible(false);
            }
        });
        jLabel6.setIcon(local);
        fileNameLabel.setMaximumSize(new Dimension(150, 15));
        fileNameLabel.setPreferredSize(new Dimension(150, 15));
        RBlocal.setEnabled(true);
        topGroup.add(RBlocal);
        panel1.add(RBlocal, new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(20, 0, 0, 0), 2, 0));
        panel1.add(component1, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 20, 0));
        panel1.add(jLabel6, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(20, 20, 20, 20), 0, 0));
        panel1.add(cancel, new GridBagConstraints(1, 13, 3, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(0, 0, 19, 0), 0, 0));
        panel1.add(OK, new GridBagConstraints(4, 13, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.EAST,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 20, 20), 0, 0));
        panel1.add(jLabel1, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(jLabel2, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 7), 0, 0));
        panel1.add(fileNameLabel, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 10, 10, 0), 0, 20));
        this.getContentPane().add(panel1, java.awt.BorderLayout.CENTER);

 }


    public Game showAndEval() {

        while (true) {
            okPressed = false;
            setVisible(true);

            // waiting for answer. ...
            if (!okPressed) {
                return null;
            }

            try {
                if (RBlocal.isSelected()) {
                        Player player1 = new LocalPlayer("Player");
                        Player player2 = new ComputerPlayer("Computer");
                        Game game = new Game(player1, player2, jgam);
                        return game;
                } else { // network game

                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                                              msg.getString("errorPort"),
                                              msg.getString("error"),
                                              JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex,
                                              msg.getString("error"),
                                              JOptionPane.ERROR_MESSAGE);

            }

        }

    }

    public void enableDisable() {
        boolean local = RBlocal.isSelected();
        boolean client = !local;
        jLabel1.setEnabled(local);
        jLabel2.setEnabled(local);
        fileNameLabel.setEnabled(!client || !local);
    }

    /**
     * feed infos from command line
     */

    public void feed(String mode, String portArg, String serverArg,
                     String name1Arg, String name2Arg, String boardFileArg) {
        if (mode.equals("local")) {
            RBlocal.setSelected(true);
        } 

    }


}


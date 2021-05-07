import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUI {

    SWF selectedSWF;
    File modSourceFile;

    JFrame frame;

    public GUI() {
        //The name is longer then the other one and I think that is funny.
        frame = new JFrame("Epicsninja's Automatic Patch-Independant String-Based Mod Installer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon img = new ImageIcon("images/hatfrogg.png");
        frame.setIconImage(img.getImage());

        //Path label. For debug purposes only
        JTextArea selectedSwfPath = new JTextArea();
        selectedSwfPath.setEditable(false);
        selectedSwfPath.setText("No Swf Path");

        JScrollPane pathScrollPane = new JScrollPane();
        pathScrollPane.setViewportView(selectedSwfPath);
        pathScrollPane.setBounds(5, 180, 400, 36);

        //Path label. For debug purposes only
        JTextArea selectedModPath = new JTextArea();
        selectedModPath.setEditable(false);
        selectedModPath.setText("No Mod Path");

        JScrollPane modPathScrollPane = new JScrollPane();
        modPathScrollPane.setViewportView(selectedModPath);
        modPathScrollPane.setBounds(5, 220, 400, 36);

        //SWF name label
        JTextArea selectedSwfName = new JTextArea();
        selectedSwfName.setEditable(false);
        selectedSwfName.setBounds(5, 5, 200, 24);
        selectedSwfName.setText("No SWF");

        //Outut SWF Name and label for it
        JLabel swfLabel = new JLabel(" Output File: ");
        swfLabel.setBounds(210, 5, 75, 24);

        JTextArea outputSWFName = new JTextArea();
        outputSWFName.setBounds(280, 5, 200, 24);

        //Button to select SWF through disgusting looking non-native file selector.
        JButton selectSwfButton = new JButton("Select SWF");
        selectSwfButton.setBounds(5, 35, 200, 24);

        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith(".swf") || f.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return null;
            }

        });

        selectSwfButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedSWF = EMethods.GetSwf(fc.getSelectedFile().getAbsolutePath(), false);
                    System.out.println("Yee " + fc.getSelectedFile().getName());
                } else {
                    System.out.println("Naw");
                }

                if (selectedSWF != null) {
                    selectedSwfPath.setText(fc.getSelectedFile().getAbsolutePath());
                    outputSWFName.setText("Modded_" + fc.getSelectedFile().getName());
                    selectedSwfName.setText(fc.getSelectedFile().getName());
                }
            }
        });

        //Button to select mod Source FIle through disgusting looking non-native file selector.
        JButton selectModSourceButton = new JButton("Select Mod");
        selectModSourceButton.setBounds(5, 70, 200, 24);

        final JFileChooser fc2 = new JFileChooser();
        fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        selectModSourceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc2.showOpenDialog(null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    modSourceFile = fc2.getSelectedFile();
                    selectedModPath.setText(fc2.getSelectedFile().getAbsolutePath());
                    System.out.println("Yee " + fc2.getSelectedFile().getName());
                } else {
                    System.out.println("Naw");
                }
            }
        });

        //Button to install the mod based on the above selected variables.
        JButton installMod = new JButton("Install Mod");
        installMod.setBounds(210, 70, 275, 24);

        installMod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (modSourceFile != null && selectedSWF != null) {
                    try {
                        EMethods.InsertMod(modSourceFile, selectedSWF, outputSWFName.getText());
                    } catch (IOException e1) {
                        infoBox("MOD INSERT FAILED: IO Exception", "ERROR");
                        e1.printStackTrace();
                    } catch (CustomExceptions e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        infoBox("MOD INSERT FAILED: Invalid Mod", "ERROR");
                    } catch(BadFormatException e1){
                        infoBox("MOD INSERT FAILED: Bad Mod Format " + e1.getMessage(), "ERROR");
                    }
                } else {
                    infoBox("MOD INSERT FAILED: source exists: " + (modSourceFile != null) + ", swf exists: " + (selectedSWF != null), "ERROR");
                }

            }
        });

        frame.add(selectedSwfName);
        frame.add(pathScrollPane);
        frame.add(modPathScrollPane);
        frame.add(swfLabel);
        frame.add(outputSWFName);
        frame.add(selectSwfButton);
        frame.add(installMod);
        frame.add(selectModSourceButton);
        frame.setSize(550, 300);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new GUI();
    }
}
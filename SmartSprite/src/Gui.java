import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;

import java.awt.event.*;
import java.io.File;
import java.util.List;

public class Gui {

    SWF selectedSWF;
    JFrame frame;

    SpriteExportMode SpriteEM = SpriteExportMode.SWF;
    ShapeExportMode ShapeEM = ShapeExportMode.SWF;
    double ExportScaleUsed = 1;


    public Gui() {
        //The name is long and I think that is funny.
        frame = new JFrame("Epicsninja's Smart Sprite and Shape Exporter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon img = new ImageIcon("images/frogg.png");
        frame.setIconImage(img.getImage());

        //Path label. THis used to be at the top but Bucket told me to move it to the bottom.
        JTextArea selectedSwfPath = new JTextArea();
        selectedSwfPath.setEditable(false);
        selectedSwfPath.setText("No Path");

        JScrollPane pathScrollPane = new JScrollPane();
        pathScrollPane.setViewportView(selectedSwfPath);
        //selectedSwfPath.setAutoscrolls(JList.HORIZONTAL_WRAP);
        pathScrollPane.setBounds(5, 500, 200, 36);

        //SWF name label
        JTextArea selectedSwfName = new JTextArea();
        selectedSwfName.setEditable(false);
        selectedSwfName.setBounds(5, 5, 200, 24);
        selectedSwfName.setText("No SWF");

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
                    selectedSwfName.setText(fc.getSelectedFile().getName());
                }
            }
        });

        //Skin Name and Label for it
        JLabel skinLabel = new JLabel(" Skin Name: ");
        skinLabel.setBounds(210, 5, 75, 24);

        JTextArea selectedSkinName = new JTextArea();
        selectedSkinName.setBounds(280, 5, 200, 24);
        selectedSkinName.setText("");

        //List of all skins within a SWF, plus some other junk due to the way skin names are found.
        JList < String > namesList = new JList < > ();

        JScrollPane namesScrollPane = new JScrollPane();
        namesScrollPane.setViewportView(namesList);
        namesList.setLayoutOrientation(JList.VERTICAL);
        namesScrollPane.setBounds(5, 90, 200, 400);

        namesList.addListSelectionListener((ListSelectionListener) new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedSkinName.setText(namesList.getSelectedValue());
            }
        });

        //Button to reload skin names
        JButton reloadNamesButton = new JButton("Reload Skin Options");
        reloadNamesButton.setBounds(5, 60, 200, 24);

        reloadNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadNamesButton.setText("Reloading...");
                if (selectedSWF != null) {
                    List < String > listNames = EMethods.GetAllSkinNames((SWF) selectedSWF, 0);

                    String[] names = new String[listNames.size()];
                    listNames.toArray(names);

                    namesList.setListData(names);
                }
                reloadNamesButton.setText("Reload Skin Options");
            }
        });

        //Label.
        JLabel formatsLabel = new JLabel("Export format");
        formatsLabel.setBounds(500, 5, 200, 24);

        //EXPORT OPTIONS: BMP, CANVAS, SVG, PNG, SWF

        //So many warnings in this section. I don't know enough about programming to fix them.
        //Box for Sprite Export format.
        JComboBox < String > spriteFormat = new JComboBox < String > ();
        spriteFormat.addItem("SWF");
        spriteFormat.addItem("SVG");
        spriteFormat.addItem("PNG");
        spriteFormat.addItem("BMP");
        spriteFormat.setBounds(500, 35, 200, 24);
        spriteFormat.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //This is a disgusting If chain but I really don't see any other options.
                if (spriteFormat.getSelectedItem() == "SWF") {
                    SpriteEM = SpriteExportMode.SWF;
                } else if (spriteFormat.getSelectedItem() == "SVG") {
                    SpriteEM = SpriteExportMode.SVG;
                } else if (spriteFormat.getSelectedItem() == "PNG") {
                    SpriteEM = SpriteExportMode.PNG;
                } else {
                    //Who in their right mind would choose BMP? Why is this even an option?
                    SpriteEM = SpriteExportMode.BMP;
                }
            }
        });

        //Box for Shape Export format.
        JComboBox < String > shapeFormat = new JComboBox < String > ();
        shapeFormat.addItem("SWF");
        shapeFormat.addItem("SVG");
        shapeFormat.addItem("PNG");
        shapeFormat.addItem("BMP");
        shapeFormat.setBounds(500, 65, 200, 24);
        shapeFormat.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //This is a disgusting If chain but I really don't see any other options.
                if (shapeFormat.getSelectedItem() == "SWF") {
                    ShapeEM = ShapeExportMode.SWF;
                } else if (shapeFormat.getSelectedItem() == "SVG") {
                    ShapeEM = ShapeExportMode.SVG;
                } else if (shapeFormat.getSelectedItem() == "PNG") {
                    ShapeEM = ShapeExportMode.PNG;
                } else {
                    //Who in their right mind would choose BMP? Why is this even an option?
                    ShapeEM = ShapeExportMode.BMP;
                }
            }
        });

        //Box for Export scale.
        JComboBox < String > exportScale = new JComboBox < String > ();
        exportScale.addItem("50%");
        exportScale.addItem("100%");
        exportScale.addItem("200%");
        exportScale.addItem("400%");
        exportScale.setSelectedItem(exportScale.getItemAt(1));
        exportScale.setBounds(210, 105, 200, 24);
        exportScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //This is a disgusting If chain but I really don't see any other options.
                if (shapeFormat.getSelectedItem() == "50%") {
                    ExportScaleUsed = 0.5;
                } else if (shapeFormat.getSelectedItem() == "100%") {
                    ExportScaleUsed = 1;
                } else if (shapeFormat.getSelectedItem() == "200%") {
                    ExportScaleUsed = 2;
                } else {
                    ExportScaleUsed = 4;
                }
            }
        });

        //Button to export Sprites for whatever the skins name is.
        JButton exportSprites = new JButton("Export Sprites");
        exportSprites.setBounds(210, 35, 275, 24);

        exportSprites.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedSWF != null && selectedSkinName.getText().length() > 1) {
                    EMethods.ExtractSprites(selectedSkinName.getText(), selectedSWF, SpriteEM, ExportScaleUsed);
                }
            }
        });

        //Button to export Shapes for whatever the skins name is.
        JButton exportShapes = new JButton("Export Shapes");
        exportShapes.setBounds(210, 65, 275, 24);

        exportShapes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedSWF != null && selectedSkinName.getText().length() > 1) {
                    EMethods.ExtractShapes(selectedSkinName.getText(), selectedSWF, ShapeEM, ExportScaleUsed);
                }
            }
        });

        //Things to consider adding: Option to pick export directory, option to make exports compatible with modloader vs aimed at normal modding.
        //demo renderer of selected skin?
        JLabel teaser = new JLabel("I might add more features later. I might not.");
        teaser.setBounds(210, 145, 275, 24);


        frame.add(selectSwfButton);
        frame.add(selectedSwfName);
        frame.add(pathScrollPane);
        frame.add(reloadNamesButton);
        frame.add(namesScrollPane);
        frame.add(skinLabel);
        frame.add(selectedSkinName);
        frame.add(exportScale);
        frame.add(exportSprites);
        frame.add(exportShapes);
        frame.add(teaser);
        frame.add(spriteFormat);
        frame.add(shapeFormat);
        frame.add(formatsLabel);
        frame.setSize(800, 600);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void main(String[] args) {
        new Gui();
    }
}
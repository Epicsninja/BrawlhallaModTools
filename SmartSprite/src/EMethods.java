import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.Helper;

import org.monte.media.jpeg.CMYKJPEGImageReader;
import org.monte.media.jpeg.CMYKJPEGImageReaderSpi;

import jdk.nashorn.internal.ir.UnaryNode;

import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.importers.ImageImporter;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

//Terminology
//ExpName: Export Name. DefineSprite_0_a_b
//PartName: Ending of ExpName. Can include SetName(b)
//SkinName: Very End of ExpName. Name of the set the skin belongs to.
class EMethods {
    public static void main(String[] args) throws FileNotFoundException {
        SWF swf = GetSwf("Gfx_ActualShark.swf", true);

        for (Tag t: swf.getTags()) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprot = (DefineSpriteTag) t;
                System.out.println(sprot.getExportFileName());
                ReadOnlyTagList sportTags = sprot.getTags();

                for (int i = 0; i < sportTags.size(); i++) {
                    if (sportTags.get(i) instanceof PlaceObject2Tag) {
                        PlaceObject2Tag PO = (PlaceObject2Tag) sportTags.get(i);

                        if (PO.placeFlagHasMatrix) {
                            MATRIX mat = PO.getMatrix();
                            mat.translateX = 5000;
                            PO.setMatrix(mat);

                            sprot.replaceTag(i, PO);
                        } else {
                            System.out.println("Bad");
                        }
                    }
                }

                swf.replaceTag(t, sprot);
            } else {}
        }

        for (Tag t: swf.getTags()) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprot = (DefineSpriteTag) t;
                ReadOnlyTagList sportTags = sprot.getTags();

                for (int i = 0; i < sportTags.size(); i++) {
                    if (sportTags.get(i) instanceof PlaceObject2Tag) {
                        PlaceObject2Tag PO = (PlaceObject2Tag) sportTags.get(i);

                        if (PO.placeFlagHasMatrix) {
                            System.out.println(PO.getMatrix().translateX);
                        } else {
                            System.out.println("Bad2");
                        }
                    }
                }
            } else {}
        }

        OutputStream os = new FileOutputStream("data/GFX_REEEEEEEEEEEEEEEEE.swf");
        try {
            swf.saveTo(os);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        swf = GetSwf("GFX_REEEEEEEEEEEEEEEEE.swf", true);

        for (Tag t: swf.getTags()) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprot = (DefineSpriteTag) t;
                ReadOnlyTagList sportTags = sprot.getTags();

                for (int i = 0; i < sportTags.size(); i++) {
                    if (sportTags.get(i) instanceof PlaceObject2Tag) {
                        PlaceObject2Tag PO = (PlaceObject2Tag) sportTags.get(i);

                        if (PO.placeFlagHasMatrix) {
                            System.out.println(PO.getMatrix().translateX);
                        } else {
                            System.out.println("Bad3");
                        }
                    }
                }
            } else {}
        }
    }

    public static void ReplacerAlpha() {
        String nameOfSkin = "SharkGoblin";
        SWF swf = GetSwf("Gfx_ActualShark.swf", true);

        List < String > replacements = new ArrayList < String > ();

        File folder = new File("data/shapes");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                replacements.add(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4));
            }
        }

        System.out.println(replacements);

        List < String > toReplace = new ArrayList < String > ();
        List < Tag > allSprites = GetSpritesList(nameOfSkin, swf);
        List < Tag > replaceSprites = new ArrayList < Tag > ();

        for (Tag t: allSprites) {

            Set < Integer > needed = new HashSet < > ();
            t.getNeededCharacters(needed);

            String name = GetPartNameFromExpName(t.getExportFileName(), nameOfSkin, false);
            name = name.substring(2, name.length());

            if (needed.size() > 0) {
                if (replacements.contains(name)) {
                    for (int i = 0; i < needed.size(); i++) {
                        //In theory I should be able to replace sprites that use multiple shapes too.
                        if (1 == needed.size()) {
                            toReplace.add(name);
                        } else {
                            toReplace.add(name + i);
                        }
                    }
                    replaceSprites.add(t);
                }
            } else {
                System.out.println(t.getExportFileName() + " has no needed tags. Weird.");
            }
        }

        System.out.println(toReplace);
        //At this stage, To Replace should be identical to Replacements, just disorganized.
        //To replace contains duplicates based on Needed Tags, while replaceSprites does not.

        //Now we Replace the shapes.
        //TODO: Turn this into the fancy inject/ sprite storing for Demodding.
        for (int i = 0; i < replaceSprites.size(); i++) {
            try {
                ReplaceSprite("_a" + toReplace.get(i) + nameOfSkin, "data/shapes/" + toReplace.get(i) + ".svg", "Gfx_ActualShark.swf");
            } catch (IOException e) {
                System.out.println("FUCK");
                e.printStackTrace();
            }
        }
    }

    //Returns a string list of all SkinNames in a SWF.
    public static List < String > GetAllSkinNames(SWF swf, int level) {
        if (swf != null) {
            //My Variables.
            List < String > namesFound = new ArrayList < String > ();

            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    if (t.getTagName().contains("DefineSprite")) {
                        String tName = ((CharacterTag) t).getExportFileName();

                        //Exclude shades due to different naming scheme.
                        if (!tName.contains("Shades")) {
                            //TODO: Make level actually work
                            //Level is how many "_" back we check a name.

                            int substringPoint = tName.lastIndexOf("_") + 1;

                            tName = tName.substring(substringPoint);

                            if (!namesFound.contains(tName)) {
                                namesFound.add(tName);
                            }
                        }
                    }
                }
            }

            System.out.println("All Skin Names got");
            return namesFound;
        }
        return null;
    }

    //DefineSprite_0_a_b --> 0_a (+_b)
    public static String GetPartNameFromExpName(String expName, String skinName, boolean includeSkin) {
        //No proper name should be shorter then this.
        //DefineSprite_0_a_b
        if (expName.length() >= 18) {
            //Clever bit of code found online that seperates out the numbers.
            String str = expName;
            str = str.replaceAll("[^0-9]+", " ");
            int ID = Integer.parseInt((str.trim().split(" "))[0]);

            //Input: DefineSprite_0_part_set
            if (includeSkin) {
                //Output: _part_set
                return expName.substring(13 + String.valueOf(ID).length(), expName.length());
            } else {
                //Output: _part_
                return expName.substring(13 + String.valueOf(ID).length(), expName.length() - skinName.length());
            }
        }
        return "NAME_TOO_SHORT";
    }

    //Returns SWF at location.
    public static SWF GetSwf(String swfName, Boolean localLocation) {
        String swfPath = "data/" + swfName;

        if (!localLocation) {
            swfPath = swfName;
        }

        File f = new File(swfPath);
        if (f.exists()) {
            System.out.println("Pog " + swfPath);
        } else {
            System.out.println(swfPath + " no...");
        }

        try (FileInputStream fis = new FileInputStream(swfPath)) { //open up a file

            //Pass the InputStream to SWF constructor.
            //Note: There are many variants of the constructor - Do not use single parameter version - is does not process whole SWF.
            SWF swf = new SWF(fis, true);

            return swf;
        } catch (SwfOpenException ex) {
            System.out.println("ERROR: Invalid SWF file");
        } catch (IOException ex) {
            System.out.println("ERROR: Error during SWF opening");
        } catch (InterruptedException ex) {
            System.out.println("ERROR: Parsing interrupted");
        }
        return null;
    }

    //Gets list of all Sprites in a Skin.
    public static List < Tag > GetSpritesList(String skinName, SWF swf) {
        //Get some SWF parameters
        System.out.println("SWF version = " + swf.version);
        System.out.println("FrameCount = " + swf.frameCount);

        //My Variables.
        String nameToFind = skinName;
        List < Tag > tagsFound = new ArrayList < > ();

        for (Tag t: swf.getTags()) {
            if (t instanceof CharacterIdTag) {
                String expName = t.getExportFileName();
                expName = expName.substring(expName.lastIndexOf("_") + 1, expName.length());
                if (expName.equals(nameToFind)) {
                    tagsFound.add(t);
                }
            } else {}

        }
        return tagsFound;
    }

    public static void ExtractSprites(String skinName, SWF swf, SpriteExportMode mode, double exportSize) {
        if (swf != null) { //open up a file
            String nameToFind = skinName;
            String namesFound = "";
            List < Tag > tagsFound = GetSpritesList(skinName, swf);

            //I don't know what any of this is, it was auto generated.
            AbortRetryIgnoreHandler handler = new AbortRetryIgnoreHandler() {

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public int handle(Throwable arg0) {
                    // TODO Auto-generated method stub
                    return 0;
                }

            };

            //Buncha dumb junk we need.
            com.jpexs.decompiler.flash.EventListener evl = swf.getExportEventListener();
            SpriteExportSettings ses = new SpriteExportSettings(mode, exportSize);
            FrameExporter frameExporter = new FrameExporter();

            System.out.println(tagsFound.size() + " tags found");

            //For all of the tags in the list, E X P O R T
            for (Tag t: tagsFound) {
                if (t instanceof DefineSpriteTag) {
                    try {
                        frameExporter.exportSpriteFrames(handler, nameToFind + "/Sprites", swf, ((DefineSpriteTag) t).getCharacterId(), null, ses, evl);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            //Writes the info to a TXT file. Isn't that nice?
            //We don't really need it now though, so I'm commenting it out.
            //out.println(namesFound);
            //out.close();

            System.out.println("OK");
        }
    }

    public static void ExtractShapes(String skinName, SWF swf, ShapeExportMode mode, double exportSize) {
        if (swf != null) { //open up a file
            String nameToFind = skinName;
            List < Tag > tagsFound = GetSpritesList(skinName, swf);

            //I don't know what any of this is, it was auto generated.
            AbortRetryIgnoreHandler handler = new AbortRetryIgnoreHandler() {

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public int handle(Throwable arg0) {
                    // TODO Auto-generated method stub
                    return 0;
                }

            };

            //Buncha dumb junk we need.
            com.jpexs.decompiler.flash.EventListener evl = swf.getExportEventListener();
            ShapeExportSettings ses = new ShapeExportSettings(mode, exportSize);
            ShapeExporter shapeExporter = new ShapeExporter();

            for (Tag t: tagsFound) {
                Set < Integer > needed = new HashSet < > ();
                t.getNeededCharacters(needed);
                List < Tag > neededTags = new ArrayList < > ();

                for (Tag ft: swf.getTags()) {
                    if (ft instanceof CharacterIdTag) {
                        //If Tag found is a sprite belonging to the set we want, add its needed characters(shapes) to an array.
                        if (needed.contains(((CharacterTag) ft).getCharacterId())) {
                            neededTags.add(ft);
                        }
                    }
                }

                //Export needed characters array.
                try {
                    shapeExporter.exportShapes(handler, nameToFind + "/Shapes", swf, new ReadOnlyTagList(neededTags), ses, evl);
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }


            System.out.println("OK");
        }
    }

    //This does not yet work with sprites that have multiple shapes.
    public static void ReplaceSprite(String toReplace, String replacement, String swfPath) throws IOException {
        SvgImporter importer = new SvgImporter();

        SWF swf = GetSwf(swfPath, true);

        String svgText = Helper.readTextFile(replacement);
        ShapeTag tagToReplace = ShapeTagFromName(swf, toReplace);

        try {
            importer.importSvg(tagToReplace, svgText);

            OutputStream outputStream = new FileOutputStream("data/" + swfPath);

            swf.saveTo(outputStream);
        } catch (NullPointerException e) {
            System.out.println("It didn't work... Sad");
            e.printStackTrace();
        }
    }

    public static void ReplaceShape(ShapeTag tagToReplace, String replacementPath, String swfPath) throws IOException {
        SvgImporter importer = new SvgImporter();

        SWF swf = GetSwf(swfPath, false);

        String svgText = Helper.readTextFile(replacementPath);

        try {
            importer.importSvg(tagToReplace, svgText);

            OutputStream outputStream = new FileOutputStream("data/" + swfPath);

            swf.saveTo(outputStream);
        } catch (NullPointerException e) {
            System.out.println("Replacement failed with " + replacementPath);
        }
    }

    public static List < Tag > GetNeededTagsClean(Tag t, SWF swf) {
        Set < Integer > needed = new HashSet < > ();
        t.getNeededCharacters(needed);

        List < Tag > neededTags = new ArrayList < > ();

        for (Tag ft: swf.getTags()) {
            if (ft instanceof CharacterIdTag) {
                if (needed.contains(((CharacterTag) ft).getCharacterId())) {
                    neededTags.add(ft);
                }
            } else {
                //System.out.println("1 Tag " + t.getTagName());
            }
        }

        return neededTags;
    }

    //Takes in NAME ONLY.
    //Should add support for Export Name
    public static ShapeTag ShapeTagFromName(SWF swf, String tagName) {
        if (swf != null) {
            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    if (t.getTagName().contains("DefineSprite")) {
                        String tName = ((CharacterTag) t).getExportFileName();

                        int substringPoint = tName.lastIndexOf("_") + 1;

                        String uName = GetPartNameFromExpName(tName, tName.substring(substringPoint), true);
                        //System.out.println(uName);

                        if (uName.equals(tagName)) {
                            CharacterTag reTag = (CharacterTag) GetNeededTagsClean(t, swf).get(0);

                            return ((ShapeTag) reTag);
                        }
                    }
                }
            }
        }
        System.out.println("No tag with name" + tagName + " Found");
        return null;
    }
}
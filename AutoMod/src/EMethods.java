import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.Helper;

import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.annotation.processing.FilerException;

//Terminology
//ExpName: Export Name. DefineSprite_0_a_b
//PartName: Ending of ExpName. Can include SetName(b)
//SkinName: Very End of ExpName. Name of the set the skin belongs to.
public class EMethods {

    public static void InsertMod(File modFile, SWF swf, String outputFileName) throws IOException, CustomExceptions {
        if (!outputFileName.endsWith(".swf")) {
            outputFileName += ".swf";
        }

        String outputFilePath = modFile.getAbsolutePath() + "/" + outputFileName;

        //Get all parts to replace
        String[] svgNameList = new File(modFile.getAbsolutePath() + "/shapes").list();
        File[] svgList = new File(modFile.getAbsolutePath() + "/shapes").listFiles();

        for (int i = 0; i < svgNameList.length; i++) {
            svgNameList[i] = svgNameList[i].substring(0, svgNameList[i].length() - 4).toLowerCase();
            System.out.println((svgNameList[i]));
        }

        //Read in the offset values from the TXT file
        ArrayList < String > offsetFull = new ArrayList < > ();

        try (BufferedReader br = new BufferedReader(new FileReader(modFile.getAbsolutePath() + "/offsets.txt"))) {
            while (br.ready()) {
                offsetFull.add(br.readLine());
            }
        }

        //OffsetCount should always be evenly divisible by 3.
        Integer offsetCount = offsetFull.size() / 3;

        //Offset File Format
        //_Part_
        //xOffset
        //yOffset
        ArrayList < String > offsetList = new ArrayList < > ();
        ArrayList < Integer > xOffset = new ArrayList < > ();
        ArrayList < Integer > yOffset = new ArrayList < > ();

        //Read info. Only the skin name right now, but maybe there will be other stuff later?
        ArrayList < String > infoFull = new ArrayList < > ();

        try (BufferedReader br = new BufferedReader(new FileReader(modFile.getAbsolutePath() + "/info.txt"))) {
            while (br.ready()) {
                infoFull.add(br.readLine());
            }
        }

        String skinName = infoFull.get(0);

        if (mv.vi(skinName)) {
            throw new CustomExceptions("n");
        }

        for (int i = 0; i < offsetCount; i++) {
            offsetList.add(offsetFull.get((i * 3)));
            xOffset.add(Integer.parseInt(offsetFull.get((i * 3) + 1)));
            yOffset.add(Integer.parseInt(offsetFull.get((i * 3) + 2)));
        }

        List < Tag > spriteLists = GetSpritesList(skinName, swf);

        //Loop through all sprites for the skin, replace the ones we have SVGs for, and add offsets to the ones that need it.
        for (int i = 0; i < spriteLists.size(); i++) {
            String partName = GetPartNameFromExpName(spriteLists.get(i).getExportFileName(), skinName, false).substring(2).toLowerCase();

            Integer partIndex = -1;

            for (int p = 0; p < svgNameList.length; p++) {
                if (svgNameList[p].equals(partName)) {
                    partIndex = p;
                }
            }

            System.out.println("Check " + partName);
            if (partIndex != -1) {
                System.out.println("Valid " + partIndex + " - " + partName);
                if (offsetList.contains(partName)) {
                    System.out.println("Offset");
                    ReplaceSprite(GetPartNameFromExpName(spriteLists.get(i).getExportFileName(), skinName, true), svgList[partIndex].getAbsolutePath(), outputFilePath, swf, false, false);

                    DefineSpriteTag sport = (DefineSpriteTag) spriteLists.get(i);
                    ReadOnlyTagList sportTags = sport.getTags();

                    for (int t = 0; t < sportTags.size(); t++) {
                        if (sportTags.get(t) instanceof PlaceObjectTypeTag) { //Find all PlaceObject(1,2,3,4) tags
                            PlaceObjectTypeTag po = (PlaceObjectTypeTag) sportTags.get(t);
                            MATRIX mat = po.getMatrix();

                            if (mat != null) {
                                mat.translateX = xOffset.get(offsetList.indexOf(partName.toLowerCase()));
                                mat.translateY = yOffset.get(offsetList.indexOf(partName.toLowerCase()));
                            }

                            po.setModified(true);
                            break;
                        }
                    }
                } else {
                    System.out.println("No Offset");
                    ReplaceSprite(GetPartNameFromExpName(spriteLists.get(i).getExportFileName(), skinName, true), svgList[partIndex].getAbsolutePath(), outputFilePath, swf, true, false);
                }
            }
        }

        OutputStream os = new FileOutputStream(outputFilePath);
        try {
            swf.saveTo(os);
            System.out.println("Saved to " + outputFilePath);
        } catch (IOException e) {
            System.out.println("ERROR: Error during SWF saving");
        }

        System.out.println("Done");
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

    //Gets list of all Sprites in a Skin, as CharacterIDTags(Which can be turned into definesprite tags.)
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
                expName = expName.substring(expName.lastIndexOf("_") + 1, expName.length()).toLowerCase();
                if (expName.equals(nameToFind.toLowerCase())) {
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
    public static void ReplaceSprite(String toReplace, String replacement, String swfOutput, SWF swf, Boolean ignoreSVGBounds, Boolean save) throws IOException {
        SvgImporter importer = new SvgImporter();

        String svgText = Helper.readTextFile(replacement);
        ShapeTag tagToReplace = ShapeTagFromName(swf, toReplace);

        try {
            System.out.println(tagToReplace.shapeBounds + ", " + replacement);
            importer.importSvg(tagToReplace, svgText, ignoreSVGBounds);
            System.out.println(tagToReplace.shapeBounds + ", " + replacement);

            if (save) {
                OutputStream outputStream = new FileOutputStream(swfOutput);

                swf.saveTo(outputStream);
            }
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

            OutputStream outputStream = new FileOutputStream(swfPath);

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
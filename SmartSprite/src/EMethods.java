import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EMethods {
    /*public static void main(String[] args) {
        System.out.println(GetNameFromExportName("DefineSprite_20_a_WeaponSwordLand_BattlePassSet2", "BattlePassSet2"));
    }*/

    public static List < String > GetAllValidNames(SWF swf, int level) {
        if (swf != null) {
            //Get some SWF parameters
            System.out.println("SWF version = " + swf.version);
            System.out.println("FrameCount = " + swf.frameCount);

            //My Variables.
            List < String > namesFound = new ArrayList < String > ();

            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    if (t.getTagName().contains("DefineSprite")) {
                        String tName = ((CharacterTag) t).getExportFileName();

                        //Exclude shades due to different naming scheme.
                        if (!tName.contains("Shades")) {
                            //This bit of code gets all "_" in a name. Level is used to determine how many "_" back we check a name.
                            //TODO: Make this actually work.
                            /*int maxLevel = 0;
                            List < Integer > levelIndices = new ArrayList < Integer > ();

                            for (int i = 0; i < tName.toCharArray().length; i++) {
                                if (Character.toString(tName.charAt(i)) == "_") {
                                    maxLevel++;
                                    levelIndices.add(i);
                                }
                            }*/

                            //For example: DefineSprite_20_a_WeaponSwordLand_BattlePassSet2
                            //Level 0 = BattlePassSet2
                            //Level 1 = WeaponSwordLand_BattlePassSet2

                            int substringPoint = tName.lastIndexOf("_") + 1;

                            /*if(maxLevel - level - 1 >= 0){
                                substringPoint = levelIndices.get(maxLevel - level - 1) + 1;
                            }*/

                            tName = tName.substring(substringPoint);

                            if (!namesFound.contains(tName)) {
                                namesFound.add(tName);
                                //numsFound.add(((CharacterTag) t).getCharacterId());
                            }
                        }
                    }
                } else {
                    //System.out.println("1 Tag " + t.getTagName());
                }
            }

            System.out.println("OK");
            return namesFound;
        }
        return null;
    }

    public static String GetNameFromExportName(String ExpName, String NameOfSet) {
        //No proper name should be shorter then this.
        //DefineSprite_0_a_b
        if (ExpName.length() >= 18) {

            //Clever bit of code found online that seperates out the numbers.
            String str = ExpName;
            str = str.replaceAll("[^0-9]+", " ");
            int ID = Integer.parseInt((str.trim().split(" "))[0]);
            System.out.println(ID);

            return ExpName.substring(13 + String.valueOf(ID).length(), ExpName.length() - NameOfSet.length());
        }
        return "NAME_TOO_SHORT";
    }

    public static SWF GetSwf(String swfName, Boolean localLocation) {
        String swfPath = "data/" + swfName;

        if (!localLocation) {
            swfPath = swfName;
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

    public static void ExtractSprites(String nameOfSkin, SWF swf, SpriteExportMode mode) {
        if (swf != null) { //open up a file

            //Get some SWF parameters
            System.out.println("SWF version = " + swf.version);
            System.out.println("FrameCount = " + swf.frameCount);

            //My Variables.
            String nameToFind = nameOfSkin;
            String namesFound = "";
            List < Tag > tagsFound = new ArrayList < > ();
            PrintWriter out = null;

            try {
                out = new PrintWriter(nameToFind + ".txt");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    //Check if name matches.
                    //Originally used String.endsWith(), but this meant "StreetReaper" would be extracted along with "Reaper". 
                    //Very few instances of this specific problem actually occured, but it's a simple fix.
                    String expName = t.getExportFileName();
                    expName = expName.substring(expName.lastIndexOf("_") + 1, expName.length());
                    if (expName == nameToFind) {
                        //We really only need fullName. The rest of the info is just cut up bits of fullName.
                        int charId = ((CharacterIdTag) t).getCharacterId();
                        String fullName = t.getExportFileName();
                        String partName = t.getExportFileName().substring(13 + String.valueOf(((CharacterTag) t).getCharacterId()).length(), t.getExportFileName().length() - nameToFind.length());

                        namesFound += (charId + "\n " + partName + "\n" + fullName + "\n \n");
                        tagsFound.add(t);
                    }
                } else {
                    //System.out.println("1 Tag " + t.getTagName());
                }
            }

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

            com.jpexs.decompiler.flash.EventListener evl = swf.getExportEventListener();

            SpriteExportSettings ses = new SpriteExportSettings(mode, 100);
            FrameExporter frameExporter = new FrameExporter();

            //For all of the tags in the list, E X P O R T
            for (Tag t: tagsFound) {
                if (t instanceof DefineSpriteTag) {
                    //ExtractAssociatedShapes(t, swfName, nameToFind);
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
            out.println(namesFound);
            out.close();

            System.out.println("OK");
        }
    }

    public static void ExtractShapes(String nameOfSkin, SWF swf, ShapeExportMode mode) {
        if (swf != null) { //open up a file

            //Get some SWF parameters
            System.out.println("SWF version = " + swf.version);
            System.out.println("FrameCount = " + swf.frameCount);

            //My Variables.
            String nameToFind = nameOfSkin;
            //String namesFound = "";
            List < Tag > tagsFound = new ArrayList < > ();

            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    //Check if name matches.
                    //Originally used String.endsWith(), but this meant "StreetReaper" would be extracted along with "Reaper". 
                    //Very few instances of this specific problem actually occured, but it's a simple fix.
                    String expName = t.getExportFileName();
                    expName = expName.substring(expName.lastIndexOf("_") + 1, expName.length());
                    System.out.println((expName + " vs " + nameToFind + " bool " + (expName.equals(nameToFind))));
                    if (expName.equals(nameToFind)) {
                        //We really only need fullName. The rest of the info is just cut up bits of fullName.
                        /*int charId = ((CharacterIdTag) t).getCharacterId();
                        String fullName = t.getExportFileName();
                        String partName = t.getExportFileName().substring(13 + String.valueOf(((CharacterTag) t).getCharacterId()).length(), t.getExportFileName().length() - nameToFind.length());

                        namesFound += (charId + "\n " + partName + "\n" + fullName + "\n \n");*/
                        tagsFound.add(t);
                    }
                } else {
                    //System.out.println("1 Tag " + t.getTagName());
                }
            }

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

            com.jpexs.decompiler.flash.EventListener evl = swf.getExportEventListener();

            ShapeExportSettings ses = new ShapeExportSettings(mode, 100);
            ShapeExporter shapeExporter = new ShapeExporter();

            for (Tag t: tagsFound) {
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

    //Not sure if this one is still useful, but I ain't deleting it.
    public static void ExtractAssociatedShapes(Tag tag, String sourceSwf, String outputLocation) {
        Set < Integer > needed = new HashSet < > ();
        tag.getNeededCharacters(needed);

        try (FileInputStream fis = new FileInputStream("data/" + sourceSwf)) { //open up a file

            //Pass the InputStream to SWF constructor.
            //Note: There are many variants of the constructor - Do not use single parameter version - is does not process whole SWF.
            SWF swf = new SWF(fis, true);

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

            List < Tag > tagsFound = new ArrayList < > ();

            for (Tag t: swf.getTags()) {
                if (t instanceof CharacterIdTag) {
                    if (needed.contains(((CharacterTag) t).getCharacterId())) {
                        tagsFound.add(t);
                    }
                } else {
                    //System.out.println("1 Tag " + t.getTagName());
                }
            }

            com.jpexs.decompiler.flash.EventListener evl = swf.getExportEventListener();

            ShapeExportSettings ses = new ShapeExportSettings(ShapeExportMode.SWF, 100);
            ShapeExporter shapeExporter = new ShapeExporter();

            //for (Tag t: tagsFound) {
            shapeExporter.exportShapes(handler, outputLocation + "/Shapes", swf, new ReadOnlyTagList(tagsFound), ses, evl);
            //}

            System.out.println("OK");
        } catch (SwfOpenException ex) {
            System.out.println("ERROR: Invalid SWF file");
        } catch (IOException ex) {
            System.out.println("ERROR: Error during SWF opening");
        } catch (InterruptedException ex) {
            System.out.println("ERROR: Parsing interrupted");
        }
    }
}
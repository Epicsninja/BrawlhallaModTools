import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class GauntletRejigger {

    static String badColor = "#52524b";
    static String goodColor = "555555";

    public static void main(String[] args) throws IOException {

        System.out.println(stringFromSVG("data/1012.svg"));
    }

    public static String stringFromSVG(String path){
        Path realPath = Paths.get(path);

        return realPath.toString();
    }
}
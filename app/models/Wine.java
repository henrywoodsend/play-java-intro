package models;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Wine {
    public int id;
    public String name;
    public String simple_name;

    public String imgsrc;
    public String wine_desc;
    public String region_desc;
    public String grape;
    public String color;

    public Wine(int id,String name){
        this.id=id;
        this.name=name;
        this.simple_name=this.deAccent(name);
    }
    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}

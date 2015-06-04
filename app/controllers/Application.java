package controllers;

import models.Wine;
import play.*;
import play.mvc.*;
import views.html.*;
import play.db.*;
import play.cache.*;

import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import static play.libs.Json.toJson;
import javax.inject.Inject;

public class Application extends Controller {

    @Inject CacheApi cache;

    public Result index() {
        return ok(index.render("Hello Henry Woodsend"));
    }

    public Result index2() {
        return ok(index2.render("Hello"));
    }

    public Result getWines(String searchstring) {

        ArrayList<Wine> wines = cache.get("wines");
        if (wines==null) {

            System.out.println("Retrieving from DB");

            wines = new ArrayList<Wine>();
            Connection connection = DB.getConnection();
            try {
                PreparedStatement stmt = connection.prepareStatement("SELECT WineID,WineDescription FROM Wine WHERE Beta=1");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    wines.add(new Wine(rs.getInt("WineID"), rs.getString("WineDescription")));
                }
                stmt.close();
                cache.set("wines",wines);
            } catch (SQLException se) {
                System.out.println(se.toString());
            } finally {
                try {connection.close();} catch (Exception ignore) {}
            }
        }
        ArrayList<Wine> return_wines = new ArrayList<Wine>();

        //Create array of search terms from space separated string
        String terms[] = searchstring.split(" ");

        int c=0;
        for (Wine w:wines){
            int matches=0;
            for (String s:terms){
                if (StringUtils.containsIgnoreCase(w.simple_name,s))
                    matches++;
            }
            if (matches==terms.length){
                return_wines.add(w);
                c++;
                if (c==10)
                    break;
            }
        }
        return ok(toJson(return_wines));
    }

    public Result getWine(Long id){

        Wine thewine = new Wine(0,"");

        Connection connection = DB.getConnection();
        try {
            //Wine Avg over vintage
            PreparedStatement stmt = connection.prepareStatement("" +
                    "SELECT d.DomainName,w.WineName,w.Image_ext,w.WineDescription,w.Colour,c.Cru,c.Score AS cru_score," +
                    "a.Appellation,a.Country,a.region,a.Sub_Region,a.category,a.Score AS appellation_score,a.Classification,a.IsCruArea," +
                    "wg.Grape_1,wg.Grape_2,wg.Grape_3,wg.Grape_1pc,wg.Grape_2pc,wg.Grape_3pc," +
                    "w.BottlesProduced,w.BottlesProduced_Score,w.VineAge,w.VineAge_Score,w.VineYardArea,w.Yield,w.Yield_Score,w.Viticulture_Score," +
                    "w.RestaurantPresence,w.RestaurantPresence_Score,w.WineRank,w.WineRank_Score,wa.Avg_Life,wa.Avg_LifeScore," +
                    "wa.Avg_BD_Rating,wa.Avg_JR_Rating,wa.Avg_JCL_Rating,wa.Avg_CriticScore,wa.Avg_Price,wa.Avg_PriceScore," +
                    "wa.Avg_Quality,wa.Avg_Brand,wa.Avg_Economics,wa.Avg_Liquidity,wa.Avg_OverallScore FROM "+
                    "Wine w " +
                    "INNER JOIN Domain d ON w.DomainID=d.DomainID " +
                    "LEFT JOIN Cru c ON w.CruID=c.CruID " +
                    "LEFT JOIN Appellation a ON w.AppellationID=a.AppellationID " +
                    "LEFT JOIN Wine_Grape wg ON w.WineID=wg.WineID " +
                    "INNER JOIN Wine_Avg wa ON w.WineID=wa.WineID " +
                    "WHERE w.WineID=?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                thewine.imgsrc=id+".jpg";
                thewine.name=rs.getString("winename");
                thewine.wine_desc=rs.getString("winedescription");
                thewine.color=rs.getString("colour");

                String region = rs.getString("region");
                String subregion = rs.getString("sub_region");
                String appellation=rs.getString("appellation");
                String classification=rs.getString("Classification");


                thewine.region_desc = rs.getString("Country") ;
                if (region.length()>0)
                    thewine.region_desc+=" > " + region;

                if (subregion.length()>0)
                    thewine.region_desc+=" > " + subregion;

                if (appellation.length()>0 && !appellation.equalsIgnoreCase(region))
                    thewine.region_desc+=" > " + appellation;

                if (classification.length()>0)
                    thewine.region_desc+=" " + classification;


                String grape1 = rs.getString("Grape_1");
                String grape2 = rs.getString("Grape_2");
                String grape3 = rs.getString("Grape_3");
                String grape1pc = rs.getString("Grape_1pc");
                String grape2pc = rs.getString("Grape_2pc");
                String grape3pc = rs.getString("Grape_3pc");

                thewine.grape = grape1;

                if (grape1pc.length()==0 && grape2.length()==0)
                    thewine.grape += " (100%)" ;
                else if (grape1pc.length()>0)
                    thewine.grape += " ("+grape1pc+"%)";

                thewine.grape += grape2.length()==0 ? "" : ", "+grape2;
                thewine.grape += grape2pc.length()==0 ? "" : " ("+grape2pc+"%)";

                thewine.grape += grape3.length()==0 ? "" : ", "+grape3;
                thewine.grape += grape3pc.length()==0 ? "" : " ("+grape3pc+"%)";

            }
            stmt.close();
        } catch (SQLException se) {
            System.out.println(se.toString());
        } finally {
            try {connection.close();} catch (Exception ignore) {}
        }

        return ok(wine.render(thewine));
    }

}

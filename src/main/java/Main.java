import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static final String baseURL = "https://iswa.gsfc.nasa.gov/iswa_data_tree/model/solar/assa/hole/";

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        List<String[]> areas  = new ArrayList<>();
        areas.add(new String[]{"year", "month", "day", "percent"});
        String specURL = baseURL;
        for (int year = 2013; year <= 2025; year++) {
            for (int month = 1; month <= 12; month++) {
                if(year == 2013 && month<11)
                    month = 11;
                //Adds '0' to the beginning of each month if necessary
                String monthStr;
                if (month < 10)
                    monthStr = "0" + month;
                else
                    monthStr = month + "";


                for (int day = 1; day < numDays(month, year); day++) {
                    specURL = baseURL + year + "/" + monthStr + "/";
                    //Adds '0' to the beginning of each day if necessary
                    String dayStr;
                    if (day < 10)
                        dayStr = "0" + day;
                    else
                        dayStr = day + "";

                    boolean found = false;
                    int count = 1;
                    String countStr;
                    while(!found && count<24){
                        if(count<10)
                            countStr="0"+count;
                        else
                            countStr = count+"";
                        specURL = specURL+"ASSA_Hole_"+year+monthStr+dayStr+countStr+".txt";
                        try {
                            //Convert string to URL
                            URI uri = URI.create(specURL);
                            URL url = uri.toURL();

                            //Connect and pull data from URL and store in String
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                            connection.setRequestProperty("Connection", "keep-alive");
                            connection.setRequestProperty("Referer", "https://iswa.gsfc.nasa.gov/");

                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String line = reader.readLine();
                                double area = 0;
                                while(line != null){
                                    if(line.length() == 36){
                                        if(line.startsWith("Area (thousandth of solar disk) :")){
                                            area+= Double.parseDouble(line.substring(33));
                                        }
                                    }
                                    line = reader.readLine();
                                }
                                areas.add(new String[]{year+"",monthStr, dayStr, area+""});
                                reader.close();
                                found = true;
                            } else {
                                System.out.println("GET request failed for "+year+"/"+month+"/"+day+" at "+countStr+":00. Response code: " + responseCode);
                            }
                        } catch (Exception e) {
                            System.out.println("Failed for "+year+"/"+month+"/"+day+" at "+countStr+":00 "+e);/*Okay because this is not production software*/
                        }
                        count++;
                    }
                }
            }
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter("moreData.csv"))) {
            writer.writeAll(areas);
            System.out.println("CSV file created successfully with OpenCSV");
        } catch (IOException e) {
            System.err.println("Error writing CSV file with OpenCSV: " + e.getMessage());
        }
    }

    //Gets number of days in a given month
    public static int numDays(int m, int y) {
        if (m == 2 && y % 4 != 0)
            return 28;
        else if (m == 2)
            return 29;
        else if (m == 9 || m == 4 || m == 6 || m == 11)
            return 30;
        else
            return 31;
    }


}

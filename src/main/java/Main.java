import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import com.opencsv.CSVWriter;
import java.io.FileWriter;

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

                try{
                    for (int day = 1; day < numDays(month, year); day++) {

                        //Adds '0' to the beginning of each day if necessary
                        String dayStr;
                        if (day < 10)
                            dayStr = "0" + day;
                        else
                            dayStr = day + "";

                        boolean areaFound = false;
                        int count = 0;
                        String countStr;
                        int totalLoop = 0;
                        double area = 0;
                        BufferedReader reader = null;
                        while (count < 24) {
                            specURL = baseURL + year + "/" + monthStr + "/";
                            if (count < 10)
                                countStr = "0" + count;
                            else
                                countStr = count + "";
                            specURL = specURL + "ASSA_Hole_" + year + monthStr + dayStr + countStr + ".txt";
                            try {
                                String[] command = {
                                    "curl",
                                    "-s", // Silent
                                    "-L", // Follow redirects
                                    "-A", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", // User-Agent
                                    "-e", "https://iswa.gsfc.nasa.gov/", // Referer
                                    specURL
                                };

                                ProcessBuilder pb = new ProcessBuilder(command);
                                Process process = pb.start();
                                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line = reader.readLine();

                                while (line != null) {
                                    if (line.length() == 36) {
                                        if (line.startsWith("Area (thousandth of solar disk) :")) {
                                            area += Double.parseDouble(line.substring(33));
                                            System.out.println("Found for " + year + "/" + month + "/" + day + " at " + countStr + ":00 ");
                                            areaFound = true;
                                            totalLoop++;
                                        }
                                    }
                                    line = reader.readLine();
                                }
                            } catch (Exception e) {
                                System.out.println("Failed for " + year + "/" + month + "/" + day + " at " + countStr + ":00 " + e);/*Okay because this is not production software*/
                            }
                            count++;
                        }
                        if (areaFound)
                            areas.add(new String[]{year + "", monthStr, dayStr, (area / totalLoop) + ""});
                        reader.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
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

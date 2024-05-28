package demo;

import java.util.Scanner;
import org.jsoup.Jsoup;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.io.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;







public class App {
    private static Scanner scanner = new Scanner(System.in); 

    public static void main(String[] args) {
        int choice;

        do {
            System.out.println("Type a valid number for your desired action:");
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] Exit");
            choice = Integer.parseInt(scanner.nextLine()); 

            switch (choice) {
                case 1:
                    showUpdates();
                    break;
                case 2:
                    addURL();
                    break;
                case 3:
                    removeURL();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice! Please enter a valid number.");
            }
        } while (choice != 4);
        
        
    }


    public static String[] extractSiteInfo(String websiteUrl) {
        String[] siteInfo = new String[2];

        try {
            URL url = new URL(websiteUrl);
            String host = url.getHost();
            siteInfo[0] = host;

            Document doc = Jsoup.connect(websiteUrl).get();
            String rssUrl = doc.select("link[type='application/rss+xml']").attr("abs:href");
            siteInfo[1] = rssUrl;
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL format!");
            return new String[]{"", ""};
        } catch (IOException e) {
            System.out.println("An error occurred while connecting to the website: " + e.getMessage());
            return new String[]{"", ""};
        }

        return siteInfo;
    }
    
    public static void showallrss() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String rssUrl = parts[2];
                    retrieveRssContent(rssUrl);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading data.txt: " + e.getMessage());
        }
    }

    private static void showUpdates() {
        ArrayList<String> titles = readTitlesFromFile("data.txt");
        if (titles.isEmpty()) {
            System.out.println("No titles found!");
            return;
        }
    
        System.out.println("Website Titles:");
        System.out.println("[0] All website");
        for (int i = 0; i < titles.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + titles.get(i));
        }
        System.out.println("[-1] exit");
    
        Scanner localScanner = new Scanner(System.in); 
        System.out.println("Enter the number of the title you want to view:");
        int choice = Integer.parseInt(localScanner.nextLine()); 
    
        if(choice == -1) {
            System.out.println("");
        }else if(choice== 0) {
            showallrss();
        }else if (choice >= 1 && choice < titles.size()+1) {
            retrieveRssContent(findRSS(titles.get(choice-1)));
        }else {
            System.out.println("Invalid choice!");
        } 
    
    }
    
    public static String findRSS(String siteName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String name = parts[0];
                    String rssUrl = parts[2];
                    if (name.equals(siteName)) {
                        return rssUrl ;
                    }
                }
            }
            System.out.println("RSS for " + siteName + " not found in data.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while reading data.txt: " + e.getMessage());
        }
        return "";
    }


    public static void retrieveRssContent(String rssUrl) {
        try {
            Document doc = Jsoup.connect(rssUrl).get();
            Elements itemElements = doc.select("item");

            for (int i = 0; i < 5 && i < itemElements.size(); ++i) {
                Element itemElement = itemElements.get(i);
                System.out.println("Title: " + getElementTextContent(itemElement, "title"));
                System.out.println("Link: " + getElementTextContent(itemElement, "link"));
                System.out.println("Description: " + getElementTextContent(itemElement, "description"));
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    private static String getElementTextContent(Element parentElement, String tagName) {
        Element element = parentElement.selectFirst(tagName);
        if (element != null) {
            return element.text();
        }
        return "";
    }
    
    
    private static ArrayList<String> readTitlesFromFile(String filename) {
        ArrayList<String> titles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 1) { 
                    titles.add(parts[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
        return titles;
    }


    private static void addURL() {
        System.out.println("Enter URL details in the format:");
        String url = scanner.nextLine();
        
        if (checkIfURLExists(url)) {
            System.out.println("This URL "+url+" already exists in the file.");
            return;
        }

        String[] siteInfo = extractSiteInfo(url);
        String websiteName = siteInfo[0];
        String rssUrl = siteInfo[1];

        String output = websiteName + ";" + url;

        if (!rssUrl.isEmpty()) {
            output += ";" + rssUrl;
            System.out.println("RSS URL extracted and saved successfully!");
        } else {
            System.out.println("RSS URL not found!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt", true))) {
            writer.write(output);
            writer.newLine();
            System.out.println("URL "+url+" added successfully!");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    private static boolean checkIfURLExists(String url) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(url)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
        return false;
    }

    private static void removeURL() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the URL to remove:");
        String urlToRemove = scanner.nextLine();
        removeURLinfile(urlToRemove);
    }

    public static void removeURLinfile(String siteName) {
        File inputFile = new File("data.txt");
        File tempFile = new File("tempData.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String name = parts[1].trim();
                    if (name.equals(siteName)) {
                        found = true;
                        continue; 
                    }
                    writer.write(line + System.lineSeparator());
                }
            }

            if (!found) {
                tempFile.delete();
                System.out.println("Site not found in data.txt");
            }

            if (found) {
                System.out.println("Site " + siteName + " removed from data.txt");
            }
            

        } catch (IOException e) {
            System.out.println("An error occurred while processing the file: " + e.getMessage());
        }

        
        if (!inputFile.delete()) {
            System.out.println("Could not delete the original file.");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename the temporary file.");
        }
    }


    
 
}

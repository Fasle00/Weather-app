import java.net.URI;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Scanner;

public class main {

    /**
     * this project requires at least JAVA v16
     * @implNote Needs Java v16
     */
    public static void main(String[] args) throws Exception {
        Scanner locationScanner = new Scanner(System.in);
        System.out.println("Where do you want your weather from?");
        System.out.print("Location: ");
        runWeather(locationScanner.nextLine());

        runTranscript(); // this is the function to transcribe a mp3 file
    }

    public static void runWeather(String location) throws Exception{

        String apKey = Key.weatherKey;
        //String location = "Umeå";
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + apKey + "&units=metric";

        HttpRequest weatherGet = HttpRequest.newBuilder()
                .uri(new URI(url))
                .build();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(weatherGet, BodyHandlers.ofString());

        System.out.println(postResponse.body());
        if (postResponse.body().contains("\"cod\":\"404\"")) return;

        System.out.println("Weather: " + getWeather(postResponse.body()));
        System.out.println("Detailed: " + getWeatherDescription(postResponse.body()));
        System.out.println("Det är " + getTemp(postResponse.body()) + " c grader ute.");
    }
    public static void runTranscript() throws Exception{
        // this is the link to your sound file, you need to upload it somewhere and use that url to get it transcribed
        String apSrc ="https://github.com/johnmarty3/JavaAPITutorial/blob/main/Thirsty.mp4?raw=true";

        String apKey = Key.transcriptKey;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", apKey)
                .POST(BodyPublishers.ofString("{\n\"audio_url\": \"" + apSrc + "\" \n }" ))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
        String id = getTransId(postResponse.body());
        String status;

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + id))
                .header("Authorization", apKey)
                .build();


        int repeat = 0;
        HttpResponse<String> getResponse;
        while (true){
            getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
            status = getStatus(getResponse.body());
            System.out.println(status);

            // this checks if it has finished or got an error or if it has gone more than 60 sec
            // when either of them happens it breaks the loop
            if ("completed".equals(status) || "error".equals(status) || repeat++ >= 60) break;

            Thread.sleep(1000);
        }

        System.out.println(getResponse.body());

        System.out.println(status);
        System.out.println(getText(getResponse.body()));
    }

    public static String getWeather(String s){
        return s.substring(s.indexOf("main")).substring(7, s.substring(s.indexOf("main")).indexOf(',')-1);
    }
    public static String getWeatherDescription(String s){
        return s.substring(s.indexOf("description")).substring(14, s.substring(s.indexOf("description")).indexOf(',')-1);
    }
    public static String getTemp(String s) {
        String temp = s.substring(s.indexOf("main")+4);
        temp = temp.substring(temp.indexOf("main")+7);
        temp = temp.substring(0,temp.indexOf('}'));
        temp = temp.substring(7,temp.indexOf(','));
        return temp;
    }

    public static String getTransId(String s){
        return s.substring(s.indexOf(':') + 3, s.indexOf(',') - 1);
    }
    public static String getStatus(String s){
        return s.substring(s.indexOf("status")).substring(10, s.substring(s.indexOf("status")).indexOf(',')-1);
    }
    public static String getText(String s){
        int start = s.substring(s.indexOf("\"audio_url\": ")).indexOf("text") + 8 + s.indexOf("\"audio_url\": ");
        String out = s.substring(start).substring(0, s.substring(start).indexOf("\","));
        if (out.equals("null")) return "there is no text";
        else return out.substring(0,out.length()-1);
    }

}

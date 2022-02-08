import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Geocoding {
	String roadAddress, jibunAddress, englishAddress;
	double x,y;
	double distance;
	
	public void geocoding(String location) {
		JsonParser jsonParser = new JsonParser();
		JsonArray jsonArray;

		String clientId = "o9au7azy48";
		String clientSecret = "GPmRaXw3lGI2UXGZBFRdV8E4ySluxEOwQw2n1Tfb";
		try {
			String addr = URLEncoder.encode(location, "utf-8");
			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + addr; 
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			int responseCode = con.getResponseCode();
			BufferedReader br;
			if(responseCode==200) { 
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {  
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			} 
			br.close();
			JsonObject jsonObj = (JsonObject)jsonParser.parse(response.toString());
			jsonArray = (JsonArray)jsonObj.get("addresses");
			for (int i = 0; i < jsonArray.size(); i++) {          
				JsonObject object = (JsonObject) jsonArray.get(i);
				roadAddress = object.get("roadAddress").getAsString();
				jibunAddress = object.get("jibunAddress").getAsString();
				englishAddress = object.get("englishAddress").getAsString();
				x = object.get("x").getAsDouble();
				y = object.get("y").getAsDouble();
				distance = object.get("distance").getAsDouble();
				/*
				System.out.println("도로명주소 : " + object.get("roadAddress"));     
				System.out.println("지번주소: " + object.get("jibunAddress"));   
				System.out.println("영어주소 : " + object.get("englishAddress"));    
				System.out.println("x좌표: " + object.get("x"));  
				System.out.println("y좌표 : " + object.get("y"));  
				System.out.println("------------------------"); 
				double a = object.get("x").getAsDouble();
				System.out.println(a);
				*/
			} 

		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	public double getDistance(String location) {
		JsonParser jsonParser = new JsonParser();
		JsonArray jsonArray;

		String clientId = "o9au7azy48";
		String clientSecret = "GPmRaXw3lGI2UXGZBFRdV8E4ySluxEOwQw2n1Tfb";
		try {
			String addr = URLEncoder.encode(location, "utf-8");
			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + addr +"&coordinate="+getX()+","+getY(); 
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			int responseCode = con.getResponseCode();
			BufferedReader br;
			if(responseCode==200) { 
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {  
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			} 
			br.close();
			JsonObject jsonObj = (JsonObject)jsonParser.parse(response.toString());
			jsonArray = (JsonArray)jsonObj.get("addresses");
			for (int i = 0; i < jsonArray.size(); i++) {          
				JsonObject object = (JsonObject) jsonArray.get(i);
				distance = object.get("distance").getAsDouble();
			} 

		} catch (Exception e) {
			System.out.println(e);
		}

		return distance;
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public String getRoadAddress() {
		return roadAddress;
	}
	public String getJibunAddress() {
		return jibunAddress;
	}
	public String getEnglishAddress() {
		return englishAddress;
	}
}
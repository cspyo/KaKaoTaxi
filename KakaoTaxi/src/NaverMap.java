import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.ImageIcon;

public class NaverMap {
	int width = 600;
	int height = 600;
	
	public void downloadMap(String fileName,double X,double Y) {
		String clientId = "o9au7azy48";
		String clientSecret = "GPmRaXw3lGI2UXGZBFRdV8E4ySluxEOwQw2n1Tfb";
		try {
			//String addr = URLEncoder.encode(fileName, "utf-8");

			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w="+width+"&h="+height+"&markers=type:a|size:mid|color:green|pos:"+X+"%20"+Y+"|label:s"; 

			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			InputStream is = con.getInputStream();
			OutputStream outputStream = new FileOutputStream(fileName);
			byte[] buffer = new byte[2048];
			int length;
			while((length = is.read(buffer)) != -1) {
				outputStream.write(buffer,0,length);
			}
			is.close();
			outputStream.close();
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public void downloadMarkerMap(String fileName,double sX, double sY, double dX, double dY) {
		String clientId = "o9au7azy48";
		String clientSecret = "GPmRaXw3lGI2UXGZBFRdV8E4ySluxEOwQw2n1Tfb";
		try {
			//String addr = URLEncoder.encode(fileName, "utf-8");

			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w="+width+"&h="+height+"&markers=type:a|size:mid|color:green|pos:"+sX+"%20"+sY+"|label:s&markers=type:a|size:mid|color:red|pos:"+dX+"%20"+dY+"|label:d"; 

			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			InputStream is = con.getInputStream();
			OutputStream outputStream = new FileOutputStream(fileName);
			byte[] buffer = new byte[2048];
			int length;
			while((length = is.read(buffer)) != -1) {
				outputStream.write(buffer,0,length);
			}
			is.close();
			outputStream.close();
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public ImageIcon getMap(String fileName) {
		return new ImageIcon((new ImageIcon(fileName)).getImage().getScaledInstance(600,600,java.awt.Image.SCALE_SMOOTH));
	}

	public void fileDelete(String fileName) {
		File f = new File(fileName);
		f.delete();
	}
}

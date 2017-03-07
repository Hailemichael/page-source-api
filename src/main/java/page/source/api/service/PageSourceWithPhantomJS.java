package page.source.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/source")
public class PageSourceWithPhantomJS {
	private static final Logger logger = LogManager.getLogger(PageSourceWithPhantomJS.class.getName());
	protected static DesiredCapabilities capabilities;
	private static boolean DEBUG = false;

	@POST
	@Consumes("text/plain")
	@Produces("text/plain")
	public String getPageSource(String url) throws MalformedURLException, ProtocolException, IOException, SecurityException,
			NoSuchElementException, TimeoutException {
		if (DEBUG)
			logger.info("User Home" + System.getProperty("user.home"));
		String pageSourceString = null;
		WebDriver driver = null;

		if (System.getProperty("debug") == "yes") {
			DEBUG = true;
		}

		try {
			boolean isvalid = checkUrlValidity(url);
			if (isvalid == true) {
				if (System.getProperty("os.name").contains("Linux")) {
					File file = new File(
							Thread.currentThread().getContextClassLoader().getResource("phantomjs").getFile());
					if (!file.canExecute()) {
						file.setExecutable(true);
					}
					System.setProperty("phantomjs.binary.path", file.getAbsolutePath());
					if (DEBUG)
						logger.info(System.getProperty("phantomjs.binary.path"));
				} else {
					File file = new File(
							Thread.currentThread().getContextClassLoader().getResource("phantomjs.exe").getFile());
					System.setProperty("phantomjs.binary.path", file.getAbsolutePath());
					if (DEBUG)
						logger.info(System.getProperty("phantomjs.binary.path"));
				}

				capabilities = new DesiredCapabilities();
				capabilities.setJavascriptEnabled(true);
				capabilities.setCapability("takesScreenshot", false);
				capabilities.setCapability("outputEncoding", "utf8");
				driver = new PhantomJSDriver(capabilities);
				driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

				driver.get(url);

				pageSourceString = driver.getPageSource();

				if (DEBUG)
					logger.info("Page Source: " + pageSourceString);

			}

		} catch (SecurityException e) {
			logger.error("SecurityException thrown while setting phantomjs executable: " + e.getMessage());
			throw e;
		} catch (NoSuchElementException e) {// not required here
			logger.error("Exception occured in Phantomjs driver: " + e.getMessage());
			throw e;
		} catch (TimeoutException e) {
			logger.error("TimeoutException occured in Phantomjs driver: " + e.getMessage());
			throw e;
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException thrown for the URL: " + url + "\nException details: " + e.getMessage());
			throw e;
		} catch (ProtocolException e) {
			logger.error("ProtocolException thrown while setting Http Method to HEAD for: " + url
					+ "\nException details: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.error("IOException thrown while opening connection to: " + url + "\nException details: "
					+ e.getMessage());
			throw e;
		} finally {
			driver.quit();
		}

		return pageSourceString;
	}

	@Path("{c}")
	@GET
	@Produces("application/xml")
	public String convertCtoFfromInput(@PathParam("c") Double c) {
		Double fahrenheit;
		Double celsius = c;
		fahrenheit = ((celsius * 9) / 5) + 32;

		String result = "@Produces(\"application/xml\") Output: \n\nC to F Converter Output: \n\n" + fahrenheit;
		return "<ctofservice>" + "<celsius>" + celsius + "</celsius>" + "<ctofoutput>" + result + "</ctofoutput>"
				+ "</ctofservice>";
	}

	private boolean checkUrlValidity(String url) throws MalformedURLException, ProtocolException, IOException {
		boolean isValid = false;
		URL pageUrl = null;
		int responseCode = 0;
		pageUrl = new URL(url);
		HttpURLConnection huc = (HttpURLConnection) pageUrl.openConnection();
		if (DEBUG)
			logger.info("Making Http HEAD request to url: " + url);
		huc.setRequestMethod("HEAD");
		huc.connect();
		responseCode = huc.getResponseCode();
		if (DEBUG)
			logger.info("Response code for Head request: " + responseCode);
		if (responseCode == 200) {
			isValid = true;
		}
		return isValid;
	}

}

import java.io.File;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.perfectomobile.httpclient.MediaType;
import com.perfectomobile.httpclient.utils.FileUtils;
import com.perfectomobile.selenium.MobileDriver;
import com.perfectomobile.selenium.api.IMobileDevice;
import com.perfectomobile.selenium.api.IMobileWebDriver;
import com.perfectomobile.selenium.by.ByMobile;
import com.perfectomobile.selenium.options.MobileDeviceFindOptions;
import com.perfectomobile.selenium.options.MobileDeviceOS;
import com.perfectomobile.selenium.options.MobileDeviceProperty;
import com.perfectomobile.selenium.options.visual.MobileNextOptions;

public class PerfectoMaster implements Runnable {
	private IMobileWebDriver nativeDriver = null;
	private IMobileWebDriver visualDriver = null;
	private MobileDriver driver = null;
	private IMobileDevice device = null;
	private String toDate = null;
	private String fromDate = null;
	private String toCity = null;
	private String fromCity = null;
	private int passengers = 1;
	private String flightClass = null;

	public PerfectoMaster() {
		driver = new MobileDriver();
	}

	public void setNativeDriver(IMobileWebDriver nativeDriver) {
		this.nativeDriver = nativeDriver;
	}

	public String gettoDate() {
		return toDate;
	}

	public void settoDate(String toDate) {
		this.toDate = toDate;
	}

	public String getfromDate() {
		return fromDate;
	}

	public void setfromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String gettoCity() {
		return toCity;
	}

	public void settoCity(String toCity) {
		this.toCity = toCity;
	}

	public String getfromCity() {
		return fromCity;
	}

	public void setfromCity(String fromCity) {
		this.fromCity = fromCity;
	}

	public int getPassengers() {
		return passengers;
	}

	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}

	public String getFlightClass() {
		return flightClass;
	}

	public void setFlightClass(String flightClass) {
		this.flightClass = flightClass;
	}

	public void setVisualDriver(IMobileWebDriver visualDriver) {
		this.visualDriver = visualDriver;
	}

	public void setDevice(MobileDeviceFindOptions options) {
		device = driver.findDevice(options);
	}

	// Select Date fn
	private void selectDate(String date) {
		String[] parts = date.split("/");
		GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(parts[2]), Integer.parseInt(parts[0]) - 1,
				Integer.parseInt(parts[1]));
		String IOS = String.format("%tB %<te, %<tY", cal);
		String Android = String.format("%tb %<te, %<tY", cal);
		nativeDriver.manageMobile().scrollingOptions().setNext(MobileNextOptions.SWIPE_UP);
		nativeDriver.manageMobile().scrollingOptions().setMaxScroll(10);
		nativeDriver.findElement(By.xpath(String.format(
				"//*[contains(@contentDesc,\"%s\") or contains(@name,\"%s\")][@isvisible='true' or @hidden='false']",
				Android, IOS))).click();
	}

	// selectCity Fn
	private void selectCity(String loc, String city) {
		nativeDriver.findElement(By.xpath(loc)).click();
		WebElement ele = null;
		try {
			ele = nativeDriver.findElement(By.xpath("//*[contains(text(),'City or Airport')]"));
		} catch (Exception e) {
			nativeDriver.findElement(By.xpath("//*[@contentDesc='Search']")).click();
			ele = nativeDriver.findElement(By.xpath("//*[contains(text(),'City or Airport')]"));
		}
		ele.sendKeys(city);
		nativeDriver.findElement(By.xpath(String.format("//*[contains(text(),'(%s)')]", city))).click();
	}

	// uninstall TripAdvisor app if it is already installed. and then install
	// new app
	private void installApp(String app) {
		if (device.getApplicationNames().contains("TripAdvisor")) {
			device.getNativeDriver("TripAdvisor").uninstall();
		}
		device.installApplication(app);
	}

	@Override
	public void run() {
		String OS = device.getProperty(MobileDeviceProperty.OS);
		try {
			device.open();
			if (OS.equalsIgnoreCase("Android")) {
				installApp("PUBLIC:TripAdvisor_9.7.2.apk");
			} else if (OS.equalsIgnoreCase("IOS")) {
				installApp("PUBLIC:TripAdvisor 10.4.ipa");
			}

			device.home();
			setNativeDriver(device.getNativeDriver("TripAdvisor"));
			nativeDriver.open();
			nativeDriver.manageMobile().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			nativeDriver.findElement(By.xpath("//*[text()='Skip']")).click();
			nativeDriver.findElement(By.xpath("//*[text()='Later' or text()='No thanks']")).click();
			nativeDriver.manageMobile().scrollingOptions().setScroll(true);
			nativeDriver.manageMobile().scrollingOptions().setMaxScroll(2);
			nativeDriver.manageMobile().scrollingOptions().setNext(MobileNextOptions.SWIPE_UP);
			nativeDriver.findElement(By.xpath("//*[text()='Flights']")).click();
			nativeDriver.manageMobile().scrollingOptions().setScroll(false);
			selectCity("//*[@resourceid='android:id/text2' or @name='Select a departure city or airport']",
					gettoCity());

			nativeDriver.manageMobile().scrollingOptions().setScroll(true);
			selectCity(
					"//*[text()='Choose your arrival city or airport.' or @name='Select an arrival city or airport']",
					getfromCity());
			nativeDriver.findElement(By.xpath("//*[text()='Select your dates' or @name='Select Travel Dates']"))
					.click();
			selectDate(gettoDate());
			selectDate(getfromDate());
			nativeDriver.findElement(By.xpath("//*[text()='ROUND TRIP' or text()='Round Trip']")).click();

			nativeDriver.findElement(By.xpath("//*[text()='Done']")).click();
			nativeDriver.manageMobile().scrollingOptions().setMaxScroll(3);
			nativeDriver.findElement(By.xpath("//text[contains(text(),'travel') or contains(text(),'Travel')]"))
					.click();
			nativeDriver.findElement(
					By.xpath(String.format("//text[contains(text(),'%s Travel')][@isvisible='true' or @hidden='false']",
							getPassengers())))
					.click();
			nativeDriver.findElement(By.xpath("//text[text()='My seat class' or text()='Economy']")).click();
			nativeDriver.findElement(By.xpath(
					String.format("//text[text()='%s'][@isvisible='true' or @hidden='false']", getFlightClass())))
					.click();
			nativeDriver.findElement(By.xpath("//*[text()='Find Flights' or text()='Search Flights']")).click();
			Thread.sleep(20000);
			nativeDriver.uninstall();
			device.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
			String filePath = String.format("Perfecto-Master-Assignment-%s-.pdf", OS);
			downloadreport(filePath);
		}

	}

	@Parameters({ "toCity", "fromCity", "toDate", "fromDate", "passengers", "flightClass" })
	@Test
	public void mainTest(String toCity, String fromCity, String toDate, String fromDate, int passengers,
			String flightClass) throws InterruptedException {
		PerfectoMaster android = new PerfectoMaster();
		PerfectoMaster ios = new PerfectoMaster();
		MobileDeviceFindOptions androidOptions = new MobileDeviceFindOptions();
		androidOptions.setOS(MobileDeviceOS.ANDROID);
		androidOptions.setOSVersion("5.1");
		androidOptions.setModel("One M9");
		android.setDevice(androidOptions);
		android.settoDate(toDate);
		android.setfromDate(fromDate);
		android.settoCity(toCity);
		android.setfromCity(fromCity);
		android.setPassengers(passengers);
		android.setFlightClass(flightClass);
		MobileDeviceFindOptions iosOptions = new MobileDeviceFindOptions();
		iosOptions.setOS(MobileDeviceOS.IOS);
		iosOptions.setOSVersion("8.0");
		ios.setDevice(iosOptions);
		ios.settoDate(toDate);
		ios.setfromDate(fromDate);
		ios.settoCity(toCity);
		ios.setfromCity(fromCity);
		ios.setPassengers(passengers);
		ios.setFlightClass(flightClass);
		Thread iosThread = new Thread(ios);
		Thread androidThread = new Thread(android);
		iosThread.start();
		androidThread.start();
		iosThread.join();
		androidThread.join();
	}

	// Method to Download Report in PDF Format
	private void downloadreport(String filePath) {
		InputStream reportStream = driver.downloadReport(MediaType.PDF);
		if (reportStream != null) {
			try {
				FileUtils.write(reportStream, new File(filePath));
			} catch (Exception e) {
				System.out.println("Failed to export report for " + filePath);
			}
		}
	}
}

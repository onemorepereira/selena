package com.pushnpray.projectpi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Timestamp;
import java.util.EmptyStackException;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.*;

public class Scraper {

	/* basic field map */
	private static String url = null;
	private static String usr = null;
	private static String pwd = null;
	private static long freq = 0;
	private static long zoom = 0;
	private static String awsKey = null;
	private static String awsSecret = null;
	private static String awsS3Bucket = null;
	private static String fileName = null;

	public static String logToken = Tokens.getNewID();

	/* selenium driver and class elements */
	final static WebDriver driver = new FirefoxDriver();
	private static WebElement elementUsr = null;
	private static WebElement elementPwd = null;

	/* logback class field */
	final static Logger logger = LoggerFactory.getLogger(Scraper.class.getName());

	public static void main(String[] args) {

		logger.info("instance={}, action={}, value={}", logToken, "init_args", 0);
		ConfigurationProperties.main(null);
		
		/* TARGET URL & CREDENTIALS */
		url = args[0].toString();
		usr = args[1].toString();
		pwd = args[2].toString();

		/* FREQ DEFINES THE SCREEN CAPTURE INTERVAL IN SECONDS */
		freq = Long.parseLong(args[3]) * 1000;

		/* ZOOM DEFINES THE BROWSER HTML-ZOOM CONTROL FACTOR TO USE */
		zoom = Long.parseLong(args[4]);

		/* ENTER THE AWS KEY & SECRET TO CONNECT TO S3 */
		awsKey = args[5].toString();
		awsSecret = args[6].toString();
		awsS3Bucket = args[7].toString();
		fileName = args[8].toString();

		logger.info("instance={}, action={}, value={}", logToken, "init_target_url", url);
		logger.info("instance={}, action={}, value={}", logToken, "init_user_name", usr.hashCode());
		logger.info("instance={}, action={}, value={}", logToken, "init_password", pwd.hashCode());
		logger.info("instance={}, action={}, value={}", logToken, "init_scrape_frequency", freq);
		logger.info("instance={}, action={}, value={}", logToken, "init_zoom_factor", zoom);
		logger.info("instance={}, action={}, value={}", logToken, "init_aws_key", awsKey.hashCode());
		logger.info("instance={}, action={}, value={}", logToken, "init_aws_secret", awsSecret.hashCode());
		logger.info("instance={}, action={}, value={}", logToken, "init_aws_s3_bucket", awsS3Bucket);
		logger.info("instance={}, action={}, value={}", logToken, "init_aws_s3_key", fileName);

		logger.info("instance={}, action={}, value={}", logToken, "do_launch_browser", 0);
		try {
			driver.get(url);
		} catch (Exception e) {
			logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
		}

		if (driver.findElements(By.id("username")).isEmpty()) {
			if (driver.findElements(By.name("username")).isEmpty()) {
				if(driver.findElements(By.id("email")).isEmpty()) {
					throw new EmptyStackException();
				} else {
					elementUsr = driver.findElement(By.id("email"));
				}
			} else {
				elementUsr = driver.findElement(By.name("username"));
			}
		} else {
			elementUsr = driver.findElement(By.name("username"));
		}

		/*
		 * FIND A CONTROL OF TYPE PASSWORD THAT WOULD MOST LIKELY BE THE CONTROL
		 * TO ENTER CREDENTIALS ON
		 */
		elementPwd = driver.findElement(By.xpath("//*[@type='password']"));

		elementUsr.sendKeys(usr);
		elementPwd.sendKeys(pwd);
		elementUsr.submit();

		/* INSTANTIATE AWS CREDENTIAL OBJECTS */
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsKey, awsSecret);

		/* INSTANTIATE AN S3 CLIENT OBJECT */
		AmazonS3 s3Client = new AmazonS3Client(awsCreds);

		/* DEFINE A PUBLIC ACL FOR S3 OBJECTS */
		AccessControlList acl = new AccessControlList();
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);

		/* MAKE SURE WE ARE ON THE RIGHT URL BEFORE STARTING TO SCREEN SCRAPE */
		for (int i = 0; i < 3; i++) {
			String tgtUrl = driver.getCurrentUrl();

			if (tgtUrl == url) {
				break;
			}
			try {
				logger.info("instance={}, action={}, value={}", logToken, "do_wait_for_target_url", 0);
				driver.get(url);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
			}

		}

		/* WE MAXIMIZE THE BROWSER WINDOW */
		logger.info("instance=" + logToken + ", action=do_maximize_browser_window");
		driver.manage().window().maximize();

		/* ADJUST BROWSER ZOOM BY DESIRED FACTOR */
		logger.info("instance=" + logToken + ", action=do_ajust_zoom_factor");
		WebElement html = driver.findElement(By.tagName("html"));
		for (long x = 0; x < zoom; x++) {
			html.sendKeys(Keys.chord(Keys.CONTROL, Keys.ADD));

		}

		/* WE START TAKING SCREENSHOTS HERE */
		logger.info("instance=" + logToken + ", action=do_start_scraper");
		for (int i = 0; i < 1000000; i++) {

			try {
				if (i % 10 == 0)
					logger.info("instance=" + logToken + ", action=init_scrape_count, value=" + Integer.toString(i));

				/* CREATE A FILE OBJECT WE CAN REUSE */
				File scrapedScreen = new File(fileName);

				/* SCRAPE THE SCREEN AND LOAD TO A BYTE ARRAY */
				byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

				/*
				 * INITIALIZE SOME TIMESTAMP VALUES TO ADD TO OUR SCRAPPED IMAGE
				 */
				java.util.Date date = new java.util.Date();
				String imgStamp = new Timestamp(date.getTime()).toString();

				/* READ THE BYTE ARRAY AND LOAD INTO AN IMAGE OBJECT */
				BufferedImage imgObj = ImageIO.read(new ByteArrayInputStream(screenshot));
				Graphics g = imgObj.getGraphics();

				/* SET FONT ATTRIBUTES */
				g.setFont(new Font("Arial", Font.TRUETYPE_FONT, 15));
				g.setColor(Color.GRAY);

				/* DRAW THE TIMESTAMP ON OUR IMAGE OBJECT */
				g.drawString(imgStamp, 10, imgObj.getHeight() - 10);
				g.dispose();

				/* FLUSH THE IMAGE OBJECT TO A FILE OBJECT */
				ImageIO.write(imgObj, "png", scrapedScreen);

				/* PUT THE STATIC SCREENSHOT OBJECT ON S3 */
				s3Client.putObject(
						new PutObjectRequest(awsS3Bucket, fileName, scrapedScreen).withAccessControlList(acl));

			} catch (Exception e) {
				logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
				logger.info("instance=" + logToken + ", action=do_abnormal_exit");
				driver.quit();
			}

			try {
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				logger.error("instance={}, action={}, value={}", logToken, "throw_exception", e.toString());
			}

		}
		logger.info("instance=" + logToken + " , action=do_graceful_exit");
		driver.quit();
	}

}

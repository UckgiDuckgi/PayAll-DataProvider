package com.example.PayAll_DataProvider.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingServiceImpl implements CrawlingService {

	@Value("${crawling.base-url}")
	private String baseUrl;

	private final ObjectMapper objectMapper;
	private final RedisTemplate<String, Object> redisTemplate;
	private final Set<String> shops = Set.of(
		"쿠팡", "11번가", "G마켓", "SSG.COM", "옥션"
	);
	public final List<String> pCodes = Arrays.asList(
		"4060647", "1026291", "16494830", "2085488", "4734659",
		"1128841", "27756731", "19879892", "7626574", "5361457",
		"1754500", "3093363", "1109302", "7090609", "9637956",
		"12673118", "2012426", "16454519", "1992016", "1008149"
	);

	@Override
	// @Scheduled(cron = "0 0 9 * * *")
	public void saveProductToRedis() {
		for (String pCode : pCodes) {
			try {
				LowestPriceDto lowestPriceDto = crawlProductInfo(pCode);
				if (lowestPriceDto != null) {
					String key = "pCode:" + pCode;
					String jsonValue = objectMapper.writeValueAsString(lowestPriceDto);
					redisTemplate.opsForValue().set(key, jsonValue);
					log.info("Saved to Redis - {}", key);
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				log.error("크롤링 실패 또는 Redis 저장 실패: ", e);
			}
		}
	}

	@Override
	public LowestPriceDto crawlProductInfo(String pCode) throws IOException {
		String url = baseUrl + pCode;

		try {
			Document doc = Jsoup.connect(url).get();

			String productName = doc.select("span.title").text();
			String productImage = doc.select("div.photo_w img").attr("src");
			Elements priceRows = doc.select("table.lwst_tbl tbody tr");

			for (Element row : priceRows) {
				Element shopElement = row.select("td.mall div.logo_over a").first();
				if (shopElement != null) {
					String shopName = shopElement.select("img").attr("alt").trim();
					if (shops.contains(shopName)) {
						int price = Integer.parseInt(
							row.select("td.price a span.txt_prc em").text().replaceAll("[^0-9]", ""));
						String shopImage = shopElement.select("img").attr("src").trim();
						String shopUrl = getShopUrl(shopElement.attr("href"));
						// System.out.println("shopUrl = " + shopUrl);
						return LowestPriceDto.builder()
							.productName(productName)
							.productImage(productImage)
							.price(price)
							.shopImage(shopImage)
							.shopName(shopName)
							.shopUrl(shopUrl).build();
					}
				}

			}
			return null;

		} catch (IOException e) {
			log.error("크롤링 실패 - pcode: {}, error: {}", pCode, e.getMessage());
			throw e;
		}

	}

	private String getShopUrl(String bridgeUrl) {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		WebDriver driver = new ChromeDriver(options);

		try {
			driver.get(bridgeUrl);
			Thread.sleep(2000);
			return driver.getCurrentUrl();
		} catch (InterruptedException e) {
			throw new RuntimeException("Selenium 처리 중 오류", e);
		} finally {
			driver.quit();
		}
	}

}

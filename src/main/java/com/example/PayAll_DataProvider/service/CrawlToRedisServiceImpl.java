package com.example.PayAll_DataProvider.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.dto.SearchProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlToRedisServiceImpl implements CrawlToRedisService {

	@Value("${crawling.base-url}")
	private String baseUrl;
	@Value("${crawling.search-url}")
	private String searchUrl;

	private final ObjectMapper objectMapper;
	private final RedisTemplate<String, Object> redisTemplate;
	private WebDriver searchDriver;
	private WebDriver shopDriver;

	@PostConstruct
	public void init() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-extensions");
		// options.setPageLoadTimeout(Duration.ofSeconds(10));
		this.searchDriver = new ChromeDriver(options);
		this.shopDriver = new ChromeDriver(options);
	}

	@PreDestroy
	public void cleanup() {
		if (searchDriver != null) {
			searchDriver.quit();
		}
		if (shopDriver != null) {
			shopDriver.quit();
		}
	}

	private final Map<String, String> shopNameMapping = Map.of(
		"쿠팡", "Coupang",
		"11번가", "11st",
		"G마켓", "GMarket",
		"SSG.COM", "SSG",
		"옥션", "Auction"
	);
	public final List<String> pCodes = Arrays.asList(
		"4060647", "1026291", "16494830", "2085488", "4734659",
		"1128841", "27756731", "19879892", "7626574", "5361457",
		"1754500", "3093363", "1109302", "7090609", "9637956",
		"12673118", "2012426", "16454519", "1992016", "1008149"
	);

	// Redis에서 상품 정보 조회
	@Override
	public LowestPriceDto getProduct(String pCode) throws JsonProcessingException {
		String productJson = (String)redisTemplate.opsForValue().get(pCode);
		if (productJson == null) {
			log.info("not found pCode");
			return null;
		}
		return objectMapper.readValue(productJson, LowestPriceDto.class);

	}

	@Override
	public List<SearchProductDto> getSearchProducts(String query, int page, int size) {
		// query 인코딩 처리
		String url = searchUrl + URLEncoder.encode(query, StandardCharsets.UTF_8);
		List<SearchProductDto> searchResults = new ArrayList<>();

		try {
			shopDriver.get(url);
			List<WebElement> productItems = shopDriver.findElements(By.cssSelector("li[id^=productItem]"));

			if (productItems.isEmpty()) {
				System.out.println("상품 리스트가 없습니다.");
				return Collections.emptyList();
			}

			// pagination
			int start = (page - 1) * size;
			int end = Math.min(start + size, productItems.size());
			List<WebElement> paginatedItems = productItems.subList(start, end);

			for (WebElement productItem : paginatedItems) {
				// productId 추출 후 크롤링으로 3개 쇼핑몰 가져오기
				String productId = Objects.requireNonNull(productItem.getAttribute("id")).replaceAll("[^0-9]", "");
				List<LowestPriceDto> lowestPriceList = crawlProductInfo(productId, 3);

				if (!lowestPriceList.isEmpty()) {
					LowestPriceDto firstDto = lowestPriceList.get(0);

					List<SearchProductDto.ShopInfoDto> shopInfos = lowestPriceList.stream()
						.map(p -> SearchProductDto.ShopInfoDto.builder()
							.shopName(p.getShopName())
							.shopUrl(p.getShopUrl())
							.price(p.getPrice()).build())
						.toList();

					SearchProductDto searchProductDto = SearchProductDto.builder()
						.pCode(firstDto.getPCode())
						.productName(firstDto.getProductName())
						.productImage(firstDto.getProductImage())
						.storeList(shopInfos)
						.build();

					searchResults.add(searchProductDto);
				}

			}
			return searchResults;

		} catch (Exception e) {
			log.error("크롤링 실패: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	// @Scheduled(cron = "0 0 9 * * *")
	public void saveProductToRedis() {
		for (String pCode : pCodes) {
			try {
				List<LowestPriceDto> lowestPriceDtoList = crawlProductInfo(pCode, 1);
				if (lowestPriceDtoList != null) {
					String jsonValue = objectMapper.writeValueAsString(lowestPriceDtoList.get(0));
					redisTemplate.opsForValue().set(pCode, jsonValue);
					log.info("Saved to Redis - {}", pCode);
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				log.error("크롤링 실패 또는 Redis 저장 실패: {}", e.getMessage());
			}
		}
		log.info("상품 데이터 Redis 저장 완료");
	}

	public List<LowestPriceDto> crawlProductInfo(String pCode, int shopCount) throws IOException {
		String url = baseUrl + pCode;
		List<LowestPriceDto> results = new ArrayList<>();

		try {
			Document doc = Jsoup.connect(url).get();

			String productName = doc.select("span.title").text();
			String productImage = doc.select("div.photo_w img").attr("src");
			Elements priceRows = doc.select("table.lwst_tbl tbody tr");

			int count = 0;
			for (Element row : priceRows) {
				if (count >= shopCount)
					break;

				Element shopElement = row.select("td.mall div.logo_over a").first();
				if (shopElement != null) {
					String shopName = shopElement.select("img").attr("alt").trim();
					if (shopNameMapping.containsKey(shopName)) {
						String englishShopName = shopNameMapping.getOrDefault(shopName, shopName);
						Long price = Long.parseLong(
							row.select("td.price a span.txt_prc em").text().replaceAll("[^0-9]", ""));
						// String shopImage = shopElement.select("img").attr("src").trim();
						String shopUrl = getShopUrl(shopElement.attr("href"));

						results.add(LowestPriceDto.builder()
							.pCode(Long.valueOf(pCode))
							.productName(productName)
							.productImage(productImage)
							.price(price)
							.shopName(englishShopName)
							.shopUrl(shopUrl).build());

						count++;
					}
				}

			}
			return results;

		} catch (IOException e) {
			log.error("크롤링 실패 - pcode: {}, error: {}", pCode, e.getMessage());
			throw e;
		}

	}

	// 상품 페이지로 이동 (셀레니움 사용)
	private String getShopUrl(String bridgeUrl) {

		try {
			searchDriver.get(bridgeUrl);
			Thread.sleep(1000);
			return searchDriver.getCurrentUrl();
		} catch (InterruptedException e) {
			throw new RuntimeException("Selenium 처리 중 오류", e);
		}
	}

}

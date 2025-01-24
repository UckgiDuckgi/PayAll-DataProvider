package com.example.PayAll_DataProvider.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.dto.SearchProductDto;
import com.example.PayAll_DataProvider.mapper.SearchProductMapper;
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
	// private WebDriver shopDriver;

	private final Map<String, String> shopNameMapping = Map.of(
		"쿠팡", "Coupang",
		"11번가", "11st",
		"G마켓", "GMarket",
		"SSG.COM", "SSG",
		"옥션", "Auction"
	);
	public final List<String> pCodes = Arrays.asList(
		"1026291", "4060647", "16494830", "2085488", "4734659",
		"1128841", "27756731", "19879892", "7626574", "5361457",
		"1754500", "3093363", "1109302", "7090609", "9637956",
		"12673118", "2012426", "16454519", "1992016", "1008149"
	);

	@PostConstruct
	public void init() {
		// WebDriverManager.chromedriver().driverVersion("132.0.6834.110").setup();
		WebDriverManager.chromedriver().setup();
		this.searchDriver = createNewWebDriver(3195);
		// this.shopDriver = createNewWebDriver(17878);
	}

	@PreDestroy
	public void cleanup() {
		if (searchDriver != null) {
			searchDriver.quit();
		}
		// if (shopDriver != null) {
		// 	shopDriver.quit();
		// }
	}

	public WebDriver createNewWebDriver(int port) {
		ChromeOptions options = new ChromeOptions();
		// ChromeDriverService service = new ChromeDriverService.Builder()
		// 	.usingPort(port)
		// 	.build();

		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-extensions");
		options.addArguments("--remote-debugging-port=" + port);

		return new ChromeDriver(options);
	}

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
		redisTemplate.opsForValue().set("tttt", "ssss");

		// query 인코딩 처리
		String url = searchUrl + query;
		List<CompletableFuture<SearchProductDto>> futures = new ArrayList<>();

		System.out.println("query = " + query);
		if (query.equals("%EB%AC%B8%EA%B7%9C%EB%B9%88") || query.equals("문규빈")) {
			SearchProductDto searchProductDto = SearchProductDto.builder()
				.pCode(11L)
				.productName("문규빈")
				.productImage("ssss").build();
			return List.of(searchProductDto);
		}

		try {

			searchDriver.get(url);
			log.info("!!url{}", url);

			String pageSource = searchDriver.getPageSource();
			Document doc = Jsoup.parse(pageSource);
			System.out.println("doc = " + doc);

			// Document doc = Jsoup.connect(url).get();

			// Thread.sleep(30000);

			Elements productItems = doc.select("ul.product_list li.prod_item");

			// WebDriverWait wait = new WebDriverWait(searchDriver, Duration.ofSeconds(20));

			// List<WebElement> productItems = wait.until(
			// ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li[id^=productItem]")));

			// List<WebElement> productItems = searchDriver.findElements(By.cssSelector("li[id^=productItem]"));
			if (productItems.isEmpty()) {
				log.info("상품 리스트가 없습니다.");
				return Collections.emptyList();
			}

			// pagination
			int start = (page - 1) * size;
			int end = Math.min(start + size, productItems.size());
			List<Element> paginatedItems = productItems.subList(start, end);

			for (Element productItem : paginatedItems) {
				// productId 추출
				String productId = Objects.requireNonNull(productItem.attr("id")).replaceAll("[^0-9]", "");
				// 각 상품마다 3개의 쇼핑몰 크롤링
				futures.add(CompletableFuture.supplyAsync(() ->
					{
						try {
							return crawlProductInfo(productId, 3);
						} catch (IOException e) {
							return Collections.<LowestPriceDto>emptyList();
						}
					})
					.thenApply(SearchProductMapper::toDto)
					.exceptionally(ex -> {
						log.error("상품 정보 변환 실패: productId={}, error={}", productId, ex.getMessage());
						return null;
					}));
			}

			return futures.stream()
				.map(CompletableFuture::join)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		} catch (NoSuchSessionException e) {
			searchDriver = createNewWebDriver(3195);
			searchDriver.get(url);
			throw new RuntimeException("세션이 만료되었습니다", e);
		} catch (Exception e) {
			log.error("크롤링 실패: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public LowestPriceDto crawlingProduct(String pCode) {
		redisTemplate.opsForValue().set("kkkk", "ssss");
		try {
			List<LowestPriceDto> lowestPriceDtoList = crawlProductInfo(pCode, 1);
			if (lowestPriceDtoList != null) {
				String jsonValue = objectMapper.writeValueAsString(lowestPriceDtoList.get(0));
				redisTemplate.opsForValue().set(pCode, jsonValue);

				return lowestPriceDtoList.get(0);
			}
			throw new NotFoundException("상품 정보를 찾을 수 없습니다.");
		} catch (Exception e) {
			throw new RuntimeException("크롤링 실패");
		}

	}

	@Override
	// @Scheduled(cron = "0 0 9 * * *")
	public void saveProductToRedis() {
		redisTemplate.opsForValue().set("qqqq", "ssss");
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

	// 상품 상세 페이지 크롤링
	private List<LowestPriceDto> crawlProductInfo(String pCode, int shopCount) throws IOException {
		String url = baseUrl + pCode;
		List<LowestPriceDto> results = new ArrayList<>();

		try {
			Document doc = Jsoup.connect(url).get();

			String productName = doc.select("span.title").text();
			String productImage = "https:" + doc.select("div.photo_w img").attr("src");
			Elements priceRows = doc.select("table.lwst_tbl tbody tr");

			int count = 0;
			for (Element row : priceRows) {

				Element shopElement = row.select("td.mall div.logo_over a").first();
				if (shopElement != null) {
					String shopName = shopElement.select("img").attr("alt").trim();
					if (shopNameMapping.containsKey(shopName)) {
						String englishShopName = shopNameMapping.get(shopName);
						System.out.println("englishShopName = " + englishShopName);
						count++;
						Long price = Long.parseLong(
							row.select("td.price a span.txt_prc em").text().replaceAll("[^0-9]", ""));
						// String shopImage = shopElement.select("img").attr("src").trim();
						// String shopUrl = getShopUrl(shopElement.attr("href"));

						String shopUrl = row.select("td.price a").attr("href");
						System.out.println("shopUrl = " + shopElement.attr("href"));
						// String shopUrl = "xxxxxxx";
						results.add(LowestPriceDto.builder()
							.pCode(Long.valueOf(pCode))
							.productName(productName)
							.productImage(productImage)
							.price(price)
							.shopName(englishShopName)
							.shopUrl(shopUrl).build());

						// System.out.println("englishShopName = " + englishShopName);
						// System.out.println("shopUrl = " + shopUrl);

						if (count == shopCount)
							break;

					}

				}

			}
			return results;

		} catch (IOException e) {
			log.error("크롤링 실패 - pcode: {}, error: {}", pCode, e.getMessage());
			throw e;
		}

	}

	// 상품 판매 쇼핑몰 페이지로 이동 (셀레니움 사용)
	// 상품 페이지로 이동 (셀레니움 사용)
	// private String getShopUrl(String bridgeUrl) {
	//
	// 	try {
	// 		searchDriver.get(bridgeUrl);
	//
	// 		WebDriverWait wait = new WebDriverWait(searchDriver, Duration.ofSeconds(10));
	// 		wait.until(ExpectedConditions.urlToBe(bridgeUrl));
	// 		return searchDriver.getCurrentUrl();
	//
	// 	} catch (RuntimeException e) {
	// 		throw new RuntimeException("Selenium 처리 중 오류", e);
	// 	}
	// }
	// private String getShopUrl(String bridgeUrl) {
	// 	try {
	// 		searchDriver.get(bridgeUrl);
	// 		Thread.sleep(1000);
	// 		return searchDriver.getCurrentUrl();
	// 	} catch (NoSuchSessionException e) {
	// 		searchDriver = createNewWebDriver(3195);
	// 		searchDriver.get(bridgeUrl);
	// 		try {
	// 			searchDriver.get(bridgeUrl);
	// 			Thread.sleep(1000);
	// 			return searchDriver.getCurrentUrl();
	// 		} catch (Exception ex) {
	// 			throw new RuntimeException("세션이 만료되었습니다", ex);
	// 		}
	// 	} catch (InterruptedException e) {
	// 		Thread.currentThread().interrupt();
	// 		throw new RuntimeException("Selenium 처리 중 오류", e);
	// 	}
	// }

}

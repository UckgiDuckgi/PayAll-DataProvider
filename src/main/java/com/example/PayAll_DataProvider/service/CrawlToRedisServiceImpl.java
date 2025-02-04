package com.example.PayAll_DataProvider.service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
		WebDriverManager.chromedriver().setup();
		this.searchDriver = createNewWebDriver();
	}

	@PreDestroy
	public void cleanup() {
		if (searchDriver != null) {
			searchDriver.quit();
		}
	}

	public WebDriver createNewWebDriver() {
		// File chromeDriverFile = new File("/usr/bin/chromedriver");
		// ChromeDriverService service = new ChromeDriverService.Builder()
		// 	.usingDriverExecutable(chromeDriverFile)
		// 	.usingAnyFreePort()
		// 	.build();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-extensions");
		// options.addArguments("--remote-debugging-port=" + port);

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

			// 명시적 대기 조건 추가
        	WebDriverWait wait = new WebDriverWait(searchDriver, Duration.ofSeconds(10));
        
        	// 1. 페이지 로딩 완료 대기
        	wait.until(webDriver -> ((JavascriptExecutor) webDriver)
            	.executeScript("return document.readyState")
            	.equals("complete"));
            
        	// 2. 특정 요소가 나타날 때까지 대기
        	wait.until(ExpectedConditions.presenceOfElementLocated(
            	By.cssSelector("li[id^=productItem]")));
        
        	// 3. 요소들이 클릭 가능할 때까지 대기
       	 	wait.until(ExpectedConditions.elementToBeClickable(
            	By.cssSelector("li[id^=productItem]")));

			List<WebElement> productItems = searchDriver.findElements(By.cssSelector("li[id^=productItem]"));
			System.out.println("searchDriver = " + searchDriver.getTitle());

			if (productItems.isEmpty()) {
				log.info("상품 리스트가 없습니다.");
				return Collections.emptyList();
			}

			// pagination
			int start = (page - 1) * size;
			int end = Math.min(start + size, productItems.size());
			List<WebElement> paginatedItems = productItems.subList(start, end);

			for (WebElement productItem : paginatedItems) {
				// productId 추출
				String productId = Objects.requireNonNull(productItem.getAttribute("id")).replaceAll("[^0-9]", "");
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
			searchDriver = createNewWebDriver();
			searchDriver.get(url);
			throw new RuntimeException("세션이 만료되었습니다", e);
		} catch (Exception e) {
			log.error("크롤링 실패: {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public LowestPriceDto crawlingProduct(String pCode) {
		try {
			List<LowestPriceDto> lowestPriceDtoList = crawlProductInfo(pCode, 1);
			log.info("!size" + lowestPriceDtoList.size());
			if (lowestPriceDtoList != null) {
				String jsonValue = objectMapper.writeValueAsString(lowestPriceDtoList.get(0));
				log.info("!jsonValue" + jsonValue);
				redisTemplate.opsForValue().set(pCode, jsonValue);
				log.info("redis 저장!!!");
				return lowestPriceDtoList.get(0);
			}
			throw new NotFoundException("상품 정보를 찾을 수 없습니다.");
		} catch (Exception e) {
			throw new RuntimeException("크롤링 실패 " + e.getMessage());
		}

	}

	@Override
	// @Scheduled(cron = "0 0 9 * * *")
	public void saveProductToRedis() {

		Set<String> pcodes = new HashSet<>(pCodes);
		pcodes.addAll(Objects.requireNonNull(redisTemplate.keys("*")));
		System.out.println("pcodes.size() = " + pcodes.size());

		for (String pCode : pcodes) {
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

	@Override
	public void saveToRedis() throws JsonProcessingException {
		LowestPriceDto dto = LowestPriceDto.builder()
			.pCode(10180392L)
			.shopUrl(
				"https://www.coupang.com/vp/products/19984840?itemId=24297003361&vendorItemId=91623037023&src=1032034&spec=10305199&addtag=400&ctag=19984840&lptag=I24297003361&itime=20250204172233&pageType=PRODUCT&pageValue=19984840&wPcid=17358916727837097809278&wRef=prod.danawa.com&wTime=20250204172233&redirect=landing&mcid=079ed67e683b43c9b25b2706a215f097")
			.productName("군용 핫팩 마이핫보온대 군인 손난로 160g 대용량, 50개")
			.productImage(
				"//img.danawa.com/prod_img/500000/392/180/img/10180392_1.jpg?shrink=330:*&amp;_v=20241209102602")
			.price(34900L)
			.shopName("Coupang")
			.build();
		String jsonValue = objectMapper.writeValueAsString(dto);
		redisTemplate.opsForValue().set(String.valueOf(dto.getPCode()), jsonValue);
	}

	// 상품 상세 페이지 크롤링
	private List<LowestPriceDto> crawlProductInfo(String pCode, int shopCount) throws IOException {
		String url = baseUrl + pCode;
		List<LowestPriceDto> results = new ArrayList<>();
		log.info("url = " + url);

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
						Long price = Long.parseLong(
							row.select("td.price a span.txt_prc em").text().replaceAll("[^0-9]", ""));

						// shopUrl 크롤링
						String shopUrl = "";
						String loadingUrl = row.select("td.price a").attr("href");  // 중간페이지 url
						Element script = Jsoup.connect(loadingUrl).get().select("script").get(1); // script 태그 중 2번째

						Pattern pattern = Pattern.compile("goLink\\(\"(.*?)\"\\)");
						Matcher matcher = pattern.matcher(script.html());
						if (matcher.find()) {
							shopUrl = matcher.group(1);
						}

						results.add(LowestPriceDto.builder()
							.pCode(Long.valueOf(pCode))
							.productName(productName)
							.productImage(productImage)
							.price(price)
							.shopName(englishShopName)
							.shopUrl(shopUrl).build());

						count++;
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

}

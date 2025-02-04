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

	List<String> productNames = Arrays.asList("무항생제 신선한 대란", "곰곰 만능두부, 300g, 1개", "오뚜기옛날 사골곰탕 국물",
		"동원홈푸드 통목전지 (냉동), 1kg, 1개", "foodi 양꼬치시즈닝, 130g, 1개", "당일생산 신선 건두부 생생 포두부 두부면, 500g, 1개",
		"다슈 솔루션 퍼퓸 데오 바디스프레이 프레쉬 블루향",
		"다우니 실내건조 플러스 초고농축 섬유유연제 프레시클린 본품, 1.05L, 3개");

	List<String> productImages = Arrays.asList(
		"https://thumbnail6.coupangcdn.com/thumbnails/remote/492x492ex/image/retail/images/110105750838087-30e2fb29-2d8b-4529-80ff-db7665fed6a4.jpg",
		"https://thumbnail8.coupangcdn.com/thumbnails/remote/492x492ex/image/retail/images/623014999248534-b4f1c0c1-532b-496f-838f-a53ab690deb4.jpg",
		"https://thumbnail8.coupangcdn.com/thumbnails/remote/492x492ex/image/1025_amir_coupang_oct_80k/a555/384cd231818943331f2ce2cebcfa30f01fe4eac3431a368e699485789ee2.jpg",
		"https://thumbnail6.coupangcdn.com/thumbnails/remote/492x492ex/image/rs_quotation_api/nkgbfidr/9ab53b0735d2455a8a4342ed1a8d48f3.jpg",
		"https://thumbnail9.coupangcdn.com/thumbnails/remote/492x492ex/image/retail/images/606746242064318-b7bf2dc9-cf35-4e86-99f8-a65a5ef20fa4.jpg",
		"https://thumbnail9.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/8f8b/f6688c79a2bcdec66c1039143b705a885620b65ffe62349fd4d0be590890.png",
		"https://thumbnail6.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/572a/a51864e78d3c6512fae8d982557d4185bc906752c36e20c87c214543160b.jpg"

	);
	List<String> shopUrls = Arrays.asList(
		"https://www.coupang.com/vp/products/4842938988?vendorItemId=73556670010&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/7941558764?vendorItemId=88923203912&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/7470370114?vendorItemId=86601386346&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/7534882027?vendorItemId=86895881505&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/5579837464?vendorItemId=76200787497&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/7255820555?vendorItemId=85512910866&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/7255549427?vendorItemId=4980465309&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=",
		"https://www.coupang.com/vp/products/8544953877?vendorItemId=71805128607&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart="
	);
	List<Long> prices = Arrays.asList(7980L, 980L, 2860L, 10800L, 5300L, 6350L, 13320L, 19580L);

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
		File chromeDriverFile = new File("/usr/bin/chromedriver");
		ChromeDriverService service = new ChromeDriverService.Builder()
			.usingDriverExecutable(chromeDriverFile)
			.usingAnyFreePort()
			.withLogFile(new File("chromedriver.log"))
			.build();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-extensions");
		options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " + "(KHTML, like Gecko) Chrome/132.0.6834.159 Safari/537.36");
		options.addArguments("sec-ch-ua-platform='Windows'");
		options.addArguments("accept-language=ko,en-US;q=0.9,en;q=0.8");
		options.addArguments("sec-fetch-site=same-origin");
		options.addArguments("sec-ch-ua-mobile=?0");
		options.addArguments("accept-encoding=gzip, deflate, br, zstd");
		options.addArguments("--disable-quic");
		options.addArguments("--disable-setuid-sandbox");
		options.addArguments("--disable-blink-features=AutomationControlled");
		options.addArguments("--disable-machine-learning");
		options.addArguments("--disable-speech-api");
		options.addArguments("--disable-voice-input");
		options.addArguments("--disable-translate");
		options.addArguments("--start-minimized");
		options.addArguments("--log-level=3");
		// options.addArguments("--remote-debugging-port=" + port);

		return new ChromeDriver(service, options);
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

			// // 명시적 대기 조건 추가
        	// WebDriverWait wait = new WebDriverWait(searchDriver, Duration.ofSeconds(10));
        
        	// // 1. 페이지 로딩 완료 대기
        	// wait.until(webDriver -> ((JavascriptExecutor) webDriver)
            // 	.executeScript("return document.readyState")
            // 	.equals("complete"));
            
        	// // 2. 특정 요소가 나타날 때까지 대기
        	// wait.until(ExpectedConditions.presenceOfElementLocated(
            // 	By.cssSelector("li[id^=productItem]")));
        
        	// // 3. 요소들이 클릭 가능할 때까지 대기
       	 	// wait.until(ExpectedConditions.elementToBeClickable(
            // 	By.cssSelector("li[id^=productItem]")));
			String pageSource1 = searchDriver.getPageSource();
        	log.info("페이지 소스1: {}", pageSource1.substring(0, Math.min(pageSource1.length(), 1000)));

			List<WebElement> productItems = searchDriver.findElements(By.cssSelector("li[id^=productItem]"));
			System.out.println("searchDriver = " + searchDriver.getTitle());

			String pageSource = searchDriver.getPageSource();
        	log.info("페이지 소스2: {}", pageSource.substring(0, Math.min(pageSource.length(), 1000)));
        
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
		
	@Override
	public void saveToRedis() throws JsonProcessingException {

		for (int i = 0; i < productImages.size(); i++) {
			LowestPriceDto dto = LowestPriceDto.builder()
				.pCode(100000L + i)
				.shopUrl(shopUrls.get(i))
				.productName(productNames.get(i))
				.productImage(productImages.get(i))
				.price(prices.get(i))
				.shopName("Coupang")
				.build();

			String jsonValue = objectMapper.writeValueAsString(dto);
			redisTemplate.opsForValue().set(String.valueOf(dto.getPCode()), jsonValue);
		}
	}

}

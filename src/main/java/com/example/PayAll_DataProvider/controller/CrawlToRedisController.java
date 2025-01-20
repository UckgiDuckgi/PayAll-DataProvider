package com.example.PayAll_DataProvider.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.dto.SearchProductDto;
import com.example.PayAll_DataProvider.service.CrawlToRedisService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class CrawlToRedisController {
	private final CrawlToRedisService crawlToRedisService;

	@GetMapping("/product/{pCode}")
	public ResponseEntity<LowestPriceDto> getProduct(@PathVariable String pCode) throws JsonProcessingException {
		return ResponseEntity.ok(crawlToRedisService.getProduct(pCode));
	}

	@PostMapping("/test")
	public ResponseEntity<String> saveProductToRedis() {
		crawlToRedisService.saveProductToRedis();
		return ResponseEntity.ok("크롤링 및 Redis 저장 완료");
	}

	@GetMapping("/search")
	public ResponseEntity<List<SearchProductDto>> searchProducts(@RequestParam int page, @RequestParam int size,
		@RequestParam String query) {
		return ResponseEntity.ok(crawlToRedisService.getSearchProducts(query, page, size));

	}

}

package com.example.PayAll_DataProvider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
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

	// @GetMapping("/crawl")
	// public LowestPriceDto crawl(@RequestParam String pcode) {
	// 	try {
	// 		return crawlingService.crawlProductInfo(pcode);
	// 	} catch (IOException e) {
	// 		throw new RuntimeException("크롤링 실패", e);
	// 	}
	// }
}

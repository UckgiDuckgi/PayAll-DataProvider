package com.example.PayAll_DataProvider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.service.CrawlToRedisService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class CrawlToRedisController {
	private final CrawlToRedisService crawlingService;

	@PostMapping("/test")
	public ResponseEntity<String> saveProductToRedis() {
		crawlingService.saveProductToRedis();
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

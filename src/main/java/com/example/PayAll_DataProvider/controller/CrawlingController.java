package com.example.PayAll_DataProvider.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.service.CrawlingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class CrawlingController {
	private final CrawlingService crawlingService;

	@PostMapping("/test")
	public ResponseEntity<String> saveProductToRedis() {
		crawlingService.saveProductToRedis();
		return ResponseEntity.ok("크롤링 및 Redis 저장 완료");
	}

	@GetMapping("/crawl")
	public LowestPriceDto crawl(@RequestParam String pcode) {
		try {
			return crawlingService.crawlProductInfo(pcode);
		} catch (IOException e) {
			throw new RuntimeException("크롤링 실패", e);
		}
	}
}
